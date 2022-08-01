package com.almostreliable.lazierae2.content.assembler.controller;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.AECableType;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.me.helpers.MachineSource;
import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.MultiBlockData;
import com.almostreliable.lazierae2.core.Config;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Entities.Assembler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static com.almostreliable.lazierae2.core.Constants.Nbt.*;

public class ControllerEntity extends GenericEntity implements IInWorldGridNodeHost, IGridConnectedBlockEntity, IGridTickable, ICraftingProvider {

    final ControllerData controllerData;
    final CraftingQueue craftingQueue;
    private final IManagedGridNode mainNode;
    private final IActionSource actionSource;
    @Nullable private MultiBlockData multiBlockData;

    private boolean isSleeping;
    private int work;

    public ControllerEntity(BlockPos pos, BlockState state) {
        super(Assembler.CONTROLLER.get(), pos, state);
        controllerData = new ControllerData(this);
        craftingQueue = new CraftingQueue(this);
        mainNode = setupMainNode();
        actionSource = new MachineSource(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (multiBlockData != null) {
            onMultiBlockCreated();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mainNode.loadFromNBT(tag);
        if (tag.contains(MULTIBLOCK_DATA_ID)) multiBlockData = MultiBlockData.load(tag.getCompound(MULTIBLOCK_DATA_ID));
        if (tag.contains(CONTROLLER_DATA_ID)) controllerData.deserializeNBT(tag.getCompound(CONTROLLER_DATA_ID));
        if (tag.contains(CRAFTING_QUEUE_ID)) craftingQueue.deserializeNBT(tag.getCompound(CRAFTING_QUEUE_ID));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        mainNode.saveToNBT(tag);
        if (multiBlockData != null) tag.put(MULTIBLOCK_DATA_ID, MultiBlockData.save(multiBlockData));
        tag.put(CONTROLLER_DATA_ID, controllerData.serializeNBT());
        tag.put(CRAFTING_QUEUE_ID, craftingQueue.serializeNBT());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        if (multiBlockData == null) return null;
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        mainNode.destroy();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        mainNode.destroy();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, isSleeping, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (level == null || level.isClientSide) return TickRateModulation.SLEEP;
        if (multiBlockData == null || !mainNode.isActive()) {
            return isSleeping ? TickRateModulation.SLEEP : TickRateModulation.IDLE;
        }

        craftingQueue.exportOutputs(getGrid().getStorageService().getInventory(), actionSource);

        if (craftingQueue.isEmpty()) {
            resetWork();
            updateSleepState();
            return TickRateModulation.SLEEP;
        }
        if (isSleeping) return TickRateModulation.SLEEP;

        var currentWork = work;
        var previousWork = currentWork;
        var workPerJob = Config.COMMON.assemblerWorkPerJob.get();
        var workToDo = Math.min(
            Config.COMMON.assemblerWorkPerTickBase.get() + Config.COMMON.assemblerWorkPerTickUpgrade.get() * controllerData.getAccelerators(),
            craftingQueue.getJobAmount() * workPerJob - currentWork
        );
        if (workToDo > 0) {
            var energyCost = Config.COMMON.assemblerEnergyPerWorkBase.get() + Config.COMMON.assemblerEnergyPerWorkUpgrade.get() * controllerData.getAccelerators();
            if (energyCost > 0) {
                var extracted = getGrid()
                    .getEnergyService()
                    .extractAEPower(energyCost * workToDo, Actionable.MODULATE, PowerMultiplier.CONFIG);
                if (extracted > 0) {
                    currentWork += (int) Math.ceil(extracted / energyCost);
                }
            } else {
                currentWork += workToDo;
            }
        }
        while (currentWork >= workPerJob) {
            if (craftingQueue.dispatchJob()) {
                currentWork -= workPerJob;
            } else {
                break;
            }
        }
        if (previousWork != currentWork) {
            work = currentWork;
            saveChanges();
            updateSleepState();
        }
        return TickRateModulation.FASTER;
    }

    @Nullable
    @Override
    public IGridNode getGridNode(Direction dir) {
        var node = mainNode.getNode();
        if (node != null && node.isExposedOnSide(dir)) {
            return node;
        }
        return null;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ControllerMenu(windowId, this, inventory);
    }

    @Override
    protected void playerDestroy(boolean creative) {
        // TODO: implement
    }

    private void resetWork() {
        if (work > 0) {
            work = 0;
            saveChanges();
        }
    }

    private void updateSleepState() {
        var wasSleeping = isSleeping;
        isSleeping = craftingQueue.isEmpty() && !craftingQueue.canExport();
        if (wasSleeping != isSleeping) {
            mainNode.ifPresent((grid, node) -> {
                if (isSleeping) {
                    grid.getTickManager().sleepDevice(node);
                } else {
                    grid.getTickManager().wakeDevice(node);
                }
            });
        }
    }

    private IManagedGridNode setupMainNode() {
        return GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE)
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .addService(IGridTickable.class, this)
            .addService(ICraftingProvider.class, this)
            .setVisualRepresentation(Blocks.REQUESTER.get())
            .setInWorldNode(true)
            .setTagName("proxy")
            .setIdlePowerUsage(Config.COMMON.assemblerIdleEnergy.get());
    }

    private void onMultiBlockCreated() {
        if (level == null || level.isClientSide) return;
        controllerData.updatePatterns();
        level.setBlock(getBlockPos(), getBlockState().setValue(GenericBlock.ACTIVE, true), 1 | 2);
        if (mainNode.isReady()) {
            mainNode.setExposedOnSides(EnumSet.of(getBlockState().getValue(ControllerBlock.FACING)));
        } else {
            mainNode.create(level, worldPosition);
        }
    }

    private void onMultiBlockDestroyed() {
        if (level == null || level.isClientSide) return;
        mainNode.setExposedOnSides(EnumSet.noneOf(Direction.class));
    }

    public int getWork() {
        return work;
    }

    @Nullable
    MultiBlockData getMultiBlockData() {
        return multiBlockData;
    }

    void setMultiBlockData(@Nullable MultiBlockData multiBlockData) {
        this.multiBlockData = multiBlockData;
        if (multiBlockData == null) {
            onMultiBlockDestroyed();
        } else {
            onMultiBlockCreated();
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return controllerData.patterns;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!mainNode.isActive() || !controllerData.patterns.contains(patternDetails) || craftingQueue.isFull()) {
            return false;
        }
        if (!(patternDetails instanceof AECraftingPattern pattern)) return false;
        craftingQueue.pushJob(pattern, inputHolder);
        updateSleepState();
        saveChanges();
        return true;
    }

    @Override
    public boolean isBusy() {
        return craftingQueue.isFull();
    }

    @Override
    public IManagedGridNode getMainNode() {
        return mainNode;
    }

    @Override
    public void securityBreak() {
        if (level == null || level.isClientSide) return;
        level.destroyBlock(worldPosition, true);
    }

    @Override
    public void saveChanges() {
        if (level == null) return;
        if (level.isClientSide) {
            setChanged();
        } else {
            level.blockEntityChanged(worldPosition);
        }
    }

    private IGrid getGrid() {
        var grid = mainNode.getGrid();
        Objects.requireNonNull(grid, "RequesterEntity was not fully initialized - Grid is null");
        return grid;
    }
}

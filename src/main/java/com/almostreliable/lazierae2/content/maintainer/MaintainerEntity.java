package com.almostreliable.lazierae2.content.maintainer;

import appeng.api.config.Actionable;
import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.me.helpers.MachineSource;
import com.almostreliable.lazierae2.component.InventoryHandler.RequestInventory;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Entities;
import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.network.PacketHandler;
import com.almostreliable.lazierae2.network.packets.MaintainerSyncPacket;
import com.almostreliable.lazierae2.util.TextUtil;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumSet;

import static com.almostreliable.lazierae2.core.Constants.*;

public class MaintainerEntity extends GenericEntity implements IInWorldGridNodeHost, IGridConnectedBlockEntity, IGridTickable, IStorageWatcherNode, ICraftingRequester {

    private static final int SLOTS = 6;
    public final long[] knownStorageAmounts;
    public final RequestInventory craftRequests;
    private final IManagedGridNode mainNode;
    private final IActionSource actionSource;
    private final MaintainerCraftTracker craftTracker;
    private final GenericStackInv craftResults;
    @Nullable
    private IStackWatcher stackWatcher;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public MaintainerEntity(
        BlockPos pos, BlockState state
    ) {
        super(Entities.MAINTAINER.get(), pos, state);
        mainNode = createMainNode();
        actionSource = new MachineSource(this);
        craftTracker = new MaintainerCraftTracker(this, SLOTS);
        craftRequests = new RequestInventory(this, SLOTS);
        craftResults = new GenericStackInv(this::setChanged, SLOTS);
        knownStorageAmounts = new long[SLOTS];
        Arrays.fill(knownStorageAmounts, -1);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        mainNode.create(level, worldPosition);
    }

    public void syncData(int slot, int flags) {
        if (level == null || level.isClientSide) return;
        var packet = new MaintainerSyncPacket(slot,
            flags,
            craftRequests.getState(slot),
            craftRequests.getStackInSlot(slot),
            craftRequests.getCount(slot),
            craftRequests.getBatch(slot)
        );
        PacketHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)),
            packet
        );
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(CRAFT_TRACKER_ID)) craftTracker.deserializeNBT(tag.getCompound(CRAFT_TRACKER_ID));
        if (tag.contains(CRAFT_REQUESTS_ID)) craftRequests.deserializeNBT(tag.getCompound(CRAFT_REQUESTS_ID));
        if (tag.contains(CRAFT_RESULTS_ID)) craftResults.readFromChildTag(tag, CRAFT_RESULTS_ID);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(CRAFT_TRACKER_ID, craftTracker.serializeNBT());
        tag.put(CRAFT_REQUESTS_ID, craftRequests.serializeNBT());
        craftResults.writeToChildTag(tag, CRAFT_RESULTS_ID);
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new MaintainerMenu(windowId, this, inventory);
    }

    @Nullable
    @Override
    public IGridNode getGridNode(Direction dir) {
        return mainNode.getNode();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!mainNode.isActive()) return TickRateModulation.IDLE;
        var didWork = doWork();
        if (didWork) return hasWork() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
        return TickRateModulation.IDLE;
    }

    @Override
    public void updateWatcher(IStackWatcher newWatcher) {
        stackWatcher = newWatcher;
        resetWatcher();
    }

    @Override
    public void onStackChange(AEKey what, long amount) {
        for (var i = 0; i < SLOTS; i++) {
            if (craftRequests.matches(i, what)) {
                knownStorageAmounts[i] = amount;
            }
        }
    }

    public void resetWatcher() {
        if (stackWatcher != null) {
            stackWatcher.reset();
            craftRequests.populateWatcher(stackWatcher);
        }
    }

    private boolean hasWork() {
        var hasWork = craftRequests.isRequesting() || !craftResults.isEmpty();
        changeActivityState(hasWork);
        return hasWork;
    }

    private boolean doWork() {
        if (level == null || level.isClientSide) return false;
        var grid = mainNode.getGrid();
        if (grid == null) return false;

        var storage = grid.getStorageService().getInventory();
        var energy = grid.getEnergyService();
        var crafting = grid.getCraftingService();
        var exported = handleExport(storage, energy);
        var crafted = handleCraft(storage, crafting);

        var workDone = exported || crafted;
        if (workDone) setChanged();
        return workDone;
    }

    private boolean handleCraft(MEStorage storage, ICraftingService crafting) {
        assert level != null;
        var workDone = false;
        for (var i = 0; i < SLOTS; i++) {
            if (craftRequests.isRequesting(i)) {
                if (knownStorageAmounts[i] == -1) {
                    var stack = craftRequests.getStackInSlot(i);
                    if (stack.isEmpty()) continue;
                    var aeStack = GenericStack.fromItemStack(stack);
                    if (aeStack == null) continue;
                    knownStorageAmounts[i] = storage.getAvailableStacks().get(aeStack.what());
                    workDone = true;
                }
                if (craftRequests.getState(i) && craftResults.getAmount(i) == 0 && !craftTracker.isBusy(i)) {
                    var toCraft = craftRequests.computeDelta(i, knownStorageAmounts[i]);
                    if (toCraft > 0) {
                        var aeStack = craftRequests.request(i, (int) toCraft);
                        if (craftTracker.requestCraft(i,
                            aeStack.what(),
                            aeStack.amount(),
                            level,
                            crafting,
                            actionSource
                        )) {
                            workDone = true;
                        }
                    }
                }
            }
        }
        return workDone;
    }

    private boolean handleExport(MEStorage storage, IEnergyService energy) {
        var workDone = false;
        for (var i = 0; i < SLOTS; i++) {
            var stack = craftResults.getStack(i);
            if (stack == null) continue;
            var inserted = StorageHelper.poweredInsert(energy,
                storage,
                stack.what(),
                stack.amount(),
                actionSource,
                Actionable.MODULATE
            );
            if (inserted > 0) {
                workDone = true;
                var remaining = stack.amount() - inserted;
                craftResults.setStack(i, remaining == 0 ? null : new GenericStack(stack.what(), remaining));
            }
        }
        return workDone;
    }

    private IManagedGridNode createMainNode() {
        return GridHelper
            .createManagedNode(this, BlockEntityNodeListener.INSTANCE)
            .addService(IStorageWatcherNode.class, this)
            .addService(ICraftingRequester.class, this)
            .addService(IGridTickable.class, this)
            .setVisualRepresentation(Blocks.MAINTAINER.get())
            .setInWorldNode(true)
            .setTagName("proxy")
            .setIdlePowerUsage(10)
            .setExposedOnSides(EnumSet.allOf(Direction.class));
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

    @Override
    public Component getDisplayName() {
        return TextUtil.translate(TRANSLATE_TYPE.BLOCK, MAINTAINER_ID);
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return craftTracker.getRequestedJobs();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        var slot = craftTracker.getSlot(link);
        if (slot == -1) return amount;
        return craftResults.insert(what, amount, mode, actionSource);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        craftTracker.jobStateChange(link);
    }
}

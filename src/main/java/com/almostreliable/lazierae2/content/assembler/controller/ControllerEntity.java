package com.almostreliable.lazierae2.content.assembler.controller;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.AECableType;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
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

import static com.almostreliable.lazierae2.core.Constants.Nbt.CONTROLLER_DATA_ID;
import static com.almostreliable.lazierae2.core.Constants.Nbt.MULTIBLOCK_DATA_ID;

public class ControllerEntity extends GenericEntity implements IInWorldGridNodeHost, IGridConnectedBlockEntity, ICraftingProvider {

    final ControllerData controllerData;
    private final IManagedGridNode mainNode;
    @Nullable private MultiBlockData multiBlockData;

    public ControllerEntity(BlockPos pos, BlockState state) {
        super(Assembler.CONTROLLER.get(), pos, state);
        controllerData = new ControllerData(this);
        mainNode = setupMainNode();
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
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        mainNode.saveToNBT(tag);
        if (multiBlockData != null) tag.put(MULTIBLOCK_DATA_ID, MultiBlockData.save(multiBlockData));
        tag.put(CONTROLLER_DATA_ID, controllerData.serializeNBT());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // used to sync the inventory to clients after
        // the multiblock is formed
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

    private IManagedGridNode setupMainNode() {
        return GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE)
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .addService(ICraftingProvider.class, this)
            .setVisualRepresentation(Blocks.REQUESTER.get())
            .setInWorldNode(true)
            .setTagName("proxy")
            .setIdlePowerUsage(Config.COMMON.requesterIdleEnergy.get());
    }

    private void onMultiBlockCreated() {
        controllerData.updatePatterns();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            if (mainNode.isReady()) {
                mainNode.setExposedOnSides(EnumSet.of(getBlockState().getValue(ControllerBlock.FACING)));
            } else {
                mainNode.create(level, worldPosition);
            }
        }
    }

    private void onMultiBlockDestroyed() {
        mainNode.setExposedOnSides(EnumSet.noneOf(Direction.class));
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
        return controllerData.getPatterns();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        // TODO: implement
        return false;
    }

    @Override
    public boolean isBusy() {
        // TODO: implement
        return false;
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
}

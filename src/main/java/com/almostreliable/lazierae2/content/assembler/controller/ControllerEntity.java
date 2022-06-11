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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.almostreliable.lazierae2.core.Constants.Nbt.DATA_ID;

public class ControllerEntity extends GenericEntity implements IInWorldGridNodeHost, IGridConnectedBlockEntity, ICraftingProvider {

    private final IManagedGridNode mainNode;
    private final List<IPatternDetails> patterns = new ArrayList<>();
    @Nullable private MultiBlockData data;

    public ControllerEntity(BlockPos pos, BlockState state) {
        super(Assembler.ASSEMBLER_CONTROLLER.get(), pos, state);
        mainNode = createMainNode();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        mainNode.create(level, worldPosition);
        updatePatterns();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(DATA_ID)) {
            data = MultiBlockData.load(tag.getCompound(DATA_ID));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (data != null) {
            tag.put(DATA_ID, MultiBlockData.save(data));
        }
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return null;
    }

    @Override
    protected void playerDestroy(boolean creative) {
        // TODO: implement
    }

    void updatePatterns() {
        patterns.clear();
        // TODO: iterate through pattern holders and upadate patterns
    }

    private IManagedGridNode createMainNode() {
        var exposedSides = EnumSet.allOf(Direction.class);
        exposedSides.remove(getBlockState().getValue(ControllerBlock.FACING));
        return GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE)
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .addService(ICraftingProvider.class, this)
            .setVisualRepresentation(Blocks.REQUESTER.get())
            .setInWorldNode(true)
            .setTagName("proxy")
            .setIdlePowerUsage(Config.COMMON.requesterIdleEnergy.get())
            .setExposedOnSides(exposedSides);
    }

    @Nullable
    MultiBlockData getData() {
        return data;
    }

    void setData(@Nullable MultiBlockData data) {
        this.data = data;
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return patterns;
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

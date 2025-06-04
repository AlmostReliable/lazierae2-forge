package com.almostreliable.lazierae2.blockentity;

import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import com.almostreliable.lazierae2.registry.LazierBlockEntities;
import com.almostreliable.lazierae2.registry.LazierBlocks;
import com.almostreliable.lib.block.SideConfigurable;
import com.almostreliable.lib.block.SideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

// 1 FE = 2 AE
public class CrystalInjectorBlockEntity extends AENetworkPowerBlockEntity implements IGridTickable, IUpgradeableObject, SideConfigurable {

    private final IUpgradeInventory upgrades;
    private final AppEngInternalInventory inventory;
    private final SideConfiguration sideConfig;

    private IssueState issueState = IssueState.NONE;
    // private Recipe recipe;
    private int progress;

    public CrystalInjectorBlockEntity(BlockPos pos, BlockState state) {
        super(LazierBlockEntities.INJECTOR.get(), pos, state);

        getMainNode()
            .setExposedOnSides(EnumSet.noneOf(Direction.class))
            .setFlags(GridFlags.REQUIRE_CHANNEL) // TODO: make configurable
            .addService(IGridTickable.class, this);

        upgrades = UpgradeInventories.forMachine(
            LazierBlocks.INJECTOR,
            5,
            this::saveChanges
        ); // TODO: make speed configurable
        inventory = new AppEngInternalInventory(2);
        sideConfig = new SideConfiguration(this, this::saveChanges);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        upgrades.writeToNBT(tag, "upgrades");
        inventory.writeToNBT(tag, "inventory");
        tag.put("side_config", sideConfig.serialize());
        tag.putInt("issue_state", issueState.ordinal());
        tag.putInt("progress", progress);
    }

    @Override
    public void loadTag(CompoundTag tag) {
        super.loadTag(tag);
        upgrades.readFromNBT(tag, "upgrades");
        inventory.readFromNBT(tag, "inventory");
        sideConfig.deserialize(tag.getCompound("side_config"));
        issueState = IssueState.values()[tag.getInt("issue_state")];
        progress = tag.getInt("progress");
    }

    @Override
    public InternalInventory getInternalInventory() {
        return inventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {

    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return inventory;
        }
        if (id.equals(ISegmentedInventory.UPGRADES)) {
            return upgrades;
        }
        return super.getSubInventory(id);
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public void setOrientation(Direction front, Direction up) {
        super.setOrientation(front, up);
        var sides = EnumSet.complementOf(EnumSet.of(getForward()));
        getMainNode().setExposedOnSides(sides);
        setPowerSides(sides);
    }

    @Override
    public void onReady() {
        getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(getForward())));
        super.onReady();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return TickRateModulation.SAME;
    }

    @Override
    public Direction getFacing() {
        return getForward();
    }

    @Nullable
    @Override
    public Direction getTop() {
        return getUp();
    }

    public enum IssueState {
        NONE, // the machine is working normally
        POWER, // the machine has no power
        INGREDIENT, // the machine has no ingredient to inject into the existing target
        TARGET // the machine has no target to inject into
    }
}

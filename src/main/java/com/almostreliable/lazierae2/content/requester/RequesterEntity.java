package com.almostreliable.lazierae2.content.requester;

import appeng.api.config.Actionable;
import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.me.helpers.MachineSource;
import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.core.Config;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Entities;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;
import com.almostreliable.lazierae2.progression.ClientState;
import com.almostreliable.lazierae2.progression.CraftingLinkState;
import com.almostreliable.lazierae2.progression.IProgressionState;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;

import static com.almostreliable.lazierae2.core.Constants.Nbt.*;

public class RequesterEntity extends GenericEntity implements IInWorldGridNodeHost, IGridConnectedBlockEntity, IGridTickable, ICraftingRequester {

    private static final int SLOTS = 6;
    public final RequesterInventory craftRequests;
    private final IProgressionState[] progressions;
    private final IManagedGridNode mainNode;
    private final IActionSource actionSource;
    private final StorageManager storageManager;
    private TickRateModulation currentTickRateModulation = TickRateModulation.IDLE;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public RequesterEntity(
        BlockPos pos, BlockState state
    ) {
        super(Entities.REQUESTER.get(), pos, state);
        actionSource = new MachineSource(this);
        craftRequests = new RequesterInventory(this, SLOTS);
        storageManager = new StorageManager(this, SLOTS);
        progressions = new IProgressionState[SLOTS];
        Arrays.fill(progressions, IProgressionState.IDLE);
        mainNode = setupMainNode();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            mainNode.create(level, worldPosition);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mainNode.loadFromNBT(tag);
        if (tag.contains(CRAFT_REQUESTS_ID)) craftRequests.deserializeNBT(tag.getCompound(CRAFT_REQUESTS_ID));
        if (tag.contains(STORAGE_MANAGER_ID)) storageManager.deserializeNBT(tag.getCompound(STORAGE_MANAGER_ID));
        if (tag.contains(PROGRESSION_STATES_ID)) loadStates(tag.getCompound(PROGRESSION_STATES_ID));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        mainNode.saveToNBT(tag);
        tag.put(CRAFT_REQUESTS_ID, craftRequests.serializeNBT());
        tag.put(STORAGE_MANAGER_ID, storageManager.serializeNBT());
        tag.put(PROGRESSION_STATES_ID, saveStates());
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
        return new RequesterMenu(windowId, this, inventory);
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

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!mainNode.isActive()) return TickRateModulation.IDLE;
        if (level == null || level.isClientSide) return TickRateModulation.IDLE;
        if (handleProgressions()) {
            setChanged();
        }
        updateActivityState();
        return currentTickRateModulation;
    }

    public IProgressionState getProgression(int slot) {
        return progressions[slot];
    }

    public void setClientProgression(int slot, PROGRESSION_TYPE type) {
        progressions[slot] = new ClientState(type);
    }

    @Override
    protected void playerDestroy(boolean creative) {
        assert level != null;
        storageManager.dropContents();
        if (creative) return;
        var stack = new ItemStack(Blocks.REQUESTER.get());
        level.addFreshEntity(new ItemEntity(
            level,
            worldPosition.getX() + 0.5,
            worldPosition.getY() + 0.5,
            worldPosition.getZ() + 0.5,
            stack
        ));
    }

    private void loadStates(CompoundTag tag) {
        for (var slot = 0; slot < progressions.length; slot++) {
            if (tag.contains(String.valueOf(slot))) {
                var stateTag = tag.getCompound(String.valueOf(slot));
                var link = StorageHelper.loadCraftingLink(stateTag, this);
                progressions[slot] = new CraftingLinkState(link);
            }
        }
    }

    private CompoundTag saveStates() {
        var tag = new CompoundTag();
        for (var slot = 0; slot < progressions.length; slot++) {
            var state = progressions[slot];
            if (state instanceof CraftingLinkState cls) {
                var stateTag = new CompoundTag();
                cls.link().writeToNBT(stateTag);
                tag.put(String.valueOf(slot), stateTag);
            }
        }
        return tag;
    }

    private boolean handleProgressions() {
        var changed = false;

        var tickRateModulation = TickRateModulation.IDLE;
        for (var slot = 0; slot < progressions.length; slot++) {
            var state = progressions[slot];
            var result = handleProgression(slot);
            if (!Objects.equals(state, result)) {
                changed = true;
            }
            var resultTickRateModulation = result.getTickRateModulation();
            if (resultTickRateModulation.ordinal() > tickRateModulation.ordinal()) {
                tickRateModulation = resultTickRateModulation;
            }

            progressions[slot] = result;
        }
        currentTickRateModulation = tickRateModulation;
        return changed;
    }

    private IProgressionState handleProgression(int slot) {
        var state = progressions[slot];
        progressions[slot] = state.handle(this, slot);
        if (progressions[slot].type() != PROGRESSION_TYPE.IDLE && !Objects.equals(progressions[slot], state)) {
            return handleProgression(slot);
        }

        return progressions[slot];
    }

    private void updateActivityState() {
        for (var progression : progressions) {
            if (progression.type() == PROGRESSION_TYPE.EXPORT || progression.type() == PROGRESSION_TYPE.LINK) {
                changeActivityState(true);
                return;
            }
        }

        changeActivityState(false);
    }

    private IManagedGridNode setupMainNode() {
        var exposedSides = EnumSet.allOf(Direction.class);
        exposedSides.remove(getBlockState().getValue(GenericBlock.FACING));
        var node = GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE)
            .addService(IStorageWatcherNode.class, storageManager)
            .addService(ICraftingRequester.class, this)
            .addService(IGridTickable.class, this)
            .setVisualRepresentation(Blocks.REQUESTER.get())
            .setInWorldNode(true)
            .setTagName("proxy")
            .setIdlePowerUsage(Config.COMMON.requesterIdleEnergy.get())
            .setExposedOnSides(exposedSides);
        if (Config.COMMON.requesterRequireChannel.get()) {
            node.setFlags(GridFlags.REQUIRE_CHANNEL);
        }
        return node;
    }

    public IActionSource getActionSource() {
        return actionSource;
    }

    public StorageManager getStorageManager() {
        return storageManager;
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
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return Arrays.stream(progressions)
            .filter(CraftingLinkState.class::isInstance)
            .map(state -> ((CraftingLinkState) state).link())
            .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        for (var slot = 0; slot < progressions.length; slot++) {
            var state = progressions[slot];
            if (state instanceof CraftingLinkState cls && cls.link().equals(link)) {
                if (!mode.isSimulate()) storageManager.get(slot).update(what, amount);
                return amount;
            }
        }
        throw new IllegalStateException("No CraftingLinkState found");
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        // state change is handled by state pattern
    }

    public IGrid getMainNodeGrid() {
        var grid = mainNode.getGrid();
        Objects.requireNonNull(grid, "RequesterEntity was not fully initialized - Grid is null");
        return grid;
    }
}

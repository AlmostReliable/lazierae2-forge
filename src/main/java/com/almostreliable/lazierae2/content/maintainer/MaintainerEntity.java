package com.almostreliable.lazierae2.content.maintainer;

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
import com.almostreliable.lazierae2.component.InventoryHandler.RequestInventory;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.core.Config;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Entities;
import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.network.PacketHandler;
import com.almostreliable.lazierae2.network.packets.MaintainerSyncPacket;
import com.almostreliable.lazierae2.progression.CraftingLinkState;
import com.almostreliable.lazierae2.progression.ExportSlotState;
import com.almostreliable.lazierae2.progression.ProgressionState;
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
import java.util.Objects;

import static com.almostreliable.lazierae2.core.Constants.*;

public class MaintainerEntity extends GenericEntity implements IInWorldGridNodeHost, IGridConnectedBlockEntity, IGridTickable, ICraftingRequester {

    private static final int SLOTS = 6;
    public final RequestInventory craftRequests;
    private final IManagedGridNode mainNode;
    private final IActionSource actionSource;
    private final ProgressionState[] progressions;
    private final StorageManager storageManager;
    private TickRateModulation currentTickRateModulation = TickRateModulation.IDLE;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public MaintainerEntity(
        BlockPos pos, BlockState state
    ) {
        super(Entities.MAINTAINER.get(), pos, state);
        actionSource = new MachineSource(this);
        craftRequests = new RequestInventory(this, SLOTS);
        // craftResults = new GenericStackInv(this::setChanged, SLOTS);
        // knownStorageAmounts = new long[SLOTS];
        storageManager = new StorageManager(this, SLOTS);
        progressions = new ProgressionState[SLOTS];
        Arrays.fill(progressions, ProgressionState.IDLE_STATE);
        mainNode = createMainNode();
    }

    public RequestInventory getCraftRequests() {
        return craftRequests;
    }

    public IActionSource getActionSource() {
        return actionSource;
    }

    public StorageManager getStorageManager() {
        return storageManager;
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
        if (tag.contains(CRAFT_REQUESTS_ID)) craftRequests.deserializeNBT(tag.getCompound(CRAFT_REQUESTS_ID));
        if (tag.contains(STORAGE_MANAGER_ID)) storageManager.deserializeNBT(tag.getCompound(STORAGE_MANAGER_ID));
        if (tag.contains(PROGRESSION_STATES)) loadStates(tag.getCompound(PROGRESSION_STATES));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(CRAFT_REQUESTS_ID, craftRequests.serializeNBT());
        tag.put(STORAGE_MANAGER_ID, storageManager.serializeNBT());
        tag.put(PROGRESSION_STATES, saveStates());
    }

    private void loadStates(CompoundTag tag) {
        for (int slot = 0; slot < progressions.length; slot++) {
            if (tag.contains(String.valueOf(slot))) {
                var stateTag = tag.getCompound(String.valueOf(slot));
                ICraftingLink link = StorageHelper.loadCraftingLink(stateTag, this);
                progressions[slot] = new CraftingLinkState(link);
            }
        }
    }

    private CompoundTag saveStates() {
        CompoundTag tag = new CompoundTag();
        for (int slot = 0; slot < progressions.length; slot++) {
            var state = progressions[slot];
            if (state instanceof CraftingLinkState cls) {
                CompoundTag stateTag = new CompoundTag();
                cls.getLink().writeToNBT(stateTag);
                tag.put(String.valueOf(slot), stateTag);
            }
        }
        return tag;
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
        if (level == null || level.isClientSide) return TickRateModulation.IDLE;
        if (handleProgressions()) {
            setChanged();
        }
        updateActivityState();
        return currentTickRateModulation;
    }

    private boolean handleProgressions() {
        boolean changed = false;

        TickRateModulation tickRateModulation = TickRateModulation.IDLE;
        for (int slot = 0; slot < progressions.length; slot++) {
            ProgressionState state = progressions[slot];
            ProgressionState result = handleProgression(slot);
            if (state != result) {
                changed = true;
            }
            TickRateModulation resultTickRateModulation = result.getTickRateModulation();
            if (resultTickRateModulation.ordinal() > tickRateModulation.ordinal()) {
                tickRateModulation = resultTickRateModulation;
            }

            progressions[slot] = result;
        }
        currentTickRateModulation = tickRateModulation;
        return changed;
    }

    private ProgressionState handleProgression(int slot) {
        ProgressionState state = progressions[slot];
        progressions[slot] = state.handle(this, slot);
        if (progressions[slot] != ProgressionState.IDLE_STATE && progressions[slot] != state) {
            return handleProgression(slot);
        }

        return progressions[slot];
    }

    private void updateActivityState() {
        for (ProgressionState progression : progressions) {
            if (progression instanceof ExportSlotState || progression instanceof CraftingLinkState) {
                changeActivityState(true);
                return;
            }
        }

        changeActivityState(false);
    }

    private IManagedGridNode createMainNode() {
        return GridHelper
            .createManagedNode(this, BlockEntityNodeListener.INSTANCE)
            .addService(IStorageWatcherNode.class, storageManager)
            .addService(ICraftingRequester.class, this)
            .addService(IGridTickable.class, this)
            .setVisualRepresentation(Blocks.MAINTAINER.get())
            .setInWorldNode(true)
            .setTagName("proxy")
            .setIdlePowerUsage(Config.COMMON.maintainerIdleEnergy.get())
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
        return Arrays
            .stream(progressions)
            .filter(state -> state instanceof CraftingLinkState)
            .map(state -> ((CraftingLinkState) state).getLink())
            .collect(ImmutableSet.toImmutableSet());
    }

    public IGrid getMainNodeGrid() {
        IGrid grid = getMainNode().getGrid();
        Objects.requireNonNull(grid, "MaintainerEntity was not fully initialized - Grid is null");
        return grid;
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        for (int slot = 0; slot < progressions.length; slot++) {
            ProgressionState state = progressions[slot];
            if (state instanceof CraftingLinkState cls && cls.getLink() == link) {
                getStorageManager().get(slot).update(what, amount);
                return amount;
            }
        }
        throw new IllegalStateException("No CraftingLinkState found");
    }

    @Override
    public void jobStateChange(ICraftingLink link) {}
}

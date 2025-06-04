package com.almostreliable.lazierae.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.almostreliable.lazierae.blockentity.CrystalInjectorBlockEntity;
import com.almostreliable.lazierae.blockentity.MachineState;
import com.almostreliable.lazierae.registry.LazierMenus;
import com.almostreliable.lib.menu.PlayerInventoryHost;
import com.almostreliable.lib.menu.ProgressProvider;
import com.almostreliable.lib.menu.SynchronizedContainerMenu;
import com.almostreliable.lib.menu.network.ClientAction;
import com.almostreliable.lib.menu.network.synchronizer.handler.EnumDataHandler;
import com.almostreliable.lib.menu.network.synchronizer.handler.IntegerDataHandler;

import appeng.core.definitions.AEItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CrystalInjectorMenu extends SynchronizedContainerMenu<CrystalInjectorBlockEntity>
    implements PlayerInventoryHost, ProgressProvider, ClientAction.Receiver {

    private int recipeTime;
    private int recipeProgress;
    private MachineState currentState = MachineState.OK;

    public CrystalInjectorMenu(int wid, Inventory inventory, CrystalInjectorBlockEntity blockEntity) {
        super(LazierMenus.INJECTOR.get(), wid, inventory, blockEntity);

        addSlot(new ContainerFilteredSlot(blockEntity.getUpgrades(), 0, 8, 54));
        addSlot(new ContainerFilteredSlot(blockEntity.getInventory(), 0, 80, 35));
        addPlayerInventorySlots(this::addSlot, inventory);

        setupSyncing();
    }

    @Override
    public void onReceiveAction(ServerPlayer player, CompoundTag nbt) {
        blockEntity.getSideConfiguration().deserialize(nbt);
    }

    private void setupSyncing() {
        sync.addDataHandler(new IntegerDataHandler(blockEntity::getRecipeTime, v -> recipeTime = v));
        sync.addDataHandler(new IntegerDataHandler(blockEntity::getRecipeProgress, v -> recipeProgress = v));
        sync.addDataHandler(new EnumDataHandler<>(blockEntity::getCurrentState, v -> currentState = v, MachineState.values()));
        sync.addDataHandler(blockEntity.getSideConfiguration());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (!slot.hasItem()) return stack;

        ItemStack slotStack = slot.getItem();
        stack = slotStack.copy();

        if (index >= 0 && index <= 1) {
            // from machine/upgrade to player
            if (!moveItemStackTo(slotStack, 2, 38, false)) {
                return ItemStack.EMPTY;
            }
        } else if (AEItems.SPEED_CARD.isSameAs(slotStack)) {
            // from player to upgrade slot
            if (!moveItemStackTo(slotStack, 0, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // from player to machine
            if (!moveItemStackTo(slotStack, 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.set(slotStack);
            slot.setChanged();
        }

        if (slotStack.getCount() == stack.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, slotStack);

        return stack;
    }

    @Override
    public int getHeightOffset() {
        return 84;
    }

    private boolean moveItemStackTo(ItemStack stack, int index, boolean reverseDirection) {
        return moveItemStackTo(stack, index, index + 1, reverseDirection);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getProgress() {
        return recipeProgress;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getMaxProgress() {
        return recipeTime;
    }

    @OnlyIn(Dist.CLIENT)
    public MachineState getCurrentState() {
        return currentState;
    }
}

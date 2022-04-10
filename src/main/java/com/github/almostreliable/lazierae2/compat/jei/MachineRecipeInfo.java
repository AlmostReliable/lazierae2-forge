package com.github.almostreliable.lazierae2.compat.jei;

import com.github.almostreliable.lazierae2.machine.MachineContainer;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation taken from JEIs
 * BasicRecipeTransferInfo.
 */
public class MachineRecipeInfo implements IRecipeTransferInfo<MachineContainer> {

    private final ResourceLocation recipeCategoryUid;
    private final int recipeSlotStart;
    private final int recipeSlotCount;
    private final int inventorySlotStart;
    private final int inventorySlotCount;

    MachineRecipeInfo(
        ResourceLocation recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart,
        int inventorySlotCount
    ) {
        this.recipeCategoryUid = recipeCategoryUid;
        this.recipeSlotStart = recipeSlotStart;
        this.recipeSlotCount = recipeSlotCount;
        this.inventorySlotStart = inventorySlotStart;
        this.inventorySlotCount = inventorySlotCount;
    }

    @Override
    public Class<MachineContainer> getContainerClass() {
        return MachineContainer.class;
    }

    @Override
    public ResourceLocation getRecipeCategoryUid() {
        return recipeCategoryUid;
    }

    @Override
    public boolean canHandle(MachineContainer container) {
        return container.tile.getMachineType().getId().equals(recipeCategoryUid.getPath());
    }

    @Override
    public List<Slot> getRecipeSlots(MachineContainer container) {
        List<Slot> slots = new ArrayList<>();
        for (int i = recipeSlotStart; i < recipeSlotStart + recipeSlotCount; i++) {
            Slot slot = container.getSlot(i);
            slots.add(slot);
        }
        return slots;
    }

    @Override
    public List<Slot> getInventorySlots(MachineContainer container) {
        List<Slot> slots = new ArrayList<>();
        for (int i = inventorySlotStart; i < inventorySlotStart + inventorySlotCount; i++) {
            Slot slot = container.getSlot(i);
            slots.add(slot);
        }
        return slots;
    }
}

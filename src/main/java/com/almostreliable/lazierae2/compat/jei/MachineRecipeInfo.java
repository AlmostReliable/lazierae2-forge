package com.almostreliable.lazierae2.compat.jei;

import com.almostreliable.lazierae2.machine.MachineContainer;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation taken from JEIs
 * BasicRecipeTransferInfo.
 */
public class MachineRecipeInfo implements IRecipeTransferInfo<MachineContainer, MachineRecipe> {

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
    public Class<MachineRecipe> getRecipeClass() {
        return MachineRecipe.class;
    }

    @Override
    public ResourceLocation getRecipeCategoryUid() {
        return recipeCategoryUid;
    }

    @Override
    public boolean canHandle(MachineContainer container, MachineRecipe recipe) {
        return container.entity.getMachineType().getId().equals(recipeCategoryUid.getPath());
    }

    @Override
    public List<Slot> getRecipeSlots(MachineContainer container, MachineRecipe recipe) {
        List<Slot> slots = new ArrayList<>();
        for (var i = recipeSlotStart; i < recipeSlotStart + recipeSlotCount; i++) {
            var slot = container.getSlot(i);
            slots.add(slot);
        }
        return slots;
    }

    @Override
    public List<Slot> getInventorySlots(MachineContainer container, MachineRecipe recipe) {
        List<Slot> slots = new ArrayList<>();
        for (var i = inventorySlotStart; i < inventorySlotStart + inventorySlotCount; i++) {
            var slot = container.getSlot(i);
            slots.add(slot);
        }
        return slots;
    }
}

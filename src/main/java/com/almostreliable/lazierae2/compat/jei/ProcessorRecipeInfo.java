package com.almostreliable.lazierae2.compat.jei;

import com.almostreliable.lazierae2.content.processor.ProcessorMenu;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation taken from JEIs
 * BasicRecipeTransferInfo.
 */
public class ProcessorRecipeInfo<R extends ProcessorRecipe> implements IRecipeTransferInfo<ProcessorMenu, R> {

    private final RecipeType<R> recipeType;
    private final int recipeSlotStart;
    private final int recipeSlotCount;
    private final int inventorySlotStart;
    private final int inventorySlotCount;

    ProcessorRecipeInfo(
        RecipeType<R> recipeType, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart,
        int inventorySlotCount
    ) {
        this.recipeType = recipeType;
        this.recipeSlotStart = recipeSlotStart;
        this.recipeSlotCount = recipeSlotCount;
        this.inventorySlotStart = inventorySlotStart;
        this.inventorySlotCount = inventorySlotCount;
    }

    @Override
    public Class<ProcessorMenu> getContainerClass() {
        return ProcessorMenu.class;
    }

    @Override
    public RecipeType<R> getRecipeType() {
        return recipeType;
    }

    @Override
    public boolean canHandle(ProcessorMenu container, ProcessorRecipe recipe) {
        return container.entity.getProcessorType().getId().equals(recipeType.getUid().getPath());
    }

    @Override
    public List<Slot> getRecipeSlots(ProcessorMenu container, ProcessorRecipe recipe) {
        List<Slot> slots = new ArrayList<>();
        for (var i = recipeSlotStart; i < recipeSlotStart + recipeSlotCount; i++) {
            var slot = container.getSlot(i);
            slots.add(slot);
        }
        return slots;
    }

    @Override
    public List<Slot> getInventorySlots(ProcessorMenu container, ProcessorRecipe recipe) {
        List<Slot> slots = new ArrayList<>();
        for (var i = inventorySlotStart; i < inventorySlotStart + inventorySlotCount; i++) {
            var slot = container.getSlot(i);
            slots.add(slot);
        }
        return slots;
    }

    @SuppressWarnings({"removal", "unchecked"})
    @Override
    public Class<R> getRecipeClass() {
        return (Class<R>) ProcessorRecipe.class;
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getRecipeCategoryUid() {
        return recipeType.getUid();
    }
}

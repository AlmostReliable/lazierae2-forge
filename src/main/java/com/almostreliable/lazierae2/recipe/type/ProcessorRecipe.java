package com.almostreliable.lazierae2.recipe.type;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.util.RecipeUtil;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public abstract class ProcessorRecipe implements Recipe<Container> {

    private final ProcessorType processorType;
    private final ResourceLocation id;
    NonNullList<Ingredient> inputs = NonNullList.create();
    private int processTime;
    private int energyCost;
    private ItemStack output;

    ProcessorRecipe(
        ResourceLocation id, ProcessorType processorType
    ) {
        this.id = id;
        this.processorType = processorType;
    }

    @Override
    public ItemStack assemble(Container inv) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return processorType.getRecipeSerializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return processorType;
    }

    public int getProcessTime() {
        return processTime;
    }

    public void setProcessTime(int processTime) {
        this.processTime = processTime;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public void setEnergyCost(int energyCost) {
        this.energyCost = energyCost;
    }

    public NonNullList<Ingredient> getInputs() {
        return inputs;
    }

    public void setInputs(NonNullList<Ingredient> inputs) {
        this.inputs = inputs;
    }

    public void setOutput(ItemStack output) {
        this.output = output;
    }

    public static class ProcessorRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ProcessorRecipe> {

        private final ProcessorType processorType;

        public ProcessorRecipeSerializer(ProcessorType processorType) {
            this.processorType = processorType;
        }

        @Override
        public ProcessorRecipe fromJson(ResourceLocation id, JsonObject json) {
            var recipe = processorType.getRecipeFactory().apply(id, processorType);
            return RecipeUtil.fromJSON(json, recipe);
        }

        @Nullable
        @Override
        public ProcessorRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            var recipe = processorType.getRecipeFactory().apply(id, processorType);
            return RecipeUtil.fromNetwork(buffer, recipe);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ProcessorRecipe recipe) {
            RecipeUtil.toNetwork(buffer, recipe);
        }
    }
}

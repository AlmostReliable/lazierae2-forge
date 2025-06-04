package com.almostreliable.lazierae.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import com.almostreliable.lazierae.registry.LazierRecipes;
import com.almostreliable.lazierae.registry.ProcessingMachineType;
import com.almostreliable.lib.item.IngredientStack;
import com.almostreliable.lib.util.filter.BlockPredicate;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProcessingMachineRecipe implements Recipe<Container> {

    public static final int DEFAULT_TIME = 60;
    public static final int DEFAULT_ENERGY = 1_000;

    private final ResourceLocation id;
    private final TriInput inputs;
    private final ItemStack output;
    private final int time;
    private final int energy;

    public ProcessingMachineRecipe(ResourceLocation id, TriInput inputs, ItemStack output, int time, int energy) {
        this.id = id;
        this.inputs = inputs;
        this.output = output;
        this.time = time;
        this.energy = energy;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container) {
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
        return LazierRecipes.ETCHER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return LazierRecipes.ETCHER;
    }

    public TriInput getInputs() {
        return inputs;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getTime() {
        return time;
    }

    public int getEnergy() {
        return energy;
    }

    public static boolean isInput(Level level, ProcessingMachineType processingMachineType, ItemStack stack) {
        return isTopInput(level, processingMachineType, stack) ||
            isMiddleInput(level, processingMachineType, stack) ||
            isBottomInput(level, processingMachineType, stack);
    }

    public static boolean isTopInput(Level level, ProcessingMachineType processingMachineType, ItemStack stack) {
        return isInput(level, processingMachineType, stack, "top");
    }

    public static boolean isMiddleInput(Level level, ProcessingMachineType processingMachineType, ItemStack stack) {
        return isInput(level, processingMachineType, stack, "middle");
    }

    public static boolean isBottomInput(Level level, ProcessingMachineType processingMachineType, ItemStack stack) {
        return isInput(level, processingMachineType, stack, "bottom");
    }

    private static boolean isInput(Level level, ProcessingMachineType processingMachineType, ItemStack stack, String slot) {
        if (stack.isEmpty()) return false;

        var recipes = level.getRecipeManager().getAllRecipesFor(processingMachineType.getRecipeEntry());
        for (ProcessingMachineRecipe recipe : recipes) {
            switch (slot) {
                case "top":
                    if (recipe.getInputs().isTop(stack)) return true;
                    break;
                case "middle":
                    if (recipe.getInputs().isMiddle(stack)) return true;
                    break;
                case "bottom":
                    if (recipe.getInputs().isBottom(stack)) return true;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + slot);
            }
        }

        return false;
    }

    @Nullable
    public static ProcessingMachineRecipe findRecipe(Level level, List<ItemStack> inputs) {
        var recipes = level.getRecipeManager().getAllRecipesFor(LazierRecipes.ETCHER);

        for (ProcessingMachineRecipe recipe : recipes) {
            if (recipe.getInputs().size() != inputs.size()) continue;

            for (int i = 0; i < inputs.size(); i++) {
                IngredientStack recipeInput = recipe.getInputs().get(i);
                ItemStack input = inputs.get(i);
                if (!recipeInput.test(input)) return null;
            }

            return recipe;
        }

        return null;
    }

    public static class Serializer implements RecipeSerializer<ProcessingMachineRecipe> {

        @Override
        public ProcessingMachineRecipe fromJson(ResourceLocation id, JsonObject json) {
            NonNullList<IngredientStack> inputs = NonNullList.withSize(3, IngredientStack.of(ItemStack.EMPTY));
            JsonObject inputJson = GsonHelper.getAsJsonObject(json, "inputs");
            // TODO: make these optional
            inputs.set(0, IngredientStack.SERIALIZER.fromJson(inputJson.get("top")));
            inputs.set(1, IngredientStack.SERIALIZER.fromJson(inputJson.get("middle")));
            inputs.set(2, IngredientStack.SERIALIZER.fromJson(inputJson.get("bottom")));

            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            Preconditions.checkArgument(!output.isEmpty(), "output must not be empty");

            int time = GsonHelper.getAsInt(json, "time", DEFAULT_TIME);
            Preconditions.checkArgument(time >= 0, "time must be non-negative");
            int energy = GsonHelper.getAsInt(json, "energy", DEFAULT_ENERGY);
            Preconditions.checkArgument(energy >= 0, "energy must be non-negative");

            return new ProcessingMachineRecipe(id, inputs, output, time, energy);
        }

        @Nullable
        @Override
        public ProcessingMachineRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            IngredientStack input = IngredientStack.SERIALIZER.fromNetwork(buffer);
            BlockPredicate inputBlock = BlockPredicate.SERIALIZER.fromNetwork(buffer);
            BlockPredicate outputBlock = BlockPredicate.SERIALIZER.fromNetwork(buffer);
            int time = buffer.readInt();
            int energy = buffer.readInt();

            return new ProcessingMachineRecipe(id, input, inputBlock, outputBlock, time, energy);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ProcessingMachineRecipe recipe) {
            IngredientStack.SERIALIZER.toNetwork(buffer, recipe.inputs);
            BlockPredicate.SERIALIZER.toNetwork(buffer, recipe.inputBlock);
            BlockPredicate.SERIALIZER.toNetwork(buffer, recipe.outputBlock);
            buffer.writeInt(recipe.time);
            buffer.writeInt(recipe.energy);
        }
    }
}

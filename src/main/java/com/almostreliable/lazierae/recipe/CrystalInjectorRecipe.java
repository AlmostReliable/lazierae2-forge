package com.almostreliable.lazierae.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import com.almostreliable.lazierae.registry.LazierRecipes;
import com.almostreliable.lib.item.IngredientStack;
import com.almostreliable.lib.util.filter.BlockPredicate;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CrystalInjectorRecipe implements Recipe<Container> {

    public static final int DEFAULT_TIME = 60;
    public static final int DEFAULT_ENERGY = 1_000;
    private static final List<Ingredient> INPUTS = new ArrayList<>();

    private final ResourceLocation id;
    private final IngredientStack input;
    private final BlockPredicate inputBlock;
    private final BlockPredicate outputBlock;
    private final int time;
    private final int energy;

    public CrystalInjectorRecipe(
        ResourceLocation id, IngredientStack input, BlockPredicate inputBlock, BlockPredicate outputBlock, int time, int energy
    ) {
        this.id = id;
        this.input = input;
        this.inputBlock = inputBlock;
        this.outputBlock = outputBlock;
        this.time = time;
        this.energy = energy;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
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

    public IngredientStack getInput() {
        return input;
    }

    public BlockPredicate getInputBlock() {
        return inputBlock;
    }

    public BlockPredicate getOutputBlock() {
        return outputBlock;
    }

    public int getTime() {
        return time;
    }

    public int getEnergy() {
        return energy;
    }

    public static boolean isInput(ItemStack stack) {
        if (stack.isEmpty()) return false;

        for (Ingredient ingredient : INPUTS) {
            if (ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }

    public static void clearCaches() {
        // replace with recipe manager iteration
        INPUTS.clear();
    }

    @Nullable
    public static CrystalInjectorRecipe findRecipe(Level level, ItemStack stack, BlockPos pos) {
        var recipes = level.getRecipeManager().getAllRecipesFor(LazierRecipes.INJECTOR);

        for (CrystalInjectorRecipe recipe : recipes) {
            if (recipe.getInput().test(stack) && testBlock(level, pos, recipe.getInputBlock())) {
                return recipe;
            }
        }

        return null;
    }

    public static boolean testBlock(Level level, BlockPos pos, BlockPredicate block) {
        if (level.getBlockEntity(pos) != null) {
            return block.test(level, pos);
        }
        return block.test(level.getBlockState(pos));
    }

    public static class Serializer implements RecipeSerializer<CrystalInjectorRecipe> {

        @Override
        public CrystalInjectorRecipe fromJson(ResourceLocation id, JsonObject json) {
            IngredientStack input = IngredientStack.SERIALIZER.fromJson(json.get("input"));
            Preconditions.checkArgument(!input.isEmpty(), "input must not be empty");

            BlockPredicate inputBlock = BlockPredicate.SERIALIZER.fromJson(json.get("block_input"));
            BlockPredicate outputBlock = BlockPredicate.SERIALIZER.fromJson(json.get("block_output"));
            Preconditions.checkArgument(outputBlock.decomposeBlockStates().size() == 1, "output block must be a single block");

            int time = GsonHelper.getAsInt(json, "time", DEFAULT_TIME);
            Preconditions.checkArgument(time >= 0, "time must be non-negative");
            int energy = GsonHelper.getAsInt(json, "energy", DEFAULT_ENERGY);
            Preconditions.checkArgument(energy >= 0, "energy must be non-negative");

            INPUTS.add(input.getIngredient());

            return new CrystalInjectorRecipe(id, input, inputBlock, outputBlock, time, energy);
        }

        @Nullable
        @Override
        public CrystalInjectorRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            IngredientStack input = IngredientStack.SERIALIZER.fromNetwork(buffer);
            BlockPredicate inputBlock = BlockPredicate.SERIALIZER.fromNetwork(buffer);
            BlockPredicate outputBlock = BlockPredicate.SERIALIZER.fromNetwork(buffer);
            int time = buffer.readInt();
            int energy = buffer.readInt();

            return new CrystalInjectorRecipe(id, input, inputBlock, outputBlock, time, energy);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CrystalInjectorRecipe recipe) {
            IngredientStack.SERIALIZER.toNetwork(buffer, recipe.input);
            BlockPredicate.SERIALIZER.toNetwork(buffer, recipe.inputBlock);
            BlockPredicate.SERIALIZER.toNetwork(buffer, recipe.outputBlock);
            buffer.writeInt(recipe.time);
            buffer.writeInt(recipe.energy);
        }
    }
}

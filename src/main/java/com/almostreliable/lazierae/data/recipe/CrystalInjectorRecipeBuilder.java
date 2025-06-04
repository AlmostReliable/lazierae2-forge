package com.almostreliable.lazierae.data.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.almostreliable.lazierae.LazierAE2;
import com.almostreliable.lazierae.recipe.CrystalInjectorRecipe;
import com.almostreliable.lazierae.registry.LazierRecipes;
import com.almostreliable.lib.datagen.recipe.ConditionalFinishedRecipe;
import com.almostreliable.lib.datagen.recipe.RecipeComponent;
import com.almostreliable.lib.util.filter.BlockPredicate;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class CrystalInjectorRecipeBuilder {

    private static final Set<String> USED_IDS = new HashSet<>();

    private final RecipeComponent input;
    private final BlockPredicate inputBlock;
    private final BlockPredicate outputBlock;

    private int time;
    private int energy;

    private CrystalInjectorRecipeBuilder(RecipeComponent input, BlockPredicate inputBlock, BlockPredicate outputBlock) {
        this.input = input;
        this.inputBlock = inputBlock;
        this.outputBlock = outputBlock;
        this.time = CrystalInjectorRecipe.DEFAULT_TIME;
        this.energy = CrystalInjectorRecipe.DEFAULT_ENERGY;
    }

    public static CrystalInjectorRecipeBuilder create(
        RecipeComponent input, BlockPredicate block, BlockPredicate output
    ) {
        return new CrystalInjectorRecipeBuilder(input, block, output);
    }

    public CrystalInjectorRecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public CrystalInjectorRecipeBuilder energy(int energy) {
        this.energy = energy;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer, String id) {
        Preconditions.checkArgument(!id.isEmpty(), "id must not be empty");
        Preconditions.checkArgument(!USED_IDS.contains(id), "id must be unique");
        Preconditions.checkArgument(outputBlock.decomposeBlockStates().size() == 1, "output block must be a single block");
        Preconditions.checkArgument(time >= 0, "time must be non-negative");
        Preconditions.checkArgument(energy >= 0, "energy must be non-negative");

        USED_IDS.add(id);
        consumer.accept(new Result(LazierAE2.MANAGER.getRL("crystal_injector/" + id)));
    }

    public final class Result implements ConditionalFinishedRecipe {

        private final ResourceLocation id;

        private Result(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input", input.toJson());
            json.add("block_input", BlockPredicate.SERIALIZER.toJson(inputBlock));
            json.add("block_output", BlockPredicate.SERIALIZER.toJson(outputBlock));
            if (time != CrystalInjectorRecipe.DEFAULT_TIME) json.addProperty("time", time);
            if (energy != CrystalInjectorRecipe.DEFAULT_ENERGY) json.addProperty("energy", energy);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return LazierRecipes.ETCHER.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }

        @Override
        public Set<String> getOptionalModIds() {
            return input.getModIds();
        }
    }
}

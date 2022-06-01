package com.almostreliable.lazierae2.compat.kubejs;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.IngredientWithCount;
import com.almostreliable.lazierae2.recipe.builder.FinishedProcessorRecipe;
import com.almostreliable.lazierae2.recipe.builder.ProcessorRecipeBuilder;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import static com.almostreliable.lazierae2.core.Constants.Recipe.*;

public class ProcessorRecipeJS extends RecipeJS {

    private final ProcessorType processorType;

    ProcessorRecipeJS(ProcessorType processorType) {
        this.processorType = processorType;
    }

    @Override
    public void create(ListJS listJS) {
        if (listJS.size() < 2) {
            throw new IllegalArgumentException("Missing input or output for recipe type " + processorType.getId());
        }

        var output = ItemStackJS.of(listJS.get(0));
        outputItems.add(output);

        if (processorType.getInputSlots() == 1) {
            inputItems.add(IngredientJS.of(listJS.get(1)));
        } else {
            for (var o : ListJS.orSelf(listJS.get(1))) {
                inputItems.add(IngredientJS.of(o));
            }
        }
    }

    @Override
    public void deserialize() {
        outputItems.add(parseResultItem(json.get(OUTPUT)));
        inputItems.addAll(parseIngredientItemList(json.get(INPUT)));
    }

    @Override
    public void serialize() {
        if (serializeOutputs || serializeInputs) {
            var builder = getBuilder(outputItems.get(0).getItemStack());
            builder.processingTime(GsonHelper.getAsInt(json, PROCESS_TIME, processorType.getBaseProcessTime()));
            builder.energyCost(GsonHelper.getAsInt(json, ENERGY_COST, processorType.getBaseEnergyCost()));
            inputItems.stream().map(ingredientJS -> {
                var ingredient = ingredientJS.createVanillaIngredient();
                var count = ingredientJS.getCount();
                return new IngredientWithCount(ingredient, count);
            }).forEach(builder::input);
            var recipe = builder.build(getOrCreateId());
            var finishedRecipe = new FinishedProcessorRecipe(recipe);
            var newRecipe = new JsonObject();
            finishedRecipe.serializeRecipeData(newRecipe);
            json = newRecipe;
        }
    }

    @SuppressWarnings("unused")
    public ProcessorRecipeJS processingTime(int processingTime) {
        json.addProperty(PROCESS_TIME, processingTime);
        save();
        return this;
    }

    @SuppressWarnings("unused")
    public ProcessorRecipeJS energyCost(int energyCost) {
        json.addProperty(ENERGY_COST, energyCost);
        save();
        return this;
    }

    private ProcessorRecipeBuilder getBuilder(ItemStack itemStack) {
        return switch (processorType) {
            case AGGREGATOR -> ProcessorRecipeBuilder.aggregator(itemStack.getItem(), itemStack.getCount());
            case ETCHER -> ProcessorRecipeBuilder.etcher(itemStack.getItem(), itemStack.getCount());
            case GRINDER -> ProcessorRecipeBuilder.grinder(itemStack.getItem(), itemStack.getCount());
            case INFUSER -> ProcessorRecipeBuilder.infuser(itemStack.getItem(), itemStack.getCount());
        };
    }
}

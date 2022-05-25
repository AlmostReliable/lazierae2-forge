package com.almostreliable.lazierae2.compat.kubejs;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.builder.FinishedProcessorRecipe;
import com.almostreliable.lazierae2.recipe.builder.ProcessorRecipeBuilder;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import static com.almostreliable.lazierae2.core.Constants.Recipe.*;

public class ProcessorRecipeJS extends RecipeJS {
    private final ProcessorType type;

    public ProcessorRecipeJS(ProcessorType type) {
        this.type = type;
    }

    @Override
    public void create(ListJS listJS) {
        if (listJS.size() < 2) {
            throw new IllegalArgumentException("Missing input or output for recipe type " + type.getId());
        }

        ItemStackJS output = ItemStackJS.of(listJS.get(0));
        this.outputItems.add(output);

        if (type.getInputSlots() == 1) {
            this.inputItems.add(IngredientJS.of(listJS.get(1)));
        } else {
            for (Object o : ListJS.orSelf(listJS.get(1))) {
                this.inputItems.add(IngredientJS.of(o));
            }
        }
    }

    @Override
    public void deserialize() {
        this.outputItems.add(this.parseResultItem(this.json.get(OUTPUT)));
        this.inputItems.addAll(this.parseIngredientItemList(this.json.get(INPUT)));
    }

    @Override
    public void serialize() {
        if (serializeOutputs || serializeInputs) {
            ProcessorRecipeBuilder builder = getBuilder(this.outputItems.get(0).getItemStack());
            builder.processingTime(GsonHelper.getAsInt(json, PROCESS_TIME, type.getBaseProcessTime()));
            builder.energyCost(GsonHelper.getAsInt(json, ENERGY_COST, type.getBaseEnergyCost()));
            this.inputItems.stream().map(IngredientJS::createVanillaIngredient).forEach(builder::input);
            ProcessorRecipe recipe = builder.build(getOrCreateId());
            FinishedProcessorRecipe finishedRecipe = new FinishedProcessorRecipe(recipe);
            JsonObject newRecipe = new JsonObject();
            finishedRecipe.serializeRecipeData(newRecipe);
            this.json = newRecipe;
        }
    }

    public ProcessorRecipeJS processingTime(int processingTime) {
        json.addProperty(PROCESS_TIME, processingTime);
        save();
        return this;
    }

    public ProcessorRecipeJS energyCost(int energyCost) {
        json.addProperty(ENERGY_COST, energyCost);
        save();
        return this;
    }

    private ProcessorRecipeBuilder getBuilder(ItemStack itemStack) {
        return switch (type) {
            case AGGREGATOR -> ProcessorRecipeBuilder.aggregator(itemStack.getItem(), itemStack.getCount());
            case ETCHER -> ProcessorRecipeBuilder.etcher(itemStack.getItem(), itemStack.getCount());
            case GRINDER -> ProcessorRecipeBuilder.grinder(itemStack.getItem(), itemStack.getCount());
            case INFUSER -> ProcessorRecipeBuilder.infuser(itemStack.getItem(), itemStack.getCount());
        };
    }
}

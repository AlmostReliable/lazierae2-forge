package com.almostreliable.lazierae2.recipe.type;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.property.IRecipeInputProvider;
import com.almostreliable.lazierae2.recipe.property.RecipeInputIngred;
import com.almostreliable.lazierae2.recipe.property.RecipeOutputStack;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;

import static com.almostreliable.lazierae2.core.Constants.Recipe.*;

public class ProcessorRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ProcessorRecipe> {

    private final ProcessorType processorType;

    public ProcessorRecipeSerializer(ProcessorType processorType) {
        this.processorType = processorType;
    }

    @Override
    public ProcessorRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        var output = new RecipeOutputStack(ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, OUTPUT)));
        NonNullList<IRecipeInputProvider> inputs = NonNullList.create();
        if (processorType.getInputSlots() == 1) {
            inputs.add(RecipeInputIngred.fromJson(GsonHelper.getAsJsonObject(json, INPUT)));
        } else {
            GsonHelper.getAsJsonArray(json, INPUT)
                .forEach(jsonInput -> inputs.add(RecipeInputIngred.fromJson(jsonInput.getAsJsonObject())));
        }
        var processTime = GsonHelper.getAsInt(json, PROCESS_TIME, processorType.getBaseProcessTime());
        var energyCost = GsonHelper.getAsInt(json, ENERGY_COST, processorType.getBaseEnergyCost());
        return processorType.getRecipeFactory()
            .create(recipeId, processorType, List.of(), output, inputs, processTime, energyCost);
    }

    @Nullable
    @Override
    public ProcessorRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        var output = new RecipeOutputStack(buffer.readItem());
        NonNullList<IRecipeInputProvider> inputs = NonNullList.create();
        var size = processorType.getInputSlots() == 1 ? 1 : buffer.readVarInt();
        for (var i = 0; i < size; i++) {
            inputs.add(RecipeInputIngred.fromNetwork(buffer));
        }
        var processTime = buffer.readInt();
        var energyCost = buffer.readInt();
        return processorType.getRecipeFactory()
            .create(recipeId, processorType, List.of(), output, inputs, processTime, energyCost);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ProcessorRecipe recipe) {
        buffer.writeItem(recipe.getResultItem());
        if (processorType.getInputSlots() == 3) buffer.writeVarInt(recipe.getInputs().size());
        recipe.getInputs().forEach(input -> input.toNetwork(buffer));
        buffer.writeInt(recipe.getProcessTime());
        buffer.writeInt(recipe.getEnergyCost());
    }
}

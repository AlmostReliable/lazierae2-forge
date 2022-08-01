package com.almostreliable.lazierae2.compat.crafttweaker;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.IngredientWithCount;
import com.almostreliable.lazierae2.recipe.builder.ProcessorRecipeBuilder;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType.Name;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;

@SuppressWarnings("unused")
@ZenRegister
@Name("mods." + MOD_ID + ".Grinder")
public class GrinderManager implements ProcessorRecipeManager {

    public static final GrinderManager INSTANCE = new GrinderManager();

    @Override
    public RecipeBuilderWrapper createRecipeBuilder(ResourceLocation id, IItemStack output) {
        return new RecipeBuilderWrapper(this, ProcessorType.GRINDER, id, output);
    }

    @Override
    public ProcessorRecipe createRecipe(
        ResourceLocation id, ItemStack output, int amount, IngredientWithCount[] inputs, int processTime, int energyCost
    ) {
        return ProcessorRecipeBuilder.grinder(output.getItem(), amount)
            .input(inputs)
            .processingTime(processTime)
            .energyCost(energyCost)
            .build(id);
    }

    @Override
    public RecipeType<ProcessorRecipe> getRecipeType() {
        return ProcessorType.GRINDER;
    }
}

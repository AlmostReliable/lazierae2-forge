package com.almostreliable.lazierae2.compat.crafttweaker;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.builder.ProcessorRecipeBuilder;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import static com.almostreliable.lazierae2.BuildConfig.MOD_ID;

@SuppressWarnings("unused")
@ZenRegister
@Name("mods." + MOD_ID + ".RecipeBuilderWrapper")
public class RecipeBuilderWrapper {

    private final IRecipeManager<? super ProcessorRecipe> manager;
    private final ResourceLocation id;
    private final ProcessorRecipeBuilder builder;

    RecipeBuilderWrapper(
        IRecipeManager<? super ProcessorRecipe> manager, ProcessorType type, ResourceLocation id, IItemStack output
    ) {
        this.manager = manager;
        this.id = id;
        builder = switch (type) {
            case AGGREGATOR -> ProcessorRecipeBuilder.aggregator(output.getInternal().getItem(), output.getAmount());
            case ETCHER -> ProcessorRecipeBuilder.etcher(output.getInternal().getItem(), output.getAmount());
            case GRINDER -> ProcessorRecipeBuilder.grinder(output.getInternal().getItem(), output.getAmount());
            case INFUSER -> ProcessorRecipeBuilder.infuser(output.getInternal().getItem(), output.getAmount());
        };
    }

    @Method
    public RecipeBuilderWrapper input(IIngredientWithAmount input) {
        builder.input(input.getIngredient().asVanillaIngredient(), input.getAmount());
        return this;
    }

    @Method
    public RecipeBuilderWrapper input(IIngredient input, int count) {
        builder.input(input.asVanillaIngredient(), count);
        return this;
    }

    @Method
    public RecipeBuilderWrapper processingTime(int ticks) {
        builder.processingTime(ticks);
        return this;
    }

    @Method
    public RecipeBuilderWrapper energyCost(int energy) {
        builder.energyCost(energy);
        return this;
    }

    @Method
    public void build() {
        var recipe = builder.build(id);
        CraftTweakerAPI.apply(new ActionAddRecipe<>(manager, recipe, ""));
    }
}

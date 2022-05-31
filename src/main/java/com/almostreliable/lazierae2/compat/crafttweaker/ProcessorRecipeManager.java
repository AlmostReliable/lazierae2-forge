package com.almostreliable.lazierae2.compat.crafttweaker;

import com.almostreliable.lazierae2.recipe.IngredientWithCount;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;

@SuppressWarnings({"unused", "WeakerAccess"})
@ZenRegister
@Name("mods." + MOD_ID + ".ProcessorRecipeManager")
public interface ProcessorRecipeManager extends IRecipeManager<ProcessorRecipe> {

    @Method
    default void addRecipe(
        String name, IItemStack output, int processTime, int energyCost, IIngredientWithAmount... inputs
    ) {
        var id = new ResourceLocation("crafttweaker", fixRecipeName(name));
        var ingredients = new IngredientWithCount[inputs.length];
        for (var i = 0; i < inputs.length; i++) {
            ingredients[i] = new IngredientWithCount(inputs[i].getIngredient().asVanillaIngredient(),
                inputs[i].getAmount()
            );
        }
        var recipe = createRecipe(id, output.getInternal(), output.getAmount(), ingredients, processTime, energyCost);
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, recipe, ""));
    }

    @Method
    default RecipeBuilderWrapper builder(String name, IItemStack output) {
        var id = new ResourceLocation("crafttweaker", fixRecipeName(name));
        return createRecipeBuilder(id, output);
    }

    RecipeBuilderWrapper createRecipeBuilder(ResourceLocation id, IItemStack output);

    ProcessorRecipe createRecipe(
        ResourceLocation id, ItemStack output, int amount, IngredientWithCount[] inputs, int processTime, int energyCost
    );
}

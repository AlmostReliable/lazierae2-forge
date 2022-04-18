package com.almostreliable.lazierae2.compat.crafttweaker;

import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;

@SuppressWarnings({"unused", "WeakerAccess"})
@ZenRegister
@Name("mods." + MOD_ID + ".ProcessorRecipeManager")
public interface ProcessorRecipeManager extends IRecipeManager<ProcessorRecipe> {

    @Method
    default void addRecipe(
        String name, IItemStack output, int processTime, int energyCost, IItemStack... inputs
    ) {
        var id = new ResourceLocation("crafttweaker", fixRecipeName(name));
        var ingredients = new Ingredient[inputs.length];
        for (var i = 0; i < inputs.length; i++) {
            ingredients[i] = inputs[i].asVanillaIngredient();
        }
        var recipe = createRecipe(id, output.getInternal(), output.getAmount(), ingredients, processTime, energyCost);
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, recipe, ""));
    }

    ProcessorRecipe createRecipe(
        ResourceLocation id, ItemStack output, int amount, Ingredient[] ingredients, int processTime, int energyCost
    );
}

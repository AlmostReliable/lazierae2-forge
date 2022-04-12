package com.github.almostreliable.lazierae2.compat.crafttweaker;

import com.blamejared.crafttweaker.CraftTweaker;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.github.almostreliable.lazierae2.recipe.type.MachineRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import static com.github.almostreliable.lazierae2.core.Constants.MOD_ID;

@SuppressWarnings({"unused", "WeakerAccess"})
@ZenRegister
@Name("mods." + MOD_ID + ".MachineRecipeManager")
public interface MachineRecipeManager extends IRecipeManager {

    @Method
    default void addRecipe(
        String name, IItemStack output, IItemStack[] inputs, int processTime, int energyCost
    ) {
        ResourceLocation id = new ResourceLocation(CraftTweaker.MODID, fixRecipeName(name));
        Ingredient[] ingredients = new Ingredient[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            ingredients[i] = inputs[i].asVanillaIngredient();
        }
        MachineRecipe recipe = createRecipe(id,
            output.getInternal(),
            output.getAmount(),
            ingredients,
            processTime,
            energyCost
        );
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, ""));
    }

    @Method
    default void addRecipe(
        String name, IItemStack output, IItemStack input, int processTime, int energyCost
    ) {
        addRecipe(name, output, new IItemStack[]{input}, processTime, energyCost);
    }

    MachineRecipe createRecipe(
        ResourceLocation id, ItemStack output, int amount, Ingredient[] ingredients, int processTime, int energyCost
    );
}

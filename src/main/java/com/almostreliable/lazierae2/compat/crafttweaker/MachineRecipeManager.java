package com.almostreliable.lazierae2.compat.crafttweaker;

import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.blamejared.crafttweaker.CraftTweaker;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;

@SuppressWarnings({"unused", "WeakerAccess"})
@ZenRegister
@Name("mods." + MOD_ID + ".MachineRecipeManager")
public abstract class MachineRecipeManager implements IRecipeManager {

    @Method
    public void addRecipe(
        String name, IItemStack output, int processTime, int energyCost, IItemStack... inputs
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
    public RecipeBuilderWrapper builder(String name, IItemStack output) {
        ResourceLocation id = new ResourceLocation(CraftTweaker.MODID, fixRecipeName(name));
        return createRecipeBuilder(id, output);
    }

    protected abstract RecipeBuilderWrapper createRecipeBuilder(ResourceLocation id, IItemStack output);

    protected abstract MachineRecipe createRecipe(
        ResourceLocation id, ItemStack output, int amount, Ingredient[] ingredients, int processTime, int energyCost
    );
}

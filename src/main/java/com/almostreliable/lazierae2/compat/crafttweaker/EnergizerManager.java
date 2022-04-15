package com.almostreliable.lazierae2.compat.crafttweaker;

import com.almostreliable.lazierae2.machine.MachineType;
import com.almostreliable.lazierae2.recipe.builder.MachineRecipeBuilder;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Name;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;

@SuppressWarnings("unused")
@ZenRegister
@Name("mods." + MOD_ID + ".Energizer")
public class EnergizerManager extends MachineRecipeManager {

    public static final EnergizerManager INSTANCE = new EnergizerManager();

    @Override
    protected RecipeBuilderWrapper createRecipeBuilder(ResourceLocation id, IItemStack output) {
        return new RecipeBuilderWrapper(this, MachineType.ENERGIZER, id, output);
    }

    @Override
    public MachineRecipe createRecipe(
        ResourceLocation id, ItemStack output, int amount, Ingredient[] ingredients, int processTime, int energyCost
    ) {
        return MachineRecipeBuilder
            .energizer(output.getItem(), amount)
            .input(ingredients)
            .processingTime(processTime)
            .energyCost(energyCost)
            .build(id);
    }

    @Override
    public IRecipeType<MachineRecipe> getRecipeType() {
        return MachineType.ENERGIZER;
    }
}

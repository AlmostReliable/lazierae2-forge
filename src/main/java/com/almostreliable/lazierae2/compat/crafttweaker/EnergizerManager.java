package com.almostreliable.lazierae2.compat.crafttweaker;

import com.almostreliable.lazierae2.content.machine.MachineType;
import com.almostreliable.lazierae2.recipe.builder.MachineRecipeBuilder;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType.Name;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;

@SuppressWarnings("unused")
@ZenRegister
@Name("mods." + MOD_ID + ".Energizer")
public class EnergizerManager implements MachineRecipeManager {

    public static final EnergizerManager INSTANCE = new EnergizerManager();

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
    public RecipeType<MachineRecipe> getRecipeType() {
        return MachineType.ENERGIZER;
    }
}

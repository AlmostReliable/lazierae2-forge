package com.github.almostreliable.lazierae2.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.github.almostreliable.lazierae2.machine.MachineType;
import com.github.almostreliable.lazierae2.recipe.builder.MachineRecipeBuilder;
import com.github.almostreliable.lazierae2.recipe.type.MachineRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Name;

import static com.github.almostreliable.lazierae2.core.Constants.MOD_ID;

@SuppressWarnings("unused")
@ZenRegister
@Name("mods." + MOD_ID + ".Aggregator")
public class AggregatorManager implements MachineRecipeManager {

    public static final AggregatorManager INSTANCE = new AggregatorManager();

    @Override
    public MachineRecipe createRecipe(
        ResourceLocation id, ItemStack output, int amount, Ingredient[] ingredients, int processTime, int energyCost
    ) {
        return MachineRecipeBuilder
            .aggregator(output.getItem(), amount)
            .input(ingredients)
            .processingTime(processTime)
            .energyCost(energyCost)
            .build(id);
    }

    @Override
    public IRecipeType<MachineRecipe> getRecipeType() {
        return MachineType.AGGREGATOR;
    }
}

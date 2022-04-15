package com.almostreliable.lazierae2.compat.crafttweaker;

import com.almostreliable.lazierae2.machine.MachineType;
import com.almostreliable.lazierae2.recipe.builder.MachineRecipeBuilder;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;

@SuppressWarnings("unused")
@ZenRegister
@Name("mods." + MOD_ID + ".RecipeBuilderWrapper")
public class RecipeBuilderWrapper {

    private final IRecipeManager manager;
    private final ResourceLocation id;
    private final MachineRecipeBuilder builder;

    RecipeBuilderWrapper(IRecipeManager manager, MachineType type, ResourceLocation id, IItemStack output) {
        this.manager = manager;
        this.id = id;
        switch (type) {
            case AGGREGATOR:
                builder = MachineRecipeBuilder.aggregator(output.getInternal().getItem(), output.getAmount());
                break;
            case CENTRIFUGE:
                builder = MachineRecipeBuilder.centrifuge(output.getInternal().getItem(), output.getAmount());
                break;
            case ENERGIZER:
                builder = MachineRecipeBuilder.energizer(output.getInternal().getItem(), output.getAmount());
                break;
            case ETCHER:
                builder = MachineRecipeBuilder.etcher(output.getInternal().getItem(), output.getAmount());
                break;
            default:
                throw new IllegalArgumentException("Unknown MachineType: " + type);
        }
    }

    @Method
    public RecipeBuilderWrapper input(IIngredient input) {
        builder.input(input.asVanillaIngredient());
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
        MachineRecipe recipe = builder.build(id);
        CraftTweakerAPI.apply(new ActionAddRecipe(manager, recipe, ""));
    }
}

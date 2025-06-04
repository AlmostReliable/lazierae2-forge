package com.almostreliable.lazierae.registry;

import net.minecraft.network.chat.Component;

import com.almostreliable.lazierae.Constants;
import com.almostreliable.lazierae.block.MachineBlock;
import com.almostreliable.lazierae.data.LazierLang;
import com.almostreliable.lazierae.recipe.ProcessingMachineRecipe;
import com.almostreliable.lib.registry.BlockEntry;
import com.almostreliable.lib.registry.RecipeEntry;

import java.util.function.Supplier;

public enum ProcessingMachineType {

    ETCHER(Constants.ETCHER_ID, LazierLang.ETCHER, LazierBlocks.ETCHER, LazierRecipes.ETCHER),
    INFUSER(Constants.INFUSER_ID, LazierLang.INFUSER, LazierBlocks.INFUSER, LazierRecipes.INFUSER);

    private final String id;
    private final Supplier<? extends Component> displayName;
    private final BlockEntry<? extends MachineBlock<?>> blockEntry;
    private final RecipeEntry<ProcessingMachineRecipe> recipeEntry;

    ProcessingMachineType(
        String id,
        Supplier<? extends Component> displayName,
        BlockEntry<? extends MachineBlock<?>> blockEntry,
        RecipeEntry<ProcessingMachineRecipe> recipeEntry
    ) {
        this.id = id;
        this.displayName = displayName;
        this.blockEntry = blockEntry;
        this.recipeEntry = recipeEntry;
    }

    public String getId() {
        return id;
    }

    public Component getDisplayName() {
        return displayName.get();
    }

    public BlockEntry<? extends MachineBlock<?>> getBlockEntry() {
        return blockEntry;
    }

    public RecipeEntry<ProcessingMachineRecipe> getRecipeEntry() {
        return recipeEntry;
    }
}

package com.almostreliable.lazierae2.compat.jei;

import com.almostreliable.lazierae2.compat.jei.category.AggregatorCategory;
import com.almostreliable.lazierae2.compat.jei.category.CentrifugeCategory;
import com.almostreliable.lazierae2.compat.jei.category.EnergizerCategory;
import com.almostreliable.lazierae2.compat.jei.category.EtcherCategory;
import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.gui.ProcessorScreen;
import com.almostreliable.lazierae2.util.GameUtil;
import com.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
@JeiPlugin
public class Plugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return TextUtil.getRL("main");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration r) {
        var guiHelper = r.getJeiHelpers().getGuiHelper();
        r.addRecipeCategories(new AggregatorCategory(guiHelper));
        r.addRecipeCategories(new CentrifugeCategory(guiHelper));
        r.addRecipeCategories(new EnergizerCategory(guiHelper));
        r.addRecipeCategories(new EtcherCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration r) {
        var recipeManager = GameUtil.getRecipeManager(null);
        r.addRecipes(recipeManager.getAllRecipesFor(ProcessorType.AGGREGATOR), AggregatorCategory.UID);
        r.addRecipes(recipeManager.getAllRecipesFor(ProcessorType.CENTRIFUGE), CentrifugeCategory.UID);
        r.addRecipes(recipeManager.getAllRecipesFor(ProcessorType.ENERGIZER), EnergizerCategory.UID);
        r.addRecipes(recipeManager.getAllRecipesFor(ProcessorType.ETCHER), EtcherCategory.UID);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration r) {
        r.addRecipeTransferHandler(new ProcessorRecipeInfo(AggregatorCategory.UID, 2, 3, 5, 36));
        r.addRecipeTransferHandler(new ProcessorRecipeInfo(CentrifugeCategory.UID, 2, 1, 3, 36));
        r.addRecipeTransferHandler(new ProcessorRecipeInfo(EnergizerCategory.UID, 2, 1, 3, 36));
        r.addRecipeTransferHandler(new ProcessorRecipeInfo(EtcherCategory.UID, 2, 3, 5, 36));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r) {
        r.addRecipeCatalyst(new ItemStack(Blocks.AGGREGATOR.get()), AggregatorCategory.UID);
        r.addRecipeCatalyst(new ItemStack(Blocks.CENTRIFUGE.get()), CentrifugeCategory.UID);
        r.addRecipeCatalyst(new ItemStack(Blocks.ENERGIZER.get()), EnergizerCategory.UID);
        r.addRecipeCatalyst(new ItemStack(Blocks.ETCHER.get()), EtcherCategory.UID);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration r) {
        r.addGuiContainerHandler(ProcessorScreen.class, new ProcessorGuiContainerHandler(
            0,
            -12,
            ProcessorScreen.TEXTURE_WIDTH - ProcessorScreen.ENERGY_WIDTH,
            10,
            AggregatorCategory.UID,
            CentrifugeCategory.UID,
            EnergizerCategory.UID,
            EtcherCategory.UID
        ));
    }
}

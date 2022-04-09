package com.github.almostreliable.lazierae2.compat.jei;

import com.github.almostreliable.lazierae2.compat.jei.category.AggregatorCategory;
import com.github.almostreliable.lazierae2.compat.jei.category.CentrifugeCategory;
import com.github.almostreliable.lazierae2.compat.jei.category.EnergizerCategory;
import com.github.almostreliable.lazierae2.compat.jei.category.EtcherCategory;
import com.github.almostreliable.lazierae2.core.Setup.Blocks;
import com.github.almostreliable.lazierae2.gui.MachineScreen;
import com.github.almostreliable.lazierae2.machine.MachineContainer;
import com.github.almostreliable.lazierae2.util.GameUtil;
import com.github.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("unused")
@JeiPlugin
public class Plugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return TextUtil.getRL("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration r) {
        IGuiHelper guiHelper = r.getJeiHelpers().getGuiHelper();
        r.addRecipeCategories(new AggregatorCategory(guiHelper));
        r.addRecipeCategories(new CentrifugeCategory(guiHelper));
        r.addRecipeCategories(new EnergizerCategory(guiHelper));
        r.addRecipeCategories(new EtcherCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration r) {
        RecipeManager recipeManager = GameUtil.getRecipeManager(null);
        // TODO: Lytho has to do some enum magic here
        // it's usually something like this:
        // r.addRecipes(recipeManager.getAllRecipesFor(Setup.Recipes.Types.AGGREGATOR), AggregatorCategory.UID);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration r) {
        r.addRecipeTransferHandler(MachineContainer.class, AggregatorCategory.UID, 2, 3, 5, 36);
        r.addRecipeTransferHandler(MachineContainer.class, CentrifugeCategory.UID, 2, 1, 3, 36);
        r.addRecipeTransferHandler(MachineContainer.class, EnergizerCategory.UID, 2, 1, 3, 36);
        r.addRecipeTransferHandler(MachineContainer.class, EtcherCategory.UID, 2, 3, 5, 36);
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
        r.addRecipeClickArea(
            MachineScreen.class,
            78,
            23,
            MachineScreen.PROGRESS_WIDTH / 2,
            MachineScreen.PROGRESS_HEIGHT,
            AggregatorCategory.UID,
            CentrifugeCategory.UID,
            EnergizerCategory.UID,
            EtcherCategory.UID
        );
    }
}

package com.almostreliable.lazierae2.compat.jei;

import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.integration.modules.jei.throwinginwater.ThrowingInWaterCategory;
import appeng.integration.modules.jei.throwinginwater.ThrowingInWaterDisplay;
import com.almostreliable.lazierae2.compat.jei.category.AggregatorCategory;
import com.almostreliable.lazierae2.compat.jei.category.EtcherCategory;
import com.almostreliable.lazierae2.compat.jei.category.GrinderCategory;
import com.almostreliable.lazierae2.compat.jei.category.InfuserCategory;
import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.core.Config;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Items;
import com.almostreliable.lazierae2.gui.ProcessorScreen;
import com.almostreliable.lazierae2.gui.RequesterScreen;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.almostreliable.lazierae2.recipe.type.SingleInputRecipe;
import com.almostreliable.lazierae2.recipe.type.TripleInputRecipe;
import com.almostreliable.lazierae2.util.GameUtil;
import com.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@JeiPlugin
public class Plugin implements IModPlugin {

    private static <T extends ProcessorRecipe> List<T> validateRecipes(List<ProcessorRecipe> recipes, Class<T> clazz) {
        return recipes.stream().filter(clazz::isInstance).map(clazz::cast).toList();
    }

    @Override
    public ResourceLocation getPluginUid() {
        return TextUtil.getRL("main");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration r) {
        var guiHelper = r.getJeiHelpers().getGuiHelper();
        r.addRecipeCategories(new AggregatorCategory(guiHelper));
        r.addRecipeCategories(new EtcherCategory(guiHelper));
        r.addRecipeCategories(new GrinderCategory(guiHelper));
        r.addRecipeCategories(new InfuserCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration r) {
        var rm = GameUtil.getRecipeManager(null);
        r.addRecipes(AggregatorCategory.TYPE,
            validateRecipes(rm.getAllRecipesFor(ProcessorType.AGGREGATOR), TripleInputRecipe.class)
        );
        r.addRecipes(EtcherCategory.TYPE,
            validateRecipes(rm.getAllRecipesFor(ProcessorType.ETCHER), TripleInputRecipe.class)
        );
        r.addRecipes(GrinderCategory.TYPE,
            validateRecipes(rm.getAllRecipesFor(ProcessorType.GRINDER), SingleInputRecipe.class)
        );
        r.addRecipes(InfuserCategory.TYPE,
            validateRecipes(rm.getAllRecipesFor(ProcessorType.INFUSER), TripleInputRecipe.class)
        );

        var inWaterRecipes = new ArrayList<>();
        if (AEConfig.instance().isInWorldCrystalGrowthEnabled()) {
            inWaterRecipes.add(new ThrowingInWaterDisplay(List.of(Ingredient.of(Items.RESONATING_SEED.get())),
                new ItemStack(Items.RESONATING_CRYSTAL.get()),
                true
            ));
        }
        if (Config.COMMON.inWorldResonating.get().equals(true)) {
            inWaterRecipes.add(new ThrowingInWaterDisplay(List.of(Ingredient.of(AEItems.SKY_DUST),
                Ingredient.of(Tags.Items.GEMS_DIAMOND),
                Ingredient.of(AEItems.ENDER_DUST)
            ), new ItemStack(Items.RESONATING_DUST.get(), 2), false));
        }
        r.addRecipes(new RecipeType<>(ThrowingInWaterCategory.ID, ThrowingInWaterDisplay.class), inWaterRecipes);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration r) {
        r.addRecipeTransferHandler(new ProcessorRecipeInfo<>(AggregatorCategory.TYPE, 2, 3, 5, 36));
        r.addRecipeTransferHandler(new ProcessorRecipeInfo<>(EtcherCategory.TYPE, 2, 3, 5, 36));
        r.addRecipeTransferHandler(new ProcessorRecipeInfo<>(GrinderCategory.TYPE, 2, 1, 3, 36));
        r.addRecipeTransferHandler(new ProcessorRecipeInfo<>(InfuserCategory.TYPE, 2, 3, 5, 36));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r) {
        r.addRecipeCatalyst(new ItemStack(Blocks.AGGREGATOR.get()), AggregatorCategory.TYPE);
        r.addRecipeCatalyst(new ItemStack(Blocks.ETCHER.get()), EtcherCategory.TYPE);
        r.addRecipeCatalyst(new ItemStack(Blocks.GRINDER.get()), GrinderCategory.TYPE);
        r.addRecipeCatalyst(new ItemStack(Blocks.INFUSER.get()), InfuserCategory.TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration r) {
        var handler = new ProcessorGuiHandler(156,
            7,
            7,
            8,
            AggregatorCategory.TYPE,
            EtcherCategory.TYPE,
            GrinderCategory.TYPE,
            InfuserCategory.TYPE
        );
        r.addGuiContainerHandler(ProcessorScreen.class, handler);
        r.addGhostIngredientHandler(RequesterScreen.class, new RequesterGhostHandler());
    }
}

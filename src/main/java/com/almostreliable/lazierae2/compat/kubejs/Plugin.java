package com.almostreliable.lazierae2.compat.kubejs;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.core.Constants;
import com.almostreliable.lazierae2.util.TextUtil;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;

public class Plugin extends KubeJSPlugin {

    /**
     * Callable through:
     * <ul>
     *     <li>event.recipes.lazierae2.aggregator(output, [...inputs])</li>
     *     <li>event.recipes.lazierae2.etcher(output, [...inputs])</li>
     *     <li>event.recipes.lazierae2.grinder(output, input)</li>
     *     <li>event.recipes.lazierae2.infuser(output, [...inputs])</li>
     * </ul>
     *
     * @param event {@link RegisterRecipeHandlersEvent}
     */
    @Override
    public void addRecipes(RegisterRecipeHandlersEvent event) {
        event.register(TextUtil.getRL(Constants.Blocks.AGGREGATOR_ID),
            () -> new ProcessorRecipeJS(ProcessorType.AGGREGATOR)
        );
        event.register(TextUtil.getRL(Constants.Blocks.ETCHER_ID), () -> new ProcessorRecipeJS(ProcessorType.ETCHER));
        event.register(TextUtil.getRL(Constants.Blocks.GRINDER_ID), () -> new ProcessorRecipeJS(ProcessorType.GRINDER));
        event.register(TextUtil.getRL(Constants.Blocks.INFUSER_ID), () -> new ProcessorRecipeJS(ProcessorType.INFUSER));
    }
}

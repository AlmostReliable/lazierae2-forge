package com.github.almostreliable.lazierae2.util;

import com.github.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import static com.github.almostreliable.lazierae2.core.Constants.MOD_ID;

public final class TextUtil {

    private TextUtil() {}

    /**
     * Gets a resource location with the given key
     * and the namespace of the mod.
     *
     * @param key the key to generate the resource location with
     * @return the generated resource location
     */
    public static ResourceLocation getRL(String key) {
        return new ResourceLocation(MOD_ID, key);
    }

    /**
     * Gets a translated phrase within the mod's namespace.
     *
     * @param type the translation type to get the translation from
     * @param key  the translation key
     * @return the translated phrase
     */
    public static String translateAsString(TRANSLATE_TYPE type, String key) {
        return translate(type, key).getString();
    }

    /**
     * Generates a Translation Text Component within the mod's namespace
     * with a custom type, key and optional color.
     *
     * @param type  the type of the translation
     * @param key   the unique key of the translation
     * @param color an optional color
     * @return the translated phrase
     */
    public static TranslationTextComponent translate(TRANSLATE_TYPE type, String key, TextFormatting... color) {
        TranslationTextComponent output = new TranslationTextComponent(getTranslationKey(type, key));
        return color.length == 0 ? output : (TranslationTextComponent) output.withStyle(color[0]);
    }

    /**
     * Colors a given String with the given color.
     *
     * @param input the string to color
     * @param color an optional color
     * @return the colorized string
     */
    public static StringTextComponent colorize(String input, TextFormatting color) {
        return (StringTextComponent) new StringTextComponent(input).withStyle(color);
    }

    /**
     * Gets the translation key from the provided type and key.
     *
     * @param type the type of the translation
     * @param key  the unique key of the translation
     * @return the translation key
     */
    private static String getTranslationKey(TRANSLATE_TYPE type, String key) {
        return String.format("%s.%s.%s", type.toString().toLowerCase(), MOD_ID, key);
    }
}

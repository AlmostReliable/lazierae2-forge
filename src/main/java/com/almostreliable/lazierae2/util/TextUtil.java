package com.almostreliable.lazierae2.util;

import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.almostreliable.lazierae2.BuildConfig.MOD_ID;

public final class TextUtil {

    private static final Locale LOCALE = Locale.getDefault();
    private static final DecimalFormat DF = (DecimalFormat) NumberFormat.getInstance(LOCALE).clone();
    private static final String[] UNITS = {"", "k", "M", "G", "T", "P"};
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{}");

    private TextUtil() {}

    /**
     * A helper method to format a string with simple bracket placeholders.
     * <p>
     * The bracket pairs will be replaced with the given values in the same order.
     * <p>
     * Brackets with index values will be replaced with the corresponding value.
     *
     * @param input the input string
     * @param args  the values to replace the brackets with
     * @return the formatted string
     */
    public static String f(String input, Object... args) {
        for (var arg : args) {
            input = PLACEHOLDER.matcher(input).replaceFirst(arg.toString());
        }
        for (var i = 0; i < args.length; i++) {
            input = input.replace("{" + i + "}", args[i].toString());
        }
        return input;
    }

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
    public static TranslatableComponent translate(
        TRANSLATE_TYPE type, String key, ChatFormatting... color
    ) {
        var output = new TranslatableComponent(getTranslationKey(type, key));
        return color.length == 0 ? output : (TranslatableComponent) output.withStyle(color[0]);
    }

    /**
     * Formats a number into a human-readable energy string.
     *
     * @param energy    The energy to format
     * @param minPlaces The minimum number of decimal places
     * @param maxPlaces The maximum number of decimal places
     * @param extended  Whether to use extended formatting
     * @param suffix    Whether to use the energy suffix like FE or kFE
     * @return The formatted energy string
     */
    public static String formatEnergy(Number energy, int minPlaces, int maxPlaces, boolean extended, boolean suffix) {
        // extended format
        if (extended) return formatNumber(energy, minPlaces, maxPlaces) + (suffix ? " FE" : "");
        // compact format
        var numberOfDigits =
            energy.intValue() == 0 ? 0 : (int) (1 + Math.floor(Math.log10(Math.abs(energy.doubleValue()))));
        var base10Exponent = numberOfDigits < 4 ? 0 : 3 * ((numberOfDigits - 1) / 3);
        var normalized = energy.doubleValue() / Math.pow(10, base10Exponent);
        return formatNumber(normalized, minPlaces, maxPlaces) + (suffix ? f(" {}FE", UNITS[base10Exponent / 3]) : "");
    }

    /**
     * Colors a given String with the given color.
     *
     * @param input the string to color
     * @param color an optional color
     * @return the colorized string
     */
    static TextComponent colorize(String input, ChatFormatting color) {
        return (TextComponent) new TextComponent(input).withStyle(color);
    }

    /**
     * Formats a number into a correctly rounded string with the given number of decimal places.
     * <p>
     * The method uses the locale of the current user and formats the number accordingly.
     *
     * @param number    the number to format
     * @param minPlaces the minimum number of decimal places
     * @param maxPlaces the maximum amount of decimal places
     * @return the formatted number
     */
    private static String formatNumber(Number number, int minPlaces, int maxPlaces) {
        DF.setRoundingMode(RoundingMode.HALF_UP);
        DF.setMinimumFractionDigits(minPlaces);
        DF.setMaximumFractionDigits(maxPlaces);
        return DF.format(number);
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

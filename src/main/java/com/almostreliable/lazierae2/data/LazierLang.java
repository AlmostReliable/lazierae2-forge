package com.almostreliable.lazierae2.data;

import com.almostreliable.lazierae2.BuildConfig;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class LazierLang extends LanguageProvider {

    LazierLang(PackOutput output) {
        super(output, BuildConfig.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (LangEntry entry : LangEntry.ENTRIES) {
            add(entry.key, entry.value);
        }
    }

    public static final class LangEntry implements Supplier<MutableComponent> {

        private static final List<LangEntry> ENTRIES = new ArrayList<>();

        private final String key;
        private final String value;

        private LangEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public static LangEntry of(String prefix, String id, String value) {
            LangEntry entry = new LangEntry(String.format("%s.%s.%s", prefix, BuildConfig.MOD_ID, id), value);
            ENTRIES.add(entry);
            return entry;
        }

        private static LangEntry menu(String id, String value) {
            return of("menu", id, value);
        }

        @Override
        public MutableComponent get() {
            return Component.translatable(key, value);
        }
    }
}

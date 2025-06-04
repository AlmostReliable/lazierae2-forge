package com.almostreliable.lazierae.data;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.almostreliable.lazierae.Constants;
import com.almostreliable.lazierae.LazierAE2;
import com.almostreliable.lazierae.LazierAE2Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface LazierLang {

    LangEntry ETCHER = LangEntry.menu(Constants.ETCHER_ID, "Circuit Etcher");
    LangEntry INFUSER = LangEntry.menu(Constants.INFUSER_ID, "Matter Infuser");
    LangEntry INJECTOR = LangEntry.menu(Constants.INJECTOR_ID, "Crystal Injector");

    static void init() {
        LazierAE2.MANAGER.dataGen().common().lang(p -> {
            for (LangEntry entry : LangEntry.ENTRIES) {
                p.addLang(entry.key, entry.value);
            }
        });
    }

    final class LangEntry implements Supplier<MutableComponent> {

        private static final List<LangEntry> ENTRIES = new ArrayList<>();

        private final String key;
        private final String value;

        private LangEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        private static LangEntry of(String prefix, String id, String value) {
            LangEntry entry = new LangEntry(String.format("%s.%s.%s", prefix, LazierAE2Constants.MOD_ID, id), value);
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

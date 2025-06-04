package com.almostreliable.lazierae.recipe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import com.almostreliable.lib.item.IngredientStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

public class TriInput {

    public static final Serializer SERIALIZER = new Serializer();

    @Nullable
    private final IngredientStack top;
    @Nullable
    private final IngredientStack middle;
    @Nullable
    private final IngredientStack bottom;

    public TriInput(@Nullable IngredientStack top, @Nullable IngredientStack middle, @Nullable IngredientStack bottom) {
        this.top = top;
        this.middle = middle;
        this.bottom = bottom;
    }

    public boolean isTop(ItemStack stack) {
        return top == null ? stack.isEmpty() : top.test(stack);
    }

    public boolean isMiddle(ItemStack stack) {
        return middle == null ? stack.isEmpty() : middle.test(stack);
    }

    public boolean isBottom(ItemStack stack) {
        return bottom == null ? stack.isEmpty() : bottom.test(stack);
    }

    public static class Serializer {

        public TriInput fromJson(@Nullable JsonElement json) {
            if (!(json instanceof JsonObject jsonObject)) {
                throw new IllegalArgumentException("expected JsonObject");
            }

            IngredientStack top = deserializeIngredientStack(jsonObject, "top");
            IngredientStack middle = deserializeIngredientStack(jsonObject, "middle");
            IngredientStack bottom = deserializeIngredientStack(jsonObject, "bottom");

            return new TriInput(top, middle, bottom);
        }

        public TriInput fromNetwork(FriendlyByteBuf buffer) {
            IngredientStack top = decodeIngredientStack(buffer);
            IngredientStack middle = decodeIngredientStack(buffer);
            IngredientStack bottom = decodeIngredientStack(buffer);

            return new TriInput(top, middle, bottom);
        }

        public void toNetwork(FriendlyByteBuf buffer, TriInput triInput) {
            encodeIngredientStack(buffer, triInput.top);
            encodeIngredientStack(buffer, triInput.middle);
            encodeIngredientStack(buffer, triInput.bottom);
        }

        @Nullable
        private static IngredientStack deserializeIngredientStack(JsonObject json, String key) {
            if (json.has(key)) {
                return IngredientStack.SERIALIZER.fromJson(json.get(key));
            }
            return null;
        }

        @Nullable
        private static IngredientStack decodeIngredientStack(FriendlyByteBuf buffer) {
            if (buffer.readBoolean()) {
                return IngredientStack.SERIALIZER.fromNetwork(buffer);
            }
            return null;
        }

        private static void encodeIngredientStack(FriendlyByteBuf buffer, @Nullable IngredientStack ingredientStack) {
            if (ingredientStack == null) {
                buffer.writeBoolean(false);
            } else {
                buffer.writeBoolean(true);
                IngredientStack.SERIALIZER.toNetwork(buffer, ingredientStack);
            }
        }
    }
}

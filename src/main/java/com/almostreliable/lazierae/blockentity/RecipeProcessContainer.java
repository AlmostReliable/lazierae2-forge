package com.almostreliable.lazierae.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import com.almostreliable.lib.util.Serializable;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipeProcessContainer<R extends Recipe<?>> implements Serializable<CompoundTag> {

    private final List<ItemStack> inputs = new ArrayList<>();

    @Nullable
    private R recipe;
    private int progress;
    private int time;

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        if (recipe != null) {
            ListTag list = new ListTag();
            for (ItemStack stack : inputs) {
                list.add(stack.serializeNBT());
            }
            tag.put("inputs", list);
            tag.putInt("progress", progress);
            tag.putInt("time", time);
        }
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        if (tag.contains("inputs")) {
            ListTag list = tag.getList("inputs", Tag.TAG_COMPOUND);
            inputs.clear();
            for (int i = 0; i < list.size(); i++) {
                inputs.add(ItemStack.of(list.getCompound(i)));
            }
        }
        if (tag.contains("progress")) progress = tag.getInt("progress");
        if (tag.contains("time")) time = tag.getInt("time");
    }

    public boolean isPresent() {
        return recipe != null;
    }

    public void reset() {
        recipe = null;
        progress = 0;
        time = 0;
    }

    public int getProgress() {
        return progress;
    }

    public int getTime() {
        return time;
    }
}

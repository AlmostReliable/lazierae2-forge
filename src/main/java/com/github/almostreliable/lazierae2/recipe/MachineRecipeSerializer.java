package com.github.almostreliable.lazierae2.recipe;

import com.github.almostreliable.lazierae2.util.RecipeUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class MachineRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<MachineRecipe> {

    private final MachineType machineType;

    public MachineRecipeSerializer(MachineType machineType) {
        this.machineType = machineType;
    }

    @Override
    public MachineRecipe fromJson(ResourceLocation id, JsonObject json) {
        MachineRecipe recipe = machineType.getFactory().apply(id, machineType);
        return RecipeUtil.fromJSON(json, recipe);
    }

    @Nullable
    @Override
    public MachineRecipe fromNetwork(ResourceLocation id, PacketBuffer buffer) {
        MachineRecipe recipe = machineType.getFactory().apply(id, machineType);
        return RecipeUtil.fromNetwork(buffer, recipe);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, MachineRecipe recipe) {
        RecipeUtil.toNetwork(buffer, recipe);
    }
}

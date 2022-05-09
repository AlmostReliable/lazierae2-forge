package com.almostreliable.lazierae2.recipe.type;

import com.almostreliable.lazierae2.machine.MachineType;
import com.almostreliable.lazierae2.util.RecipeUtil;
import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public abstract class MachineRecipe implements IRecipe<IInventory> {

    final NonNullList<Ingredient> inputs = NonNullList.create();
    private final MachineType machineType;
    private final ResourceLocation id;
    private int processTime;
    private int energyCost;
    private ItemStack output;

    MachineRecipe(
        ResourceLocation id, MachineType machineType
    ) {
        this.id = id;
        this.machineType = machineType;
    }

    @Override
    public ItemStack assemble(IInventory inv) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return machineType.getRecipeSerializer().get();
    }

    @Override
    public IRecipeType<?> getType() {
        return machineType;
    }

    public void validate() {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException(f("No inputs for recipe type '{}' with output '{}'!",
                machineType.getId(),
                output.toString()
            ));
        }
        if (inputs.size() > machineType.getInputSlots()) {
            throw new IllegalArgumentException(f("Too many inputs for recipe type '{}' with output '{}'!",
                machineType.getId(),
                output.toString()
            ));
        }
        if (processTime == 0) processTime = machineType.getBaseProcessTime();
        if (energyCost == 0) energyCost = machineType.getBaseEnergyCost();
    }

    public int getProcessTime() {
        return processTime;
    }

    public void setProcessTime(int processTime) {
        this.processTime = processTime;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public void setEnergyCost(int energyCost) {
        this.energyCost = energyCost;
    }

    public NonNullList<Ingredient> getInputs() {
        return inputs;
    }

    public void setOutput(ItemStack output) {
        this.output = output;
    }

    public static class MachineRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<MachineRecipe> {

        private final MachineType machineType;

        public MachineRecipeSerializer(MachineType machineType) {
            this.machineType = machineType;
        }

        @Override
        public MachineRecipe fromJson(ResourceLocation id, JsonObject json) {
            MachineRecipe recipe = machineType.getRecipeFactory().apply(id, machineType);
            RecipeUtil.fromJSON(json, recipe);
            recipe.validate();
            return recipe;
        }

        @Nullable
        @Override
        public MachineRecipe fromNetwork(ResourceLocation id, PacketBuffer buffer) {
            MachineRecipe recipe = machineType.getRecipeFactory().apply(id, machineType);
            RecipeUtil.fromNetwork(buffer, recipe);
            return recipe;
        }

        @Override
        public void toNetwork(PacketBuffer buffer, MachineRecipe recipe) {
            RecipeUtil.toNetwork(buffer, recipe);
        }
    }
}

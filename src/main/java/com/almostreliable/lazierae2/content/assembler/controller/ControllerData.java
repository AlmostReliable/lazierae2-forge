package com.almostreliable.lazierae2.content.assembler.controller;

import appeng.api.crafting.IPatternDetails;
import com.almostreliable.lazierae2.content.assembler.holder.PatternHolderBlock;
import com.almostreliable.lazierae2.content.assembler.holder.PatternHolderEntity;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ControllerData implements INBTSerializable<ListTag>, IItemHandler {

    private final ControllerEntity controller;
    private final Set<BlockPos> holderPositions = new HashSet<>();
    private final List<PatternHolderEntity> patternHolderCache = new ArrayList<>();
    private int accelerators;

    ControllerData(ControllerEntity controller) {
        this.controller = controller;
        RangeMap<Integer, String> range = TreeRangeMap.create();
        range.put(Range.closed(0, 20), "Accelerator");
        range.get(15);
    }

    @Override
    public ListTag serializeNBT() {
        var tag = new ListTag();
        for (var holderPos : holderPositions) {
            tag.add(NbtUtils.writeBlockPos(holderPos));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(ListTag tag) {
        holderPositions.clear();
        for (var holderPos : tag) {
            holderPositions.add(NbtUtils.readBlockPos((CompoundTag) holderPos));
        }
    }

    void onLoad() {
        update();
    }

    void addHolder(BlockPos holderPos) {
        holderPositions.add(holderPos);
    }

    private void update() {
        if (controller.getLevel() == null || controller.getLevel().isClientSide || holderPositions.isEmpty()) return;
        patternHolderCache.clear();
        accelerators = 0;

        for (var holderPos : holderPositions) {
            var block = controller.getLevel().getBlockState(holderPos).getBlock();
            holderPositions.removeIf(pos -> !(block instanceof PatternHolderBlock));
            var holder = controller.getLevel().getBlockEntity(holderPos);
            if (holder instanceof PatternHolderEntity holderEntity) {
                // holderEntity.rebindController(controller);
                patternHolderCache.add(holderEntity);
            } else {
                accelerators++;
            }
        }
    }

    List<PatternHolderEntity> getPatternHolders() {
        if (holderPositions.size() != patternHolderCache.size()) {
            update();
        }
        return patternHolderCache;
    }

    List<IPatternDetails> getPatterns() {
        List<IPatternDetails> patterns = new ArrayList<>();
        for (var holder : patternHolderCache) {
            // patterns.addAll(holder.getPatterns());
        }
        return patterns;
    }

    @Override
    public int getSlots() {
        return 0;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return null;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false;
    }
}

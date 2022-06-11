package com.almostreliable.lazierae2.content.assembler;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.content.assembler.PatternHolderBlock.HOLDER_TIER;
import com.almostreliable.lazierae2.core.Setup.Entities.Assembler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.almostreliable.lazierae2.core.Constants.Nbt.INVENTORY_ID;

public class PatternHolderEntity extends GenericEntity {

    final PatternInventory patternStorage;
    private final List<IPatternDetails> patterns = new ArrayList<>();

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public PatternHolderEntity(
        BlockPos pos, BlockState state
    ) {
        super(Assembler.PATTERN_HOLDER.get(), pos, state);
        patternStorage = new PatternInventory(this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(INVENTORY_ID)) patternStorage.deserializeNBT(tag.getCompound(INVENTORY_ID));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(INVENTORY_ID, patternStorage.serializeNBT());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int menuID, Inventory inventory, Player player) {
        return new PatternHolderMenu(menuID, this, inventory);
    }

    @Override
    protected void playerDestroy(boolean creative) {
        assert level != null;
        patternStorage.dropContents();
    }

    void updatePatterns() {
        patterns.clear();
        for (var slot = 0; slot < patternStorage.getSlots(); slot++) {
            var stack = patternStorage.getStackInSlot(slot);
            var details = PatternDetailsHelper.decodePattern(stack, getLevel());
            if (details != null) patterns.add(details);
        }
        setChanged();
    }

    public HOLDER_TIER getTier() {
        return ((PatternHolderBlock) getBlockState().getBlock()).getTier();
    }
}

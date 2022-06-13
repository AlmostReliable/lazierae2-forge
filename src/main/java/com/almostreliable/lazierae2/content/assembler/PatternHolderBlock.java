package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.assembler.MultiBlock.PositionType;

public class PatternHolderBlock extends AssemblerBlock {

    private final HOLDER_TIER tier;

    public PatternHolderBlock(HOLDER_TIER tier) {
        this.tier = tier;
    }

    @Override
    public boolean isValidMultiBlockPos(PositionType posType) {
        return posType == PositionType.INNER;
    }

    public HOLDER_TIER getTier() {
        return tier;
    }

    public enum HOLDER_TIER {
        ACCELERATOR, TIER_1, TIER_2, TIER_3
    }
}

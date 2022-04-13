package com.almostreliable.lazierae2.multiblock;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;

public enum OptionalDirection implements StringRepresentable {
    NONE("none", null),
    UP("up", Direction.UP),
    DOWN("down", Direction.DOWN),
    NORTH("north", Direction.NORTH),
    EAST("east", Direction.EAST),
    SOUTH("south", Direction.SOUTH),
    WEST("west", Direction.WEST);

    private static final BiMap<OptionalDirection, Direction> OPT_TO_DIR = HashBiMap.create();

    static {
        for (OptionalDirection value : OptionalDirection.values()) {
            OPT_TO_DIR.put(value, value.direction);
        }
    }

    @Nullable
    private final Direction direction;
    private final String name;

    OptionalDirection(String name, @Nullable Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    @Nullable
    public static OptionalDirection fromDirection(@Nullable Direction direction) {
        return OPT_TO_DIR.inverse().get(direction);
    }

    @Nullable
    public static Direction fromOptionalDirection(@Nullable OptionalDirection direction) {
        return OPT_TO_DIR.get(direction);
    }

    @Nullable
    public Direction getDirection() {
        return direction;
    }

    public BlockPos relative(BlockPos blockPos) {
        if (direction == null) {
            return blockPos;
        }

        return blockPos.relative(direction);
    }

    public void relative(BlockPos.MutableBlockPos blockPos) {
        if (direction != null) {
            blockPos.set(direction.getNormal());
        }
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}

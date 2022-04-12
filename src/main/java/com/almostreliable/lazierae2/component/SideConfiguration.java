package com.almostreliable.lazierae2.component;

import com.almostreliable.lazierae2.core.TypeEnums.BLOCK_SIDE;
import com.almostreliable.lazierae2.core.TypeEnums.IO_SETTING;
import com.almostreliable.lazierae2.machine.MachineBlock;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.function.Consumer;

public class SideConfiguration implements INBTSerializable<CompoundTag> {

    private final BlockEntity entity;
    private final EnumMap<Direction, IO_SETTING> config = new EnumMap<>(Direction.class);

    public SideConfiguration(BlockEntity entity) {
        this.entity = entity;
        for (var direction : Direction.values()) {
            config.put(direction, IO_SETTING.OFF);
        }
    }

    public IO_SETTING get(Direction direction) {
        return config.get(direction);
    }

    public IO_SETTING get(BLOCK_SIDE side) {
        return config.get(getDirectionFromSide(side));
    }

    public void set(BLOCK_SIDE side, IO_SETTING setting) {
        config.put(getDirectionFromSide(side), setting);
        entity.setChanged();
    }

    public void reset() {
        for (var direction : Direction.values()) {
            config.put(direction, IO_SETTING.OFF);
        }
    }

    public boolean hasChanged() {
        return Arrays.stream(Direction.values()).anyMatch(direction -> config.get(direction) != IO_SETTING.OFF);
    }

    public void forEachOutput(Consumer<? super Direction> consumer) {
        for (var direction : Direction.values()) {
            if (config.get(direction) == IO_SETTING.OUTPUT || config.get(direction) == IO_SETTING.IO) {
                consumer.accept(direction);
            }
        }
    }

    public ContainerData toContainerData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return config.get(Direction.values()[index]).ordinal();
            }

            @Override
            public void set(int index, int value) {
                config.put(Direction.values()[index], IO_SETTING.values()[value]);
                entity.setChanged();
            }

            @Override
            public int getCount() {
                return config.size();
            }
        };
    }

    @Override
    public CompoundTag serializeNBT() {
        var nbt = new CompoundTag();
        for (var side : BLOCK_SIDE.values()) {
            nbt.putInt(side.toString(), get(side).ordinal());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (var side : BLOCK_SIDE.values()) {
            set(side, IO_SETTING.values()[tag.getInt(side.toString())]);
        }
    }

    private Direction getDirectionFromSide(BLOCK_SIDE side) {
        var facing = entity.getBlockState().getValue(MachineBlock.FACING);
        return switch (side) {
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
            case LEFT -> facing.getClockWise();
            case RIGHT -> facing.getCounterClockWise();
            case BACK -> facing.getOpposite();
            default -> facing;
        };
    }
}

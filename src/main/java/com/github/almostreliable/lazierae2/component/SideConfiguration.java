package com.github.almostreliable.lazierae2.component;

import com.github.almostreliable.lazierae2.core.TypeEnums.BLOCK_SIDE;
import com.github.almostreliable.lazierae2.core.TypeEnums.IO_SETTING;
import com.github.almostreliable.lazierae2.machine.MachineBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.EnumMap;
import java.util.function.Consumer;

public class SideConfiguration implements INBTSerializable<CompoundNBT> {

    private final TileEntity tile;
    private final EnumMap<Direction, IO_SETTING> config = new EnumMap<>(Direction.class);

    public SideConfiguration(TileEntity tile) {
        this.tile = tile;
        for (Direction direction : Direction.values()) {
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
        tile.setChanged();
    }

    public void reset() {
        for (Direction direction : Direction.values()) {
            config.put(direction, IO_SETTING.OFF);
        }
    }

    public void forEachOutput(Consumer<? super Direction> consumer) {
        for (Direction direction : Direction.values()) {
            if (config.get(direction) == IO_SETTING.OUTPUT || config.get(direction) == IO_SETTING.IO) {
                consumer.accept(direction);
            }
        }
    }

    public IIntArray toIIntArray() {
        return new IIntArray() {
            @Override
            public int get(int index) {
                return config.get(Direction.values()[index]).ordinal();
            }

            @Override
            public void set(int index, int value) {
                config.put(Direction.values()[index], IO_SETTING.values()[value]);
                tile.setChanged();
            }

            @Override
            public int getCount() {
                return config.size();
            }
        };
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        for (Direction direction : Direction.values()) {
            nbt.putInt(direction.toString(), config.get(direction).ordinal());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        for (Direction direction : Direction.values()) {
            config.put(direction, IO_SETTING.values()[nbt.getInt(direction.toString())]);
        }
    }

    private Direction getDirectionFromSide(BLOCK_SIDE side) {
        Direction facing = tile.getBlockState().getValue(MachineBlock.FACING);
        switch (side) {
            case TOP:
                return Direction.UP;
            case BOTTOM:
                return Direction.DOWN;
            case LEFT:
                return facing.getClockWise();
            case RIGHT:
                return facing.getCounterClockWise();
            case BACK:
                return facing.getOpposite();
            default:
                return facing;
        }
    }
}

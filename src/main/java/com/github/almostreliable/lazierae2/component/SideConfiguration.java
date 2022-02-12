package com.github.almostreliable.lazierae2.component;

import com.github.almostreliable.lazierae2.core.TypeEnums.BLOCK_SIDE;
import com.github.almostreliable.lazierae2.core.TypeEnums.IO_SETTING;
import com.github.almostreliable.lazierae2.machine.MachineBlock;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.EnumMap;
import java.util.function.Consumer;

public class SideConfiguration implements INBTSerializable<CompoundNBT> {

    private final EnumMap<Direction, IO_SETTING> config = new EnumMap<>(Direction.class);

    public SideConfiguration() {
        for (Direction direction : Direction.values()) {
            config.put(direction, IO_SETTING.OFF);
        }
    }

    /**
     * Gets the direction from the given block side depending on the facing of the block.
     *
     * @param state the block state
     * @param side  the block side to get the direction from
     * @return the direction
     */
    private static Direction getDirectionFromSide(BlockState state, BLOCK_SIDE side) {
        Direction facing = state.getValue(MachineBlock.FACING);
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

    /**
     * Gets an IO setting by a specified direction.
     * This automatically takes the facing direction into account.
     *
     * @param direction the direction to get the IO setting from
     * @return the IO setting
     */
    public IO_SETTING get(Direction direction) {
        return config.get(direction);
    }

    /**
     * Gets an IO setting by a specified block side.
     * This automatically takes the facing direction into account.
     *
     * @param state the block state
     * @param side  the block side to get the IO setting from
     * @return the IO setting
     */
    public IO_SETTING get(BlockState state, BLOCK_SIDE side) {
        return config.get(getDirectionFromSide(state, side));
    }

    /**
     * Sets the specified block side to the specified IO setting.
     *
     * @param state   the block state
     * @param side    the side on which the setting should be changed
     * @param setting the setting which should be set
     */
    public void set(BlockState state, BLOCK_SIDE side, IO_SETTING setting) {
        config.put(getDirectionFromSide(state, side), setting);
    }

    /**
     * Applies the given consumer to all output sides.
     *
     * @param consumer the consumer to apply
     */
    public void forEachOutput(Consumer<? super Direction> consumer) {
        for (Direction direction : Direction.values()) {
            if (config.get(direction) == IO_SETTING.OUTPUT || config.get(direction) == IO_SETTING.IO) {
                consumer.accept(direction);
            }
        }
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
}

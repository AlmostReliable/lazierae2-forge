package com.almostreliable.lazierae2.content.processor;

import com.almostreliable.lazierae2.content.MachineBlock;
import com.almostreliable.lazierae2.core.TypeEnums.BLOCK_SIDE;
import com.almostreliable.lazierae2.core.TypeEnums.IO_SETTING;
import com.almostreliable.lazierae2.network.sync.IDataHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.function.Consumer;

public class SideConfiguration implements INBTSerializable<CompoundTag>, IDataHandler {

    private final BlockEntity entity;
    private final EnumMap<Direction, IO_SETTING> config = new EnumMap<>(Direction.class);
    private boolean changed;

    SideConfiguration(BlockEntity entity) {
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
        changed = true;
    }

    public void reset() {
        for (var direction : Direction.values()) {
            config.put(direction, IO_SETTING.OFF);
        }
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

    @Override
    public void encode(FriendlyByteBuf buffer) {
        for (var i = 0; i < BLOCK_SIDE.values().length; i++) {
            buffer.writeInt(get(BLOCK_SIDE.values()[i]).ordinal());
        }
        changed = false;
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        for (var i = 0; i < BLOCK_SIDE.values().length; i++) {
            set(BLOCK_SIDE.values()[i], IO_SETTING.values()[buffer.readInt()]);
        }
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    void forEachOutput(Consumer<? super Direction> consumer) {
        for (var direction : Direction.values()) {
            if (config.get(direction) == IO_SETTING.OUTPUT || config.get(direction) == IO_SETTING.IO) {
                consumer.accept(direction);
            }
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

    boolean isConfigured() {
        return Arrays.stream(Direction.values()).anyMatch(direction -> config.get(direction) != IO_SETTING.OFF);
    }
}

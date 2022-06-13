package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.assembler.MultiBlock.PositionType;
import com.almostreliable.lazierae2.content.assembler.controller.ControllerBlock;
import com.almostreliable.lazierae2.content.assembler.controller.ControllerEntity;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.almostreliable.lazierae2.content.assembler.MultiBlock.MAX_SIZE;

public class HullBlock extends AssemblerBlock {

    public static final OptionalDirectionProperty HORIZONTAL = OptionalDirectionProperty.HORIZONTAL_PROP;
    public static final OptionalDirectionProperty VERTICAL = OptionalDirectionProperty.VERTICAL_PROP;
    public final HULL_TYPE type;

    public HullBlock(HULL_TYPE type) {
        this.type = type;
        registerDefaultState(defaultBlockState()
            .setValue(HORIZONTAL, OptionalDirection.NONE)
            .setValue(VERTICAL, OptionalDirection.NONE));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var superState = super.getStateForPlacement(context);
        var state = superState == null ? defaultBlockState() : superState;
        return state.setValue(HORIZONTAL, OptionalDirection.NONE).setValue(VERTICAL, OptionalDirection.NONE);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HORIZONTAL, VERTICAL);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (isMultiBlock(oldState) && newState.isAir()) {
            var controllerPos = findControllerPos(level, pos, oldState);
            if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof ControllerEntity entity &&
                entity.getBlockState().getBlock() instanceof ControllerBlock controller) {
                controller.destroyMultiBlock(level, entity, pos);
            }
        }
        super.onRemove(oldState, level, pos, newState, isMoving);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return super.use(state, level, pos, player, hand, hit);
        }

        // TODO: check if valid multiblock and open the gui through the controller
        var controllerState = findControllerPos(level, pos, state);

        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isValidMultiBlockPos(PositionType posType) {
        return (posType == PositionType.WALL && type == HULL_TYPE.WALL) ||
            ((posType == PositionType.CORNER || posType == PositionType.EDGE) && type == HULL_TYPE.FRAME);
    }

    @Override
    public BlockState setupMultiBlockState(BlockState state, BlockPos hullPos, BlockPos controllerPos) {
        return super.setupMultiBlockState(state, hullPos, controllerPos)
            .setValue(HORIZONTAL, getHorizontalOffset(hullPos, controllerPos))
            .setValue(VERTICAL, getVerticalOffset(hullPos, controllerPos));
    }

    private OptionalDirection getHorizontalOffset(BlockPos hullPos, BlockPos controllerPos) {
        if (controllerPos.getX() == hullPos.getX() && controllerPos.getZ() == hullPos.getZ()) {
            return OptionalDirection.NONE;
        }
        if (controllerPos.getZ() < hullPos.getZ()) {
            return OptionalDirection.NORTH;
        }
        if (controllerPos.getZ() > hullPos.getZ()) {
            return OptionalDirection.SOUTH;
        }
        if (controllerPos.getX() < hullPos.getX()) {
            return OptionalDirection.WEST;
        }
        if (controllerPos.getX() > hullPos.getX()) {
            return OptionalDirection.EAST;
        }
        return OptionalDirection.NONE;
    }

    private OptionalDirection getVerticalOffset(BlockPos hullPos, BlockPos controllerPos) {
        if (controllerPos.getY() == hullPos.getY()) {
            return OptionalDirection.NONE;
        }
        return controllerPos.getY() < hullPos.getY() ? OptionalDirection.DOWN : OptionalDirection.UP;
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    @Nullable
    private BlockPos findControllerPos(
        Level level, BlockPos pos, BlockState state
    ) {
        var horizontal = state.getValue(HORIZONTAL);
        var vertical = state.getValue(VERTICAL);
        if (horizontal == OptionalDirection.NONE && vertical == OptionalDirection.NONE) {
            return null;
        }

        var mutablePos = pos.mutable();
        for (var i = 0; i < MAX_SIZE; i++) {
            horizontal.relative(mutablePos);
            vertical.relative(mutablePos);
            var relativeState = level.getBlockState(mutablePos);
            if (relativeState.getBlock() instanceof ControllerBlock) {
                return mutablePos;
            }
            if (relativeState.getBlock() instanceof HullBlock) {
                return findControllerPos(level, mutablePos, relativeState);
            }
        }
        return null;
    }

    public enum HULL_TYPE {
        WALL, FRAME;

        public boolean validForMultiBlock(BlockState state) {
            return state.getBlock() instanceof HullBlock hull && !isMultiBlock(state) && hull.type == this;
        }
    }

    private enum OptionalDirection implements StringRepresentable {

        NONE(null),
        UP(Direction.UP),
        DOWN(Direction.DOWN),
        NORTH(Direction.NORTH),
        EAST(Direction.EAST),
        SOUTH(Direction.SOUTH),
        WEST(Direction.WEST);

        private static final BiMap<OptionalDirection, Direction> OPT_TO_DIR = HashBiMap.create();
        @Nullable
        private final Direction direction;
        private final String name;

        static {
            for (var value : values()) {
                OPT_TO_DIR.put(value, value.direction);
            }
        }

        OptionalDirection(@Nullable Direction direction) {
            this.direction = direction;
            name = direction == null ? "none" : direction.toString().toLowerCase();
        }

        @Nullable
        public static OptionalDirection fromDirection(@Nullable Direction direction) {
            return OPT_TO_DIR.inverse().get(direction);
        }

        public void relative(MutableBlockPos pos) {
            if (direction == null) return;
            pos.move(direction.getNormal());
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    private static final class OptionalDirectionProperty extends EnumProperty<OptionalDirection> {

        private static final OptionalDirectionProperty HORIZONTAL_PROP = create(
            "horizontal",
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
        );
        private static final OptionalDirectionProperty VERTICAL_PROP = create("vertical", Direction.UP, Direction.DOWN);

        private OptionalDirectionProperty(
            String name, Collection<Direction> directions
        ) {
            super(name, OptionalDirection.class, createOptionalDirections(directions));
        }

        private static OptionalDirectionProperty create(String name, Direction... directions) {
            return new OptionalDirectionProperty(name, List.of(directions));
        }

        private static Collection<OptionalDirection> createOptionalDirections(Collection<Direction> directions) {
            Set<OptionalDirection> optionalDirections = EnumSet.noneOf(OptionalDirection.class);
            optionalDirections.add(OptionalDirection.NONE);
            directions.forEach(direction -> optionalDirections.add(OptionalDirection.fromDirection(direction)));
            return optionalDirections;
        }
    }
}

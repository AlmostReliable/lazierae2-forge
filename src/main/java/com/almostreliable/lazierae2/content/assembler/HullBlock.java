package com.almostreliable.lazierae2.content.assembler;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Tuple;
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
        if (newState.isAir()) {
            var controllerTuple = findControllerBlockState(level,
                pos,
                oldState.getValue(HORIZONTAL),
                oldState.getValue(VERTICAL)
            );
            if (controllerTuple != null && controllerTuple.getA().getBlock() instanceof ControllerBlock cb) {
                cb.invalidate(level, controllerTuple.getA(), controllerTuple.getB());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return super.use(state, level, pos, player, hand, hit);
        }

        var horizontalDirection = state.getValue(HORIZONTAL);
        var verticalDirection = state.getValue(VERTICAL);

        var found = findControllerBlockState(level, pos, horizontalDirection, verticalDirection);
        // TODO open gui

        return InteractionResult.CONSUME;
    }

    @Nullable
    public Tuple<BlockState, BlockPos> findControllerBlockState(
        Level level, BlockPos pos, OptionalDirection horizontalDirection, OptionalDirection verticalDirection
    ) {
        if (horizontalDirection == OptionalDirection.NONE && verticalDirection == OptionalDirection.NONE) {
            return null;
        }

        var mutable = pos.mutable();
        for (var i = 0; i < ControllerBlock.MAX_SIZE; i++) {
            horizontalDirection.relative(mutable);
            verticalDirection.relative(mutable);

            var relativeBlockState = level.getBlockState(mutable);
            if (relativeBlockState.getBlock() instanceof ControllerBlock) {
                return new Tuple<>(relativeBlockState, mutable.immutable());
            }

            if (relativeBlockState.getBlock() instanceof HullBlock) {
                var horizontal = relativeBlockState.getValue(HORIZONTAL);
                var vertical = relativeBlockState.getValue(VERTICAL);
                return findControllerBlockState(level, mutable, horizontal, vertical);
            }
        }
        return null;
    }

    public BlockState createDefaultMultiBlockState(BlockPos blockPos, BlockPos lookPos) {
        var horizontalOffset = getHorizontalOffset(blockPos, lookPos);
        var verticalOffset = getVerticalOffset(blockPos, lookPos);

        return defaultBlockState()
            .setValue(HORIZONTAL, horizontalOffset)
            .setValue(VERTICAL, verticalOffset)
            .setValue(IS_MULTIBLOCK, true);
    }

    public boolean isUsableForMultiBlock(BlockState state) {
        return state.getBlock() instanceof HullBlock && !state.getValue(IS_MULTIBLOCK);
    }

    protected OptionalDirection getVerticalOffset(BlockPos blockPos, BlockPos lookPos) {
        if (lookPos.getY() == blockPos.getY()) {
            return OptionalDirection.NONE;
        }

        return lookPos.getY() < blockPos.getY() ? OptionalDirection.DOWN : OptionalDirection.UP;
    }

    protected OptionalDirection getHorizontalOffset(BlockPos blockPos, BlockPos lookPos) {
        if (lookPos.getX() == blockPos.getX() && lookPos.getZ() == blockPos.getZ()) {
            return OptionalDirection.NONE;
        }

        if (lookPos.getZ() > blockPos.getZ()) {
            return OptionalDirection.SOUTH;
        }

        if (lookPos.getZ() < blockPos.getZ()) {
            return OptionalDirection.NORTH;
        }

        if (lookPos.getX() > blockPos.getX()) {
            return OptionalDirection.EAST;
        }

        return OptionalDirection.WEST;
    }

    private enum OptionalDirection implements StringRepresentable {

        NONE("none", null),
        UP("up", Direction.UP),
        DOWN("down", Direction.DOWN),
        NORTH("north", Direction.NORTH),
        EAST("east", Direction.EAST),
        SOUTH("south", Direction.SOUTH),
        WEST("west", Direction.WEST);

        private static final BiMap<OptionalDirection, Direction> OPT_TO_DIR = HashBiMap.create();
        @Nullable
        private final Direction direction;
        private final String name;

        static {
            for (var value : values()) {
                OPT_TO_DIR.put(value, value.direction);
            }
        }

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

        public BlockPos relative(BlockPos pos) {
            if (direction == null) {
                return pos;
            }

            return pos.relative(direction);
        }

        public void relative(MutableBlockPos pos) {
            if (direction != null) {
                pos.move(direction.getNormal());
            }
        }

        @Nullable
        public Direction getDirection() {
            return direction;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum HULL_TYPE {
        WALL, FRAME
    }

    private static final class OptionalDirectionProperty extends EnumProperty<OptionalDirection> {

        private static final OptionalDirectionProperty HORIZONTAL_PROP = create("horizontal",
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

package com.almostreliable.lazierae2.multiblock;

import com.almostreliable.lazierae2.core.Constants;
import com.almostreliable.lazierae2.core.Setup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class ControllerBlock extends Block implements EntityBlock {
    private static final Logger LOG = LoggerFactory.getLogger(Constants.MOD_ID);
    public static int MIN_SIZE = 5;
    public static int MAX_SIZE = 13;
    public static BooleanProperty CONTROLLER_VALID = BooleanProperty.create("valid");
    public static DirectionProperty FACING = DirectionalBlock.FACING;

    public ControllerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this
            .getStateDefinition()
            .any()
            .setValue(FACING, Direction.NORTH)
            .setValue(CONTROLLER_VALID, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(CONTROLLER_VALID, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(FACING, context.getNearestLookingDirection().getOpposite())
            .setValue(CONTROLLER_VALID, false);
    }

    @Override
    public InteractionResult use(
        BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return super.use(blockState, level, blockPos, player, hand, hit);
        }

        if (!(level.getBlockEntity(blockPos) instanceof ControllerBlockEntity blockEntity)) {
            return InteractionResult.FAIL;
        }

        if (blockEntity.isValid()) {
            return InteractionResult.PASS;
        }

        return tryCreateMultiBlock(blockState, level, blockPos, blockEntity);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (newState.isAir() ||
            (newState.getBlock() instanceof ControllerBlock && !newState.getValue(CONTROLLER_VALID))) {
            if (level.getBlockEntity(blockPos) instanceof ControllerBlockEntity blockEntity) {
                this.destroyMultiBlock(level, blockEntity);
            }
        }

        super.onRemove(oldState, level, blockPos, newState, isMoving);
    }

    private InteractionResult destroyMultiBlock(Level level, ControllerBlockEntity blockEntity) {
        MultiBlock.Data data = blockEntity.getMultiBlockData();
        if (data == null) return InteractionResult.FAIL;
        MultiBlock.iterateMultiBlock(data, (type, currentPos) -> {
            switch (type) {
                case WALL -> {
                    BlockState blockState = level.getBlockState(currentPos);
                    if (!blockState.isAir()) {
                        level.setBlock(currentPos, Setup.Blocks.ASSEMBLER_WALL_BLOCK.get().defaultBlockState(), 2 | 16);
                    }
                }
                case CORNER, EDGE -> {
                    BlockState blockState = level.getBlockState(currentPos);
                    if (!blockState.isAir()) {
                        level.setBlock(currentPos,
                            Setup.Blocks.ASSEMBLER_FRAME_BLOCK.get().defaultBlockState(),
                            2 | 16
                        );
                    }
                }
            }
            return true;
        });
        blockEntity.setMultiBlockData(null);
        level.setBlock(blockEntity.getBlockPos(), blockEntity.getBlockState().setValue(CONTROLLER_VALID, false), 3);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult tryCreateMultiBlock(
        BlockState blockState, Level level, BlockPos blockPos, ControllerBlockEntity blockEntity
    ) {
        MultiBlock.IterateDirections itDirs = MultiBlock.IterateDirections.ofFacing(blockState.getValue(FACING));
        MultiBlock.Data multiBlockData = MultiBlock.Data.of(blockPos,
            itDirs,
            MIN_SIZE,
            MAX_SIZE,
            potentialEdge -> isValidForEdge(level.getBlockState(potentialEdge))
        );

        if (multiBlockData == null) {
            LOG.debug("Could not determine multi block edges or size is incorrect");
            return InteractionResult.FAIL;
        }

        Set<BlockPos> walls = new HashSet<>();
        Set<BlockPos> edges = new HashSet<>();
        boolean result = MultiBlock.iterateMultiBlock(multiBlockData, (type, currentPos) -> {
            if (currentPos.equals(blockPos)) return true;

            BlockState currentBlockState = level.getBlockState(currentPos);
            switch (type) {
                case WALL:
                    if (isValidForWall(currentBlockState)) {
                        walls.add(currentPos);
                        return true;
                    }
                case CORNER, EDGE:
                    if (isValidForEdge(currentBlockState)) {
                        edges.add(currentPos);
                        return true;
                    }
                    break;
                case CENTER:
                    return currentBlockState.getBlock() == Blocks.AIR;
            }
            return false;
        });

        if (!result) {
            LOG.debug("Invalid multi block");
            return InteractionResult.FAIL;
        }

        for (BlockPos wallPos : walls) {
            level.setBlock(wallPos,
                Setup.Blocks.ASSEMBLER_WALL_BLOCK.get().createValidBlockState(wallPos, blockPos),
                2 | 16
            );
        }

        for (BlockPos edgePos : edges) {
            level.setBlock(edgePos,
                Setup.Blocks.ASSEMBLER_FRAME_BLOCK.get().createValidBlockState(edgePos, blockPos),
                2 | 16
            );
        }

        level.setBlock(blockPos, blockState.setValue(CONTROLLER_VALID, true), 3);
        blockEntity.setMultiBlockData(multiBlockData);
        return InteractionResult.SUCCESS;
    }

    public boolean isValidForWall(BlockState blockState) {
        return blockState.getBlock() instanceof AssemblerWallBlock wall && wall.isValid(blockState);
    }

    public boolean isValidForEdge(BlockState blockState) {
        return blockState.getBlock() instanceof AssemblerFrameBlock wall && wall.isValid(blockState);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ControllerBlockEntity(blockPos, blockState);
    }

    public void invalidate(Level level, BlockState blockState, BlockPos blockPos) {
        if (blockState.getBlock() instanceof ControllerBlock && blockState.getValue(CONTROLLER_VALID)) {
            level.setBlock(blockPos, blockState.setValue(CONTROLLER_VALID, false), 3);
        }

        throw new IllegalStateException("Controller is not valid");
    }
}

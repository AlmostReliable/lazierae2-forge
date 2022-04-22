package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.assembler.MultiBlock.Data;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.IterateDirections;
import com.almostreliable.lazierae2.core.Setup.Blocks.Assembler;
import com.almostreliable.lazierae2.core.TypeEnums.HULL_TYPE;
import com.mojang.logging.LogUtils;
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
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class ControllerBlock extends AssemblerBlock implements EntityBlock {

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    static final int MAX_SIZE = 13;
    private static final int MIN_SIZE = 5;
    private static final Logger LOG = LogUtils.getLogger();

    public ControllerBlock() {
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var superState = super.getStateForPlacement(context);
        var state = superState == null ? defaultBlockState() : superState;
        return state.setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if ((newState.isAir() ||
            (newState.getBlock() instanceof ControllerBlock && !newState.getValue(IS_MULTIBLOCK))) &&
            level.getBlockEntity(pos) instanceof ControllerEntity blockEntity) {
            destroyMultiBlock(level, blockEntity);
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

        if (!(level.getBlockEntity(pos) instanceof ControllerEntity blockEntity)) {
            return InteractionResult.FAIL;
        }

        if (blockEntity.isMultiBlockMaster()) {
            return InteractionResult.PASS;
        }

        return tryCreateMultiBlock(state, level, pos, blockEntity);
    }

    public boolean isUsableForWall(BlockState state) {
        return state.getBlock() instanceof HullBlock hull && hull.isUsableForMultiBlock(state) &&
            hull.type == HULL_TYPE.WALL;
    }

    public boolean isUsableForFrame(BlockState state) {
        return state.getBlock() instanceof HullBlock hull && hull.isUsableForMultiBlock(state) &&
            hull.type == HULL_TYPE.FRAME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ControllerEntity(pos, state);
    }

    public void invalidate(Level level, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof ControllerBlock && state.getValue(IS_MULTIBLOCK).equals(true)) {
            level.setBlock(pos, state.setValue(IS_MULTIBLOCK, false), 3);
        }

        throw new IllegalStateException("Controller is not valid");
    }

    private void destroyMultiBlock(Level level, ControllerEntity entity) {
        var data = entity.getMultiBlockData();
        if (data == null) return;
        MultiBlock.iterateMultiBlock(data, (type, pos) -> {
            switch (type) {
                case WALL -> {
                    var blockState = level.getBlockState(pos);
                    if (!blockState.isAir()) {
                        level.setBlock(pos, Assembler.WALL.get().defaultBlockState(), 2 | 16);
                    }
                }
                case CORNER, EDGE -> {
                    var blockState = level.getBlockState(pos);
                    if (!blockState.isAir()) {
                        level.setBlock(pos, Assembler.FRAME.get().defaultBlockState(), 2 | 16);
                    }
                }
            }
            return true;
        });
        entity.setMultiBlockData(null);
        level.setBlock(entity.getBlockPos(), entity.getBlockState().setValue(IS_MULTIBLOCK, false), 3);
    }

    private InteractionResult tryCreateMultiBlock(
        BlockState state, Level level, BlockPos pos, ControllerEntity entity
    ) {
        var itDirs = IterateDirections.ofFacing(state.getValue(FACING));
        var multiBlockData = Data.of(pos,
            itDirs,
            MIN_SIZE,
            MAX_SIZE,
            potentialFrame -> isUsableForFrame(level.getBlockState(potentialFrame))
        );

        if (multiBlockData == null) {
            LOG.debug("Could not determine multi block edges or size is incorrect");
            return InteractionResult.FAIL;
        }

        Set<BlockPos> walls = new HashSet<>();
        Set<BlockPos> edges = new HashSet<>();
        var result = MultiBlock.iterateMultiBlock(multiBlockData, (type, currentPos) -> {
            if (currentPos.equals(pos)) return true;

            var currentBlockState = level.getBlockState(currentPos);
            switch (type) {
                case WALL:
                    if (isUsableForWall(currentBlockState)) {
                        walls.add(currentPos);
                        return true;
                    }
                    break;
                case CORNER, EDGE:
                    if (isUsableForFrame(currentBlockState)) {
                        edges.add(currentPos);
                        return true;
                    }
                    break;
                case CENTER:
                    return currentBlockState.getBlock().equals(Blocks.AIR);
            }
            return false;
        });

        if (!result) {
            LOG.debug("Invalid multi block");
            return InteractionResult.FAIL;
        }

        for (var wallPos : walls) {
            level.setBlock(wallPos, Assembler.WALL.get().createDefaultMultiBlockState(wallPos, pos), 2 | 16);
        }

        for (var edgePos : edges) {
            level.setBlock(edgePos, Assembler.FRAME.get().createDefaultMultiBlockState(edgePos, pos), 2 | 16);
        }

        level.setBlock(pos, state.setValue(IS_MULTIBLOCK, true), 3);
        entity.setMultiBlockData(multiBlockData);
        return InteractionResult.SUCCESS;
    }
}
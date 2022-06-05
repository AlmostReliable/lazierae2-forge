package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.assembler.HullBlock.HULL_TYPE;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.IterateDirections;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.MultiBlockData;
import com.almostreliable.lazierae2.core.Setup.Blocks.Assembler;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class ControllerBlock extends AssemblerBlock implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
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
        if (isMultiBlock(oldState) && newState.isAir() &&
            level.getBlockEntity(pos) instanceof ControllerEntity entity) {
            destroyMultiBlock(level, entity, pos);
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
        if (!(level.getBlockEntity(pos) instanceof ControllerEntity entity)) {
            return InteractionResult.FAIL;
        }

        // TODO: check if the multiblock is already formed and open the gui, otherwise create mb
        if (isMultiBlock(state)) {
            return InteractionResult.PASS;
        }
        return formMultiBlock(state, level, pos, entity);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ControllerEntity(pos, state);
    }

    void destroyMultiBlock(Level level, ControllerEntity entity, BlockPos origin) {
        var data = entity.getMultiBlockData();
        if (data == null) return;

        MultiBlock.iterateMultiBlock(data, (type, pos) -> {
            if (pos.equals(origin)) return true;
            var state = level.getBlockState(pos);
            if (state.isAir()) return true;

            switch (type) {
                case WALL -> level.setBlock(pos, Assembler.WALL.get().defaultBlockState(), 2 | 16);
                case CORNER, EDGE -> level.setBlock(pos, Assembler.FRAME.get().defaultBlockState(), 2 | 16);
                case INNER -> {
                    // TODO: invalidate pattern holders
                }
            }
            return true;
        });

        entity.setMultiBlockData(null);
        level.setBlock(entity.getBlockPos(), entity.getBlockState().setValue(GenericBlock.ACTIVE, false), 3);
    }

    private InteractionResult formMultiBlock(
        BlockState state, Level level, BlockPos pos, ControllerEntity entity
    ) {
        var itDirs = IterateDirections.of(state.getValue(FACING));
        var multiBlockData = MultiBlockData.of(pos,
            itDirs,
            potentialFrame -> HULL_TYPE.FRAME.isValid(level.getBlockState(potentialFrame))
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
                    if (HULL_TYPE.WALL.isValid(currentBlockState)) {
                        walls.add(currentPos);
                        return true;
                    }
                    break;
                case CORNER, EDGE:
                    if (HULL_TYPE.FRAME.isValid(currentBlockState)) {
                        edges.add(currentPos);
                        return true;
                    }
                    break;
                case INNER:
                    return currentBlockState.getBlock().equals(Blocks.AIR);
            }
            return false;
        });

        if (!result) {
            LOG.debug("Invalid multi block");
            return InteractionResult.FAIL;
        }

        for (var wallPos : walls) {
            level.setBlock(wallPos, Assembler.WALL.get().createMultiBlockState(wallPos, pos), 2 | 16);
        }

        for (var edgePos : edges) {
            level.setBlock(edgePos, Assembler.FRAME.get().createMultiBlockState(edgePos, pos), 2 | 16);
        }

        level.setBlock(pos, state.setValue(GenericBlock.ACTIVE, true), 3);
        entity.setMultiBlockData(multiBlockData);
        return InteractionResult.SUCCESS;
    }
}

package com.almostreliable.lazierae2.content.assembler.controller;

import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.assembler.AssemblerBlock;
import com.almostreliable.lazierae2.content.assembler.HullBlock.HULL_TYPE;
import com.almostreliable.lazierae2.content.assembler.MultiBlock;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.IterateDirections;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.MultiBlockData;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.PositionType;
import com.almostreliable.lazierae2.content.assembler.holder.PatternHolderBlock;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

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
        if ((level.getBlockEntity(pos) instanceof ControllerEntity entity) &&
            (isMultiBlock(state) || formMultiBlock(state.getValue(FACING), level, pos, entity))) {
            // TODO: open GUI
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ControllerEntity(pos, state);
    }

    @Override
    public boolean isValidMultiBlockPos(PositionType posType) {
        return posType == PositionType.WALL;
    }

    @Override
    public BlockState setupMultiBlockState(BlockState state, BlockPos hullPos, BlockPos controllerPos) {
        return super.setupMultiBlockState(state, hullPos, controllerPos).setValue(FACING, state.getValue(FACING));
    }

    public void destroyMultiBlock(Level level, ControllerEntity controller, BlockPos origin) {
        var multiBlockData = controller.getMultiBlockData();
        if (multiBlockData == null) return;

        Map<BlockPos, AssemblerBlock> destroyData = new HashMap<>();
        MultiBlock.iterateMultiBlock(multiBlockData, (posType, pos) -> {
            var state = level.getBlockState(pos);
            if (pos.equals(origin) || pos.equals(controller.getBlockPos()) || state.isAir()) return true;
            if (state.getBlock() instanceof AssemblerBlock block) {
                destroyData.put(pos, block);
            } else {
                LOG.warn("Block at {} is not an AssemblerBlock", pos);
            }
            return true;
        });

        for (var data : destroyData.entrySet()) {
            var updateFlags = (data.getValue() instanceof EntityBlock ? 1 : 16) | 2;
            level.setBlock(
                data.getKey(),
                data.getValue().defaultBlockState().setValue(GenericBlock.ACTIVE, false),
                updateFlags
            );
        }
        controller.setMultiBlockData(null);
        if (controller.getBlockPos().equals(origin)) return;
        level.setBlock(controller.getBlockPos(), controller.getBlockState().setValue(GenericBlock.ACTIVE, false), 3);
    }

    private boolean formMultiBlock(Direction facing, Level level, BlockPos pos, ControllerEntity controller) {
        var multiBlockData = MultiBlockData.of(
            pos,
            IterateDirections.of(facing),
            potentialFrame -> HULL_TYPE.FRAME.validForMultiBlock(level.getBlockState(potentialFrame))
        );
        if (multiBlockData == null) {
            LOG.debug("Could not determine multi block edges or size is incorrect");
            return false;
        }

        Map<BlockPos, AssemblerBlock> formData = new HashMap<>();
        var result = MultiBlock.iterateMultiBlock(multiBlockData, (posType, currentPos) -> {
            var currentState = level.getBlockState(currentPos);
            if (currentState.isAir()) {
                return posType == PositionType.INNER;
            }
            if (currentState.getBlock() instanceof AssemblerBlock block &&
                !block.isMultiBlock(currentState) && block.isValidMultiBlockPos(posType)) {
                formData.put(currentPos, block);
                if (block instanceof PatternHolderBlock) {
                    controller.controllerData.addHolder(currentPos);
                }
                return true;
            }
            return false;
        });
        if (!result) {
            LOG.debug("Invalid multi block");
            return false;
        }

        for (var data : formData.entrySet()) {
            var updateFlags = (data.getValue() instanceof EntityBlock ? 1 : 16) | 2;
            var currentState = level.getBlockState(data.getKey());
            level.setBlock(
                data.getKey(),
                data.getValue().setupMultiBlockState(currentState, data.getKey(), pos),
                updateFlags
            );
        }
        controller.setMultiBlockData(multiBlockData);
        return true;
    }
}

package com.almostreliable.lazierae2.content.assembler.controller;

import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.assembler.AssemblerBlock;
import com.almostreliable.lazierae2.content.assembler.HullBlock.HULL_TYPE;
import com.almostreliable.lazierae2.content.assembler.MultiBlock;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.IterateDirections;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.MultiBlockData;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.PositionType;
import com.almostreliable.lazierae2.content.assembler.PatternHolderBlock;
import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.util.GameUtil;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerBlock extends GenericBlock implements EntityBlock {

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
        if (AssemblerBlock.isMultiBlock(oldState) && newState.isAir() &&
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
        if (level.isClientSide || hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return super.use(state, level, pos, player, hand, hit);
        }
        if (level.getBlockEntity(pos) instanceof ControllerEntity controller) {
            if (AssemblerBlock.isMultiBlock(state)) {
                return GenericBlock.openScreen(level, pos, player);
            }
            return formMultiBlock(state.getValue(FACING), level, pos, controller, player);
        }
        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ControllerEntity(pos, state);
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
            level.setBlock(
                data.getKey(),
                data.getValue().defaultBlockState().setValue(GenericBlock.ACTIVE, false),
                2 | 16
            );
        }
        if (controller.getBlockPos().equals(origin)) return;
        controller.setMultiBlockData(null);
        level.setBlock(
            controller.getBlockPos(),
            controller.getBlockState().setValue(GenericBlock.ACTIVE, false),
            1 | 2
        );
    }

    private InteractionResult formMultiBlock(
        Direction facing, Level level, BlockPos pos, ControllerEntity controller, Player player
    ) {
        var multiBlockData = MultiBlockData.of(
            pos,
            IterateDirections.of(facing),
            potentialFrame -> HULL_TYPE.FRAME.validForMultiBlock(level.getBlockState(potentialFrame)),
            potentialHull -> HULL_TYPE.WALL.validForMultiBlock(level.getBlockState(potentialHull)), player
        );
        if (multiBlockData == null) {
            return InteractionResult.FAIL;
        }

        Map<BlockPos, AssemblerBlock> formData = new HashMap<>();
        var result = MultiBlock.iterateMultiBlock(multiBlockData, (posType, currentPos) -> {
            if (currentPos.equals(pos)) return true;
            var currentState = level.getBlockState(currentPos);

            if (currentState.isAir()) {
                var check = posType == PositionType.INNER;
                if (check) return true;
                GameUtil.sendPlayerMessage(player, "invalid_air", ChatFormatting.AQUA, currentPos.toShortString());
                LOG.debug("Invalid air at {}", currentPos);
                return false;
            }

            if (currentState.getBlock() instanceof AssemblerBlock block) {
                if (!AssemblerBlock.isMultiBlock(currentState) && block.isValidMultiBlockPos(posType)) {
                    formData.put(currentPos, block);
                    return true;
                }
                GameUtil.sendPlayerMessage(
                    player,
                    "invalid_pos",
                    ChatFormatting.AQUA,
                    block.getName(),
                    currentPos.toShortString()
                );
                LOG.debug("Invalid pos for assembler block at {}", currentPos);
                return false;
            }

            GameUtil.sendPlayerMessage(
                player,
                "invalid_block",
                ChatFormatting.AQUA,
                currentState.getBlock().getName(),
                currentPos.toShortString()
            );
            LOG.debug("Invalid block {} at {}", currentState.getBlock().getName(), currentPos);
            return false;
        });

        if (!result) {
            GameUtil.sendPlayerMessage(
                player,
                "malformed",
                ChatFormatting.DARK_RED,
                TextUtil.translateAsString(TRANSLATE_TYPE.GUI, "controller")
            );
            return InteractionResult.FAIL;
        }

        List<PatternHolderBlock> patternHolders = new ArrayList<>();
        for (var data : formData.entrySet()) {
            if (data.getValue() instanceof PatternHolderBlock patternBlock) {
                patternHolders.add(patternBlock);
            }
            var currentState = level.getBlockState(data.getKey());
            level.setBlock(
                data.getKey(),
                data.getValue().setupMultiBlockState(currentState, data.getKey(), pos),
                2 | 16
            );
        }
        controller.controllerData.setHolders(patternHolders);
        controller.setMultiBlockData(multiBlockData);
        return InteractionResult.CONSUME;
    }
}

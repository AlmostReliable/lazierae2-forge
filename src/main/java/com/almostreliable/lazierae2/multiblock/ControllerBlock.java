package com.almostreliable.lazierae2.multiblock;

import com.almostreliable.lazierae2.core.Setup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
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

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class ControllerBlock extends Block implements EntityBlock {
    public static int MIN_SIZE = 5;
    public static int MAX_SIZE = 13;
    public static BooleanProperty VALID = BooleanProperty.create("valid");
    public static DirectionProperty FACING = DirectionalBlock.FACING;

    public ControllerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this
            .getStateDefinition()
            .any()
            .setValue(FACING, Direction.NORTH)
            .setValue(VALID, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(VALID, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(FACING, context.getNearestLookingDirection().getOpposite())
            .setValue(VALID, false);
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

        MultiBlock.IterateDirections itDirs = MultiBlock.IterateDirections.ofFacing(blockState.getValue(FACING));
        MultiBlock.Data multiBlockData = MultiBlock.Data.of(blockPos,
            itDirs,
            MIN_SIZE,
            MAX_SIZE,
            potentialEdge -> isValidForEdge(level.getBlockState(potentialEdge))
        );

        if (multiBlockData == null) {
            player.sendMessage(new TextComponent("Could not determine multi block edges or size is incorrect"),
                player.getUUID()
            );
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
            player.sendMessage(new TextComponent("Invalid multiblock"), player.getUUID());
            return InteractionResult.FAIL;
        }

        for (BlockPos wallPos : walls) {
            level.setBlock(wallPos,
                Setup.Blocks.VALID_WALL_BLOCK.get().createValidBlockState(wallPos, blockPos),
                2 | 16,
                1
            );
        }

        for (BlockPos edgePos : edges) {
            level.setBlock(edgePos,
                Setup.Blocks.VALID_EDGE_BLOCK.get().createValidBlockState(edgePos, blockPos),
                2 | 16,
                1
            );
        }

        level.setBlock(blockPos, blockState.setValue(VALID, true), 3);
        blockEntity.setMultiBlockData(multiBlockData);

        player.sendMessage(new TextComponent("Valid multiblock"), player.getUUID());
        return InteractionResult.CONSUME;
    }

    public boolean isValidForWall(BlockState blockState) {
        return blockState.getBlock() == Blocks.GOLD_BLOCK;
    }

    public boolean isValidForEdge(BlockState blockState) {
        return blockState.getBlock() == Blocks.DIAMOND_BLOCK;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ControllerBlockEntity(blockPos, blockState);
    }
}

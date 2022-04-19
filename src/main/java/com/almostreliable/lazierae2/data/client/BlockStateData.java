package com.almostreliable.lazierae2.data.client;

import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.MachineBlock;
import com.almostreliable.lazierae2.content.assembler.AssemblerBlock;
import com.almostreliable.lazierae2.content.assembler.ControllerBlock;
import com.almostreliable.lazierae2.content.assembler.HullBlock;
import com.almostreliable.lazierae2.content.processor.ProcessorBlock;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Blocks.Assembler;
import net.minecraft.core.Direction.Axis;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Function;

import static com.almostreliable.lazierae2.core.Constants.*;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class BlockStateData extends BlockStateProvider {

    // TODO: refactor

    public BlockStateData(
        DataGenerator gen, ExistingFileHelper fileHelper
    ) {
        super(gen, MOD_ID, fileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        registerProcessor(Blocks.AGGREGATOR.get());
        registerProcessor(Blocks.CENTRIFUGE.get());
        registerProcessor(Blocks.ENERGIZER.get());
        registerProcessor(Blocks.ETCHER.get());
        registerAssembler(CONTROLLER_ID, Assembler.CONTROLLER.get());
        registerAssembler(WALL_ID, Assembler.WALL.get());
        registerAssembler(FRAME_ID, Assembler.FRAME.get());
        registerMachine(MAINTAINER_ID, Blocks.MAINTAINER.get());
    }

    /**
     * Handles the registration of the processor blockstate data.
     *
     * @param block the processor block to register
     */
    private void registerProcessor(ProcessorBlock block) {
        registerMachine(block.getId(), block);
    }

    /**
     * Handles the registration of the machine blockstate data.
     * <p>
     * Creates a blockstate with the same texture on all sides except the facing direction.
     * The texture of the facing direction depends on the current value of the ACTIVE blockstate.
     *
     * @param id    the id of the block
     * @param block the machine block to register
     */
    private void registerMachine(String id, MachineBlock block) {
        var sideTexture = new ResourceLocation(MOD_ID, "block/machine/side");
        var inactiveTexture = new ResourceLocation(MOD_ID, f("block/machine/{}", id));
        var activeTexture = new ResourceLocation(MOD_ID, f("block/machine/{}_active", id));

        var modelInactive = models().orientable(id, sideTexture, inactiveTexture, sideTexture);
        var modelActive = models().orientable(getActiveId(id), sideTexture, activeTexture, sideTexture);

        orientedBlock(block,
            MachineBlock.FACING,
            state -> state.getValue(GenericBlock.ACTIVE).equals(Boolean.TRUE) ? modelActive : modelInactive
        );
    }

    private void registerAssembler(String id, GenericBlock block) {
        var inactiveTexture = new ResourceLocation(MOD_ID, f("block/assembler/{}", id));
        var activeTexture = new ResourceLocation(MOD_ID, f("block/assembler/{}_active", id));
        BlockModelBuilder modelInactive;
        BlockModelBuilder modelActive;
        if (block instanceof AssemblerBlock) {
            modelInactive = models().cubeAll(id, inactiveTexture);
            modelActive = models().cubeAll(getActiveId(id), activeTexture);
            stateBlock(block,
                state -> state.getValue(GenericBlock.ACTIVE).equals(Boolean.TRUE) ? modelActive : modelInactive
            );
        } else {
            var sideTexture = new ResourceLocation(MOD_ID, f("block/assembler/{}", WALL_ID));
            modelInactive = models().orientable(id, sideTexture, inactiveTexture, sideTexture);
            modelActive = models().orientable(getActiveId(id), sideTexture, activeTexture, sideTexture);
            orientedBlock(block,
                ControllerBlock.FACING,
                state -> state.getValue(GenericBlock.ACTIVE).equals(Boolean.TRUE) ? modelActive : modelInactive,
                AssemblerBlock.VALID
            );
        }
    }

    /**
     * Generates the blockstate variants depending on the facing direction of the block.
     *
     * @param block         the processor block to generate the variants for
     * @param modelFunction the function to get the correct model file
     */
    private void orientedBlock(
        Block block, DirectionProperty facingProp, Function<? super BlockState, ? extends ModelFile> modelFunction,
        Property<?>... ignored
    ) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            var facing = state.getValue(facingProp);

            return ConfiguredModel
                .builder()
                .modelFile(modelFunction.apply(state))
                .rotationX(facing.getAxis() == Axis.Y ? facing.getAxisDirection().getStep() * -90 : 0)
                .rotationY(facing.getAxis() == Axis.Y ? 0 : ((facing.get2DDataValue() + 2) % 4) * 90)
                .build();
        }, ignored);
    }

    private void stateBlock(Block block, Function<? super BlockState, ? extends ModelFile> modelFunction) {
        getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel
            .builder()
            .modelFile(modelFunction.apply(state))
            .build(), AssemblerBlock.VALID, HullBlock.HORIZONTAL, HullBlock.VERTICAL);
    }

    private String getActiveId(String id) {
        return f("{}_active", id);
    }
}

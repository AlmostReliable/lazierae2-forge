package com.almostreliable.lazierae2.data.client;

import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.processor.ProcessorBlock;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import net.minecraft.core.Direction.Axis;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Function;

import static com.almostreliable.lazierae2.core.Constants.MAINTAINER_ID;
import static com.almostreliable.lazierae2.core.Constants.MOD_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class BlockStateData extends BlockStateProvider {

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
        registerBlock(MAINTAINER_ID, Blocks.MAINTAINER.get());
    }

    /**
     * Handles the registration of the processor blockstate data.
     *
     * @param block the processor block to register
     */
    private void registerProcessor(ProcessorBlock block) {
        registerBlock(block.getId(), block);
    }

    /**
     * Handles the registration of the generic blockstate data.
     * <p>
     * Creates a blockstate with the same texture on all sides except the facing direction.
     * The texture of the facing direction depends on the current value of the ACTIVE blockstate.
     *
     * @param id    the id of the block
     * @param block the generic block to register
     */
    private void registerBlock(String id, GenericBlock block) {
        var sideTexture = new ResourceLocation(MOD_ID, "block/processor");
        var inactiveTexture = new ResourceLocation(MOD_ID, f("block/{}", id));
        var activeTexture = new ResourceLocation(MOD_ID, f("block/{}_active", id));

        var modelInactive = models().orientable(id, sideTexture, inactiveTexture, sideTexture);
        var modelActive = models().orientable(f("{}_active", id), sideTexture, activeTexture, sideTexture);

        orientedBlock(block,
            state -> state.getValue(GenericBlock.ACTIVE).equals(Boolean.TRUE) ? modelActive : modelInactive
        );
    }

    /**
     * Generates the blockstate variants depending on the facing direction of the block.
     *
     * @param block         the processor block to generate the variants for
     * @param modelFunction the function to get the correct model file
     */
    private void orientedBlock(
        Block block, Function<? super BlockState, ? extends ModelFile> modelFunction
    ) {
        getVariantBuilder(block).forAllStates(state -> {
            var facing = state.getValue(GenericBlock.FACING);

            return ConfiguredModel
                .builder()
                .modelFile(modelFunction.apply(state))
                .rotationX(facing.getAxis() == Axis.Y ? facing.getAxisDirection().getStep() * -90 : 0)
                .rotationY(facing.getAxis() == Axis.Y ? 0 : ((facing.get2DDataValue() + 2) % 4) * 90)
                .build();
        });
    }
}

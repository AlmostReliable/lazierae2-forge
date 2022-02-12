package com.github.almostreliable.lazierae2.data.client;

import com.github.almostreliable.lazierae2.core.Setup.Blocks;
import com.github.almostreliable.lazierae2.machine.MachineBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Function;

import static com.github.almostreliable.lazierae2.core.Constants.MOD_ID;

public class BlockStateData extends BlockStateProvider {

    public BlockStateData(
        DataGenerator gen, ExistingFileHelper fileHelper
    ) {
        super(gen, MOD_ID, fileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        registerMachine(Blocks.AGGREGATOR.get());
        registerMachine(Blocks.CENTRIFUGE.get());
        registerMachine(Blocks.ENERGIZER.get());
        registerMachine(Blocks.ETCHER.get());
    }

    /**
     * Handles the registration of the machine blocks.
     * <p>
     * Creates a block with the same texture on all sides except the facing direction.
     * The texture of the facing direction depends on the current value of the ACTIVE blockstate.
     *
     * @param block the machine block to register
     */
    private void registerMachine(MachineBlock block) {
        String id = block.getId();
        ResourceLocation sideTexture = new ResourceLocation(MOD_ID, "block/machine");
        ResourceLocation inactiveTexture = new ResourceLocation(MOD_ID, "block/" + id);
        ResourceLocation activeTexture = new ResourceLocation(MOD_ID, "block/" + id + "_active");

        BlockModelBuilder modelInactive = models().orientable(id, sideTexture, inactiveTexture, sideTexture);
        BlockModelBuilder modelActive = models().orientable(id + "_active", sideTexture, activeTexture, sideTexture);

        orientedBlock(block,
            state -> state.getValue(MachineBlock.ACTIVE).equals(Boolean.TRUE) ? modelActive : modelInactive
        );
    }

    /**
     * Generates the blockstate variants depending on the facing direction of the block.
     *
     * @param block         the machine block to generate the variants for
     * @param modelFunction the function to get the correct model file
     */
    private void orientedBlock(
        Block block, Function<? super BlockState, ? extends ModelFile> modelFunction
    ) {
        getVariantBuilder(block).forAllStates(state -> {
            Direction facing = state.getValue(MachineBlock.FACING);

            return ConfiguredModel
                .builder()
                .modelFile(modelFunction.apply(state))
                .rotationX(facing.getAxis() == Axis.Y ? facing.getAxisDirection().getStep() * -90 : 0)
                .rotationY(facing.getAxis() == Axis.Y ? 0 : ((facing.get2DDataValue() + 2) % 4) * 90)
                .build();
        });
    }
}

package com.github.almostreliable.lazierae2.data.client;

import com.github.almostreliable.lazierae2.block.MachineBlock;
import com.github.almostreliable.lazierae2.core.Setup.Blocks;
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
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Function;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public class BlockStateData extends BlockStateProvider {

    public BlockStateData(
        DataGenerator gen, ExistingFileHelper filderHelper
    ) {
        super(gen, MOD_ID, filderHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        registerMachine(AGGREGATOR_ID, Blocks.AGGREGATOR);
        registerMachine(CENTRIFUGE_ID, Blocks.CENTRIFUGE);
        registerMachine(ENERGIZER_ID, Blocks.ENERGIZER);
        registerMachine(ETCHER_ID, Blocks.ETCHER);
    }

    /**
     * Handles the registration of the machine blocks.
     * <p>
     * Creates a block with the same texture on all sides except the facing direction.
     * The texture of the facing direction depends on the current value of the ACTIVE blockstate.
     *
     * @param block the machine block to register
     * @param <B>   the type of the machine block
     */
    private <B extends MachineBlock> void registerMachine(String id, RegistryObject<B> block) {
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
     * @param <B>           the type of the machine block
     */
    private <B extends MachineBlock> void orientedBlock(
        RegistryObject<B> block, Function<? super BlockState, ? extends ModelFile> modelFunction
    ) {
        getVariantBuilder(block.get()).forAllStates(state -> {
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

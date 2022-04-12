package com.almostreliable.lazierae2.data.client;

import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.machine.MachineBlock;
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
        registerMachine(Blocks.AGGREGATOR.get());
        registerMachine(Blocks.CENTRIFUGE.get());
        registerMachine(Blocks.ENERGIZER.get());
        registerMachine(Blocks.ETCHER.get());
    }

    /**
     * Handles the registration of the machine blockstate data.
     * <p>
     * Creates a blockstate with the same texture on all sides except the facing direction.
     * The texture of the facing direction depends on the current value of the ACTIVE blockstate.
     *
     * @param block the machine block to register
     */
    private void registerMachine(MachineBlock block) {
        var id = block.getId();
        var sideTexture = new ResourceLocation(MOD_ID, "block/machine");
        var inactiveTexture = new ResourceLocation(MOD_ID, f("block/{}", id));
        var activeTexture = new ResourceLocation(MOD_ID, f("block/{}_active", id));

        var modelInactive = models().orientable(id, sideTexture, inactiveTexture, sideTexture);
        var modelActive = models().orientable(f("{}_active", id), sideTexture, activeTexture, sideTexture);

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
            var facing = state.getValue(MachineBlock.FACING);

            return ConfiguredModel
                .builder()
                .modelFile(modelFunction.apply(state))
                .rotationX(facing.getAxis() == Axis.Y ? facing.getAxisDirection().getStep() * -90 : 0)
                .rotationY(facing.getAxis() == Axis.Y ? 0 : ((facing.get2DDataValue() + 2) % 4) * 90)
                .build();
        });
    }
}

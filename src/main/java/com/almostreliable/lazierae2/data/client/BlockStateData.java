package com.almostreliable.lazierae2.data.client;

import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.processor.ProcessorBlock;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.util.GameUtil;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.core.Direction.Axis;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
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
        machineNoModel(Blocks.AGGREGATOR.get());
        machine(Blocks.ETCHER.get());
        machine(Blocks.GRINDER.get());
        machineNoModel(Blocks.INFUSER.get());
        machine(Blocks.REQUESTER.get());
    }

    private void machine(GenericBlock block) {
        var id = GameUtil.getIdFromBlock(block);
        var wall = TextUtil.getRL("block/machine/wall");
        var top = TextUtil.getRL("block/machine/top");
        var inactive = TextUtil.getRL(f("block/machine/{}", id));
        var active = TextUtil.getRL(f("block/machine/{}", formActiveId(id)));
        var modelInactive = models().orientableWithBottom(id, wall, inactive, top, top);
        var modelActive = models().orientableWithBottom(formActiveId(id), wall, active, top, top);
        orientedBlock(
            block,
            state -> state.getValue(GenericBlock.ACTIVE).equals(Boolean.TRUE) ? modelActive : modelInactive
        );
    }

    private void machineNoModel(ProcessorBlock block) {
        var id = GameUtil.getIdFromBlock(block);
        var modelInactive = TextUtil.getRL(f("block/{}", id));
        var modelActive = TextUtil.getRL(f("block/{}", formActiveId(id)));
        orientedBlock(
            block,
            state -> new UncheckedModelFile(
                state.getValue(GenericBlock.ACTIVE).equals(Boolean.TRUE) ? modelActive : modelInactive)
        );
    }

    private void orientedBlock(
        Block block, Function<? super BlockState, ? extends ModelFile> modelFunction
    ) {
        getVariantBuilder(block).forAllStates(state -> {
            var facing = state.getValue(GenericBlock.FACING);

            return ConfiguredModel.builder()
                .modelFile(modelFunction.apply(state))
                .rotationX(facing.getAxis() == Axis.Y ? facing.getAxisDirection().getStep() * -90 : 0)
                .rotationY(facing.getAxis() == Axis.Y ? 0 : ((facing.get2DDataValue() + 2) % 4) * 90)
                .build();
        });
    }

    private String formActiveId(String id) {
        return f("{}_active", id);
    }
}

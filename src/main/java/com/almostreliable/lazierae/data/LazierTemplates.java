package com.almostreliable.lazierae.data;

import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import com.almostreliable.lazierae.block.MachineBlock;
import com.almostreliable.lib.datagen.provider.BlockStateProvider;
import com.almostreliable.lib.datagen.provider.ItemModelProvider;
import com.almostreliable.lib.registry.BlockEntry;
import com.almostreliable.lib.registry.RegistryEntry;
import com.almostreliable.lib.util.Rotation;

public final class LazierTemplates {

    private LazierTemplates() {
    }

    // ///////////////
    // Model Templates
    // ///////////////

    public static <T extends BlockItem> void simpleItemModelFromBlock(
        RegistryEntry<T> entry, ItemModelProvider provider
    ) {
        ResourceLocation resourceLocation = entry.getKey().location();
        ModelTemplates.FLAT_HANDHELD_ITEM.create(
            ModelLocationUtils.getModelLocation(entry.get()),
            TextureMapping.layer0(new ResourceLocation(resourceLocation.getNamespace(), "block/" + resourceLocation.getPath())),
            provider.getModelConsumer()
        );
    }

    // ////////////////////
    // BlockState Templates
    // ////////////////////

    public static <T extends Block> void crystalClusterBlockState(BlockEntry<T> entry, BlockStateProvider provider) {
        Block block = entry.get();

        var mapping = new TextureMapping().put(TextureSlot.CROSS, TextureMapping.getBlockTexture(block));
        ResourceLocation crossModel = ModelTemplates.CROSS.create(block, mapping, provider.getModelConsumer());

        provider.createVariantExcept(
            block,
            model -> model.model(crossModel).xRotation(switch (model.getValue(BlockStateProperties.FACING)) {
                case UP -> Rotation.R0;
                case DOWN -> Rotation.R180;
                default -> Rotation.R90;
            }).yRotation(switch (model.getValue(BlockStateProperties.FACING)) {
                case SOUTH -> Rotation.R180;
                case EAST -> Rotation.R90;
                case WEST -> Rotation.R270;
                default -> Rotation.R0;
            }),
            BlockStateProperties.WATERLOGGED
        );
    }

    public static <B extends Block> void injectorBlockState(BlockEntry<B> entry, BlockStateProvider provider) {
        Block block = entry.get();
        var mappingInactive = new TextureMapping().put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "/top"))
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "/bottom"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "/side"));
        var mappingActive = new TextureMapping().put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "/top_active"))
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "/bottom_active"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "/side_active"));

        ResourceLocation modelInactive = ModelTemplates.CUBE_BOTTOM_TOP.create(block, mappingInactive, provider.getModelConsumer());
        ResourceLocation modelActive = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(
            block,
            "_active",
            mappingActive,
            provider.getModelConsumer()
        );

        provider.createVariantFor(
            block,
            model -> model.model(model.getValue(MachineBlock.ACTIVE) ? modelActive : modelInactive),
            MachineBlock.ACTIVE
        );
    }

    public static <B extends Block> void machineBlockState(BlockEntry<B> entry, BlockStateProvider provider) {
        Block block = entry.get();
        var mappingInactive = new TextureMapping().put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "/top"))
            .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "/front"))
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "/bottom"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "/side"));
        var mappingActive = new TextureMapping().put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "/top_active"))
            .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "/front_active"))
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "/bottom_active"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "/side_active"));

        ResourceLocation modelInactive = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(
            block,
            mappingInactive,
            provider.getModelConsumer()
        );
        ResourceLocation modelActive = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(
            block,
            "_active",
            mappingActive,
            provider.getModelConsumer()
        );

        provider.createVariantFor(
            block,
            model -> model.model(model.getValue(MachineBlock.ACTIVE) ? modelActive : modelInactive),
            MachineBlock.ACTIVE
        );
    }
}

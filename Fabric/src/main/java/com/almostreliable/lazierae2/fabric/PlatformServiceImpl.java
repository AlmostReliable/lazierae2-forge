package com.almostreliable.lazierae2.fabric;

import appeng.block.AEBaseBlock;
import com.almostreliable.lazierae2.PlatformService;
import com.google.auto.service.AutoService;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

@AutoService(PlatformService.class)
public class PlatformServiceImpl implements PlatformService {

    @Override
    public Platform getPlatform() {
        return Platform.FABRIC;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public BlockBehaviour.Properties defaultBlockProps(Material material) {
        return AEBaseBlock.defaultProps(material);
    }

    @Override
    public BlockBehaviour.Properties defaultBlockProps(Material material, MaterialColor color) {
        return AEBaseBlock.defaultProps(material, color);
    }
}

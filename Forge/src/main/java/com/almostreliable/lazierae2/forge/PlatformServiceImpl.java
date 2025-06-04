package com.almostreliable.lazierae2.forge;

import appeng.block.AEBaseBlock;
import com.almostreliable.lazierae2.PlatformService;
import com.google.auto.service.AutoService;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

@AutoService(PlatformService.class)
public class PlatformServiceImpl implements PlatformService {

    @Override
    public Platform getPlatform() {
        return Platform.FORGE;
    }

    @Override
    public boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
        }
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
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

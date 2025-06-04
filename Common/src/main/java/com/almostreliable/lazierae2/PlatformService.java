package com.almostreliable.lazierae2;

import com.almostreliable.lib.AlmostLib;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public interface PlatformService {

    PlatformService INSTANCE = AlmostLib.loadService(PlatformService.class);

    /**
     * Gets the current platform.
     *
     * @return The current platform.
     */
    Platform getPlatform();

    /**
     * Checks whether a mod with the given id is loaded.
     *
     * @param modId The mod id to check.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the default block properties for the current platform.
     *
     * @param material The material of the block.
     * @return The default block properties.
     */
    BlockBehaviour.Properties defaultBlockProps(Material material);

    /**
     * Gets the default block properties for the current platform.
     *
     * @param material The material of the block.
     * @param color    The color of the block.
     * @return The default block properties.
     */
    BlockBehaviour.Properties defaultBlockProps(Material material, MaterialColor color);

    enum Platform {
        FORGE,
        FABRIC
    }
}

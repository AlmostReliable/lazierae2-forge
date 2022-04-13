package com.almostreliable.lazierae2.multiblock;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RenderLevelLastEvent;

import java.util.OptionalDouble;

public class DebugControllerValid {

    public static BlockPos controllerBlockPos = null;

    public static void handle(RenderLevelLastEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            return;
        }

        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!itemStack.getItem().equals(Items.GOLDEN_APPLE)) {
            return;
        }
    }

    // Extend to use the render states which are protected instead of using AT
    public static class DebugNodeRenderType extends RenderType {

        public static final RenderType DEBUG_LINE = RenderType.create(
            "debug_node_line",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.LINES,
            256,
            false,
            false,
            RenderType.CompositeState
                .builder()
                .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(6f)))
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setTextureState(NO_TEXTURE)
                .setDepthTestState(NO_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false)
        );

        public DebugNodeRenderType(
            String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling,
            boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState
        ) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
        }
    }
}

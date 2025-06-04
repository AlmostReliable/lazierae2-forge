package com.almostreliable.lazierae.client.renderer;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.almostreliable.lazierae.LazierAE2Constants;
import com.almostreliable.lib.block.io.IoSetting;
import com.almostreliable.lib.block.io.SideConfigurable;
import com.almostreliable.lib.block.io.SideConfiguration;
import com.almostreliable.lib.client.gui.widget.AlmostWidget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class SideConfigRenderer<T extends BlockEntity & SideConfigurable> implements BlockEntityRenderer<T> {

    private static final ResourceLocation IO_TEXTURE = AlmostWidget.getTexture(LazierAE2Constants.MOD_ID, "overlay/io");
    private static final int TEXTURE_WIDTH = 16;
    private static final int TEXTURE_HEIGHT = 48;

    public SideConfigRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
        T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay
    ) {
        Direction facing = blockEntity.getFacing();
        Direction top = blockEntity.getTop();
        SideConfiguration sideConfig = blockEntity.getSideConfiguration();

        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, IO_TEXTURE);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        for (Direction dir : Direction.values()) {
            if (dir == facing) continue;
            if (top == null && (dir == Direction.UP || dir == Direction.DOWN)) continue;

            IoSetting setting = sideConfig.get(dir);
            if (setting == IoSetting.OFF) continue;

            poseStack.pushPose();
            // move a slight amount towards the direction to avoid z-fighting
            poseStack.translate(dir.getStepX() * 0.001, dir.getStepY() * 0.001, dir.getStepZ() * 0.001);

            Vector3f[] vertices = getFaceVertices(dir);
            float[] vOffset = getVFromSetting(setting);
            Matrix4f matrix = poseStack.last().pose();
            bufferBuilder.vertex(matrix, vertices[0].x(), vertices[0].y(), vertices[0].z())
                .uv(0, vOffset[0])
                .endVertex();
            bufferBuilder.vertex(matrix, vertices[1].x(), vertices[1].y(), vertices[1].z())
                .uv(0, vOffset[1])
                .endVertex();
            bufferBuilder.vertex(matrix, vertices[2].x(), vertices[2].y(), vertices[2].z())
                .uv(1, vOffset[1])
                .endVertex();
            bufferBuilder.vertex(matrix, vertices[3].x(), vertices[3].y(), vertices[3].z())
                .uv(1, vOffset[0])
                .endVertex();

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableDepthTest();

        poseStack.popPose();
    }

    private float[] getVFromSetting(IoSetting setting) {
        switch (setting) {
            case IN -> {
                return new float[]{0, 1 / 3f};
            }
            case OUT -> {
                return new float[]{1 / 3f, 2 / 3f};
            }
            case IO -> {
                return new float[]{2 / 3f, 1};
            }
            default -> throw new IllegalStateException("Unexpected value: " + setting);
        }
    }

    private Vector3f[] getFaceVertices(Direction face) {
        Vector3f[] vertices = new Vector3f[4];
        switch (face) {
            case DOWN -> {
                vertices[0] = new Vector3f(0, 0, 0);
                vertices[1] = new Vector3f(1, 0, 0);
                vertices[2] = new Vector3f(1, 0, 1);
                vertices[3] = new Vector3f(0, 0, 1);
            }
            case UP -> {
                vertices[0] = new Vector3f(0, 1, 1);
                vertices[1] = new Vector3f(1, 1, 1);
                vertices[2] = new Vector3f(1, 1, 0);
                vertices[3] = new Vector3f(0, 1, 0);
            }
            case NORTH -> {
                vertices[0] = new Vector3f(0, 1, 0);
                vertices[1] = new Vector3f(1, 1, 0);
                vertices[2] = new Vector3f(1, 0, 0);
                vertices[3] = new Vector3f(0, 0, 0);
            }
            case SOUTH -> {
                vertices[0] = new Vector3f(0, 0, 1);
                vertices[1] = new Vector3f(1, 0, 1);
                vertices[2] = new Vector3f(1, 1, 1);
                vertices[3] = new Vector3f(0, 1, 1);
            }
            case WEST -> {
                vertices[0] = new Vector3f(0, 0, 0);
                vertices[1] = new Vector3f(0, 0, 1);
                vertices[2] = new Vector3f(0, 1, 1);
                vertices[3] = new Vector3f(0, 1, 0);
            }
            case EAST -> {
                vertices[0] = new Vector3f(1, 1, 0);
                vertices[1] = new Vector3f(1, 1, 1);
                vertices[2] = new Vector3f(1, 0, 1);
                vertices[3] = new Vector3f(1, 0, 0);
            }
        }
        return vertices;
    }
}

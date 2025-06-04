package com.almostreliable.lazierae.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.almostreliable.lazierae.LazierAE2Constants;
import com.almostreliable.lib.block.BlockSide;
import com.almostreliable.lib.block.io.SideConfigurable;
import com.almostreliable.lib.client.gui.AlmostGuiEventListener;
import com.almostreliable.lib.client.gui.WidgetData;
import com.almostreliable.lib.client.gui.widget.AlmostWidget;
import com.almostreliable.lib.client.rendering.AlmostRendering;
import com.almostreliable.lib.client.rendering.AlphaBufferSource;
import com.almostreliable.lib.client.rendering.ScissorStack;
import com.almostreliable.lib.menu.network.ClientAction;
import com.almostreliable.lib.util.Area;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class SideConfigWidget implements AlmostWidget<WidgetData>, AlmostGuiEventListener<WidgetData> {

    // TODO: make all of the statics, pos, background color, overlay textures and size configurable
    private static final int DEFAULT_DEPTH = 200;
    private static final float DEFAULT_ZOOM = 3;
    private static final long DEFAULT_BG_COLOR = 0xFF20_2020L;
    private static final int ZOOM_VELOCITY = 2;
    private static final float NEIGHBOR_ALPHA = 0.4f;
    private static final int YAW_VELOCITY = 4;
    private static final int PITCH_VELOCITY = 2;

    private static final ResourceLocation SELECTION_TEXTURE = AlmostWidget.getTexture(LazierAE2Constants.MOD_ID, "overlay/selection");

    // widget data
    private final WidgetData data;
    private final SideConfigurable host;
    private final NeighborBlockTint neighborBlockTint;
    private final MainBlockTint mainBlockTint;
    private final Minecraft mc;

    // transformation data
    private int depth = DEFAULT_DEPTH;
    private float zoom = DEFAULT_ZOOM;
    private float yaw;
    private float pitch;

    // render options
    private long backgroundColor = DEFAULT_BG_COLOR;
    private boolean showNeighborBlocks = true;
    private boolean showNeighborBlockEntities = true;
    private boolean showNeighborFluids = true; // TODO

    private final PoseStack debugStack = new PoseStack();

    @Nullable
    private Direction hoveredBlockFace;
    private boolean isDragging;

    public SideConfigWidget(SideConfigurable host, Area area) {
        this.host = host;
        this.data = WidgetData.of(area);
        this.neighborBlockTint = new NeighborBlockTint(host.getBlockPos());
        this.mainBlockTint = new MainBlockTint(host.getBlockPos());
        this.mc = Minecraft.getInstance();

        // initial yaw and pitch
        if (mc.player != null) {
            var lookAngle = mc.player.getLookAngle();
            yaw = (float) (Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 90);
            pitch = (float) (Math.toDegrees(Math.atan2(
                Math.sqrt(lookAngle.x * lookAngle.x + lookAngle.z * lookAngle.z),
                lookAngle.y
            )) - 90);
            limitPitch();
        }

        debugStack.pushPose();
        debugStack.translate(0, 30, depth);
        debugStack.scale(1.5f, 1.5f, 1);
    }

    public SideConfigWidget(SideConfigurable host, int x, int y, int width, int height) {
        this(host, Area.of(x, y, width, height));
    }

    public SideConfigWidget(SideConfigurable host, int x, int y, int size) {
        this(host, x, y, size, size);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        AlmostWidget.super.render(poseStack, mouseX, mouseY, delta);

        poseStack.pushPose();
        poseStack.translate(0, 0, depth);

        AlmostRendering.debug("Depth: ", depth);
        AlmostRendering.debug("Zoom: ", zoom);

        AlmostRendering.fill(poseStack, data, backgroundColor);

        Quaternion rot = Vector3f.ZP.rotationDegrees(180);
        rot.mul(Vector3f.YP.rotationDegrees(yaw));
        rot.mul(Vector3f.XN.rotationDegrees(pitch));

        updateHoveredFace(rot, mouseX, mouseY);

        renderScene(poseStack, delta);

        if (AlmostRendering.isDebug()) {
            renderDebugGrid(poseStack, rot);
        }

        poseStack.popPose();
        AlmostRendering.renderAndClearDebug(debugStack);
    }

    private void renderScene(PoseStack poseStack, float delta) {
        Quaternion rot = Vector3f.ZP.rotationDegrees(180);
        rot.mul(Vector3f.XN.rotationDegrees(pitch));
        rot.mul(Vector3f.YP.rotationDegrees(yaw));

        // outer matrix at the center of the widget to handle rotation
        poseStack.pushPose();
        poseStack.translate(data.getCenterX(), data.getCenterY(), 100);

        // flip and rotate to properly show the block
        poseStack.scale(1, 1, -1);
        poseStack.mulPose(rot);

        // inner matrix to handle block rendering in the center of the widget
        poseStack.pushPose();

        // move the block so its center is the center of the widget and scale it to a visible size
        poseStack.translate(getSizeModifier(-2), getSizeModifier(-2), getSizeModifier(-2));
        poseStack.scale(getSizeModifier(), getSizeModifier(), getSizeModifier());

        ScissorStack.push(data);
        ModelBlockRenderer.enableCaching();

        Lighting.setupForFlatItems();

        AlphaBufferSource.INSTANCE.renderWithAlpha(NEIGHBOR_ALPHA, alphaBuffer -> {
            BlockPos hostPos = host.getBlockPos();

            // render block entities
            if (showNeighborBlockEntities) {
                for (var dir : Direction.values()) {
                    poseStack.pushPose();
                    var neighborPos = hostPos.relative(dir);
                    poseStack.translate(dir.getStepX(), dir.getStepY(), dir.getStepZ());
                    var blockEntity = neighborBlockTint.getBlockEntity(neighborPos);
                    if (blockEntity != null) {
                        mc.getBlockEntityRenderDispatcher().render(blockEntity, delta, poseStack, alphaBuffer);
                    }
                    poseStack.popPose();
                }
            }

            // render the host block
            var bufferSource = mc.renderBuffers().bufferSource();
            var blockEntity = mainBlockTint.getBlockEntity(hostPos);
            if (blockEntity != null) {
                mc.getBlockEntityRenderDispatcher().render(blockEntity, delta, poseStack, bufferSource);
            }
            renderBlock(bufferSource, poseStack, host.getBlockState(), hostPos, true);
            bufferSource.endBatch();

            // render neighbor blocks
            if (showNeighborBlocks) {
                for (var dir : Direction.values()) {
                    poseStack.pushPose();
                    var neighborPos = hostPos.relative(dir);
                    var neighborState = neighborBlockTint.getBlockState(neighborPos);
                    poseStack.translate(dir.getStepX(), dir.getStepY(), dir.getStepZ());
                    renderBlock(alphaBuffer, poseStack, neighborState, neighborPos, false);
                    poseStack.popPose();
                }
            }
        });

        ModelBlockRenderer.clearCache();
        ScissorStack.pop();

        renderSelection(poseStack);

        Lighting.setupFor3DItems();

        poseStack.popPose();
        poseStack.popPose();
    }

    private void renderSelection(PoseStack poseStack) {
        if (hoveredBlockFace == null) return;
        if (!host.allowsConfiguration(hoveredBlockFace, BlockSide.fromDirection(hoveredBlockFace, host))) return;

        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SELECTION_TEXTURE);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        Vector3f[] vertices = getFaceVertices(hoveredBlockFace);
        Matrix4f matrix4f = poseStack.last().pose();
        bufferbuilder.vertex(matrix4f, vertices[0].x(), vertices[0].y(), vertices[0].z())
            .uv(0, 0)
            .endVertex();
        bufferbuilder.vertex(matrix4f, vertices[1].x(), vertices[1].y(), vertices[1].z())
            .uv(0, 1)
            .endVertex();
        bufferbuilder.vertex(matrix4f, vertices[2].x(), vertices[2].y(), vertices[2].z())
            .uv(1, 1)
            .endVertex();
        bufferbuilder.vertex(matrix4f, vertices[3].x(), vertices[3].y(), vertices[3].z())
            .uv(1, 0)
            .endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());

        poseStack.popPose();
    }

    private void updateHoveredFace(Quaternion rot, int mouseX, int mouseY) {
        float deltaX = (mouseX - data.getCenterX()) / getSizeModifier();
        float deltaY = (mouseY - data.getCenterY()) / getSizeModifier();
        BlockHitResult hitResult = raycast(deltaX, deltaY, rot);
        if (isDragging || hitResult == null) {
            hoveredBlockFace = null;
        } else {
            hoveredBlockFace = hitResult.getDirection();
        }

        AlmostRendering.debug("Direction: ", hoveredBlockFace == null ? "None" : hoveredBlockFace);
        var setting = host.getSideConfiguration().get(hoveredBlockFace);
        AlmostRendering.debug("Setting: ", setting == null ? "None" : setting);
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

    private void renderBlock(
        MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, BlockState state, BlockPos pos, boolean center
    ) {
        var blockRenderer = mc.getBlockRenderer();
        var shape = state.getRenderShape();
        if (shape == RenderShape.INVISIBLE) return;
        var layer = ItemBlockRenderTypes.getChunkRenderType(state);
        if (layer == RenderType.cutoutMipped()) layer = RenderType.cutout(); // converted because cutout mipped is removed below alpha 0.5
        var level = mc.level;
        if (level == null) return;
        if (center) {
            blockRenderer.renderBatched(state, pos, mainBlockTint, poseStack, bufferSource.getBuffer(layer), true, level.random);
        } else {
            blockRenderer.renderBatched(state, pos, neighborBlockTint, poseStack, bufferSource.getBuffer(layer), true, level.random);
        }
    }

    @Nullable
    private BlockHitResult raycast(float deltaX, float deltaY, Quaternion rotation) {
        Vec3 start = new Vec3(0, 0, -1).add(deltaX, deltaY, 0);
        Vec3 end = new Vec3(0, 0, 1).add(deltaX, deltaY, 0);

        start = transform(start, rotation);
        end = transform(end, rotation);

        VoxelShape shape = host.getBlockState().getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        return shape.clip(start, end, BlockPos.ZERO);
    }

    private Vec3 transform(Vec3 vec, Quaternion rotation) {
        Vector4f vec4 = new Vector4f((float) vec.x, (float) vec.y, (float) vec.z, 1);
        vec4.transform(rotation);
        return new Vec3(vec4.x() + .5f, vec4.y() + .5f, vec4.z() + .5f);
    }

    private void renderDebugGrid(PoseStack poseStack, Quaternion rotation) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f pose = poseStack.last().pose();

        for (int x = data.getX(); x < data.getRight(); x += 1) {
            for (int y = data.getY() + x % 2; y < data.getBottom(); y += 2) {
                float dX = (x - data.getCenterX()) / getSizeModifier();
                float dY = (y - data.getCenterY()) / getSizeModifier();
                BlockHitResult hit = raycast(dX, dY, rotation);
                float red;
                float green;
                float blue;
                if (hit != null) {
                    Direction dir = hit.getDirection();
                    red = .5F + .5F * dir.getStepX();
                    green = .5F + .5F * dir.getStepY();
                    blue = .5F + .5F * dir.getStepZ();
                } else {
                    red = 0.25f;
                    green = 0.25f;
                    blue = 0.25f;
                }

                bufferbuilder.vertex(pose, x, y + 1, 1).color(red, green, blue, 1).endVertex();
                bufferbuilder.vertex(pose, x + 1, y + 1, 1).color(red, green, blue, 1).endVertex();
                bufferbuilder.vertex(pose, x + 1, y, 1).color(red, green, blue, 1).endVertex();
                bufferbuilder.vertex(pose, x, y, 1).color(red, green, blue, 1).endVertex();
            }
        }

        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private void limitPitch() {
        pitch = Mth.clamp(pitch, -80, 80);
    }

    private float getSizeModifier() {
        return getSizeModifier(1);
    }

    private float getSizeModifier(int zoomMultiplier) {
        return Math.min(data.getWidth(), data.getHeight()) / (zoom * zoomMultiplier);
    }

    @Override
    public boolean isValidMouseClickButton(int mouseButton) {
        return mouseButton == AlmostGuiEventListener.LMB ||
            mouseButton == AlmostGuiEventListener.RMB;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton != AlmostGuiEventListener.LMB || hoveredBlockFace == null || mc.player == null) return;
        if (!host.allowsConfiguration(hoveredBlockFace, BlockSide.fromDirection(hoveredBlockFace, host))) return;

        int wid = mc.player.containerMenu.containerId;
        var sideConfig = host.getSideConfiguration();
        if (Screen.hasShiftDown()) {
            sideConfig.reset(hoveredBlockFace);
        } else {
            sideConfig.cycleNext(hoveredBlockFace);
        }
        ClientAction.sendAction(wid, sideConfig.serialize());
    }

    @Override
    public void onRelease(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton != AlmostGuiEventListener.RMB) return;
        isDragging = false;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if (mouseButton != AlmostGuiEventListener.RMB) return;
        isDragging = true;
        double deltaX = dragX / data.getWidth();
        double deltaY = dragY / data.getHeight();
        yaw += YAW_VELOCITY * deltaX * 180 % 360;
        pitch += PITCH_VELOCITY * deltaY * 180;
        limitPitch();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        zoom = (float) Mth.clamp(zoom - amount / ZOOM_VELOCITY, 1.5, 5);
        return true;
    }

    @Override
    public void playClickSound(SoundManager soundManager, int mouseButton) {
        // no sound
    }

    @Override
    public WidgetData getData() {
        return data;
    }

    public SideConfigWidget setDepth(int depth) {
        this.depth = depth;
        return this;
    }

    public SideConfigWidget setZoom(float zoom) {
        this.zoom = zoom;
        return this;
    }

    public SideConfigWidget showNeighborBlocks(boolean showNeighborBlocks) {
        this.showNeighborBlocks = showNeighborBlocks;
        return this;
    }

    public SideConfigWidget showNeighborBlockEntities(boolean showNeighborBlockEntities) {
        this.showNeighborBlockEntities = showNeighborBlockEntities;
        return this;
    }

    public SideConfigWidget setBackgroundColor(long backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public static final class MainBlockTint extends NeighborBlockTint {

        private static final Set<BlockPos> MASK = Set.of(BlockPos.ZERO);

        private MainBlockTint(BlockPos origin) {
            super(origin);
        }

        @Override
        protected boolean consideredAir(BlockPos blockPos) {
            var relative = blockPos.subtract(origin);
            return !MASK.contains(relative);
        }

        @Override
        public float getShade(Direction face, boolean hasShade) {
            if (!hasShade) {
                return 1.0F;
            }
            return switch (face) {
                case DOWN -> 0.5F;
                case UP -> 1.0F;
                case NORTH, SOUTH -> 0.8F;
                case WEST, EAST -> 0.6F;
            };
        }
    }

    public static class NeighborBlockTint implements BlockAndTintGetter {

        private static final Set<BlockPos> MASK = Set.of(
            BlockPos.ZERO,
            BlockPos.ZERO.above(),
            BlockPos.ZERO.below(),
            BlockPos.ZERO.north(),
            BlockPos.ZERO.south(),
            BlockPos.ZERO.east(),
            BlockPos.ZERO.west()
        );

        final BlockPos origin;

        private NeighborBlockTint(BlockPos origin) {
            this.origin = origin;
        }

        private Level getClientLevel() {
            return Minecraft.getInstance().level;
        }

        @Override
        public float getShade(Direction face, boolean hasShade) {
            return 1;
        }

        @Override
        public int getBrightness(LightLayer lightLayer, BlockPos blockPos) {
            return 15;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return getClientLevel().getChunkSource().getLightEngine();
        }

        @Override
        public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
            return getClientLevel().getBlockTint(blockPos, colorResolver);
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos blockPos) {
            if (consideredAir(blockPos)) {
                return null;
            }
            return getClientLevel().getBlockEntity(blockPos);
        }

        @Override
        public BlockState getBlockState(BlockPos blockPos) {
            if (consideredAir(blockPos)) {
                return Blocks.AIR.defaultBlockState();
            }
            return getClientLevel().getBlockState(blockPos);
        }

        protected boolean consideredAir(BlockPos blockPos) {
            var relative = blockPos.subtract(origin);
            return !MASK.contains(relative);
        }

        @Override
        public FluidState getFluidState(BlockPos blockPos) {
            return getClientLevel().getFluidState(blockPos);
        }

        @Override
        public int getHeight() {
            return getClientLevel().getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            return getClientLevel().getMinBuildHeight();
        }
    }
}

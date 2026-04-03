package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.blocks.panelcovers.DarkCover;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * Caches pre-built vertex data for a PanelTile's cells.
 * Instead of recomputing all cell geometry every frame, we capture the vertex data
 * once (when dirty) and replay it on subsequent frames.
 * <p>
 * The capture works by providing a proxy MultiBufferSource that records all vertex
 * operations. On replay, the recorded vertices are written directly to the real
 * MultiBufferSource's VertexConsumer, applying only the panel's world-space transform.
 */
public class CachedPanelRenderer {

    private boolean dirty = true;
    private int lastCombinedLight = -1;
    private boolean isCamouflageCache = false;

    private static ModelBlockRenderer cachedModelRenderer;

    // Captured vertex data, separated by RenderType
    private List<CachedVertex> solidVertices = new ArrayList<>();
    private List<CachedVertex> translucentVertices = new ArrayList<>();

    /**
     * A single captured vertex with all its attributes, pre-transformed
     * by the cell's local PoseStack at capture time.
     */
    public static class CachedVertex {
        final float x, y, z;
        final float r, g, b, a;
        final float u, v;
        final int lightU, lightV;
        final float normalX, normalY, normalZ;
        final boolean hasUV; // false for position+color-only vertices (drawTriangle)

        CachedVertex(float x, float y, float z, float r, float g, float b, float a,
                     float u, float v, int lightU, int lightV, float normalX, float normalY, float normalZ) {
            this.x = x; this.y = y; this.z = z;
            this.r = r; this.g = g; this.b = b; this.a = a;
            this.u = u; this.v = v;
            this.lightU = lightU; this.lightV = lightV;
            this.normalX = normalX; this.normalY = normalY; this.normalZ = normalZ;
            this.hasUV = true;
        }

        CachedVertex(float x, float y, float z, float r, float g, float b, float a) {
            this.x = x; this.y = y; this.z = z;
            this.r = r; this.g = g; this.b = b; this.a = a;
            this.u = 0; this.v = 0;
            this.lightU = 0; this.lightV = 0;
            this.normalX = 0; this.normalY = 0; this.normalZ = 0;
            this.hasUV = false;
        }
    }

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    /**
     * Check if lighting changed enough to warrant a rebuild.
     */
    public boolean lightChanged(int combinedLight) {
        return this.lastCombinedLight != combinedLight;
    }

    /**
     * Returns true if this cache contains camouflage cover geometry, which should
     * be replayed WITHOUT the panel-facing rotation (it's in block-local space).
     */
    public boolean isCamouflageCache() {
        return isCamouflageCache;
    }

    public List<CachedVertex> getSolidVertices() {
        return solidVertices;
    }

    public List<CachedVertex> getTranslucentVertices() {
        return translucentVertices;
    }

    /**
     * Rebuild the cache by running the panel's cell rendering into a capturing proxy.
     * After this call, solidVertices and translucentVertices contain all the
     * pre-transformed vertex data.
     */
    public void rebuild(PanelTile tile, PoseStack matrixStack, int combinedLight, int combinedOverlay) {
        solidVertices.clear();
        translucentVertices.clear();
        isCamouflageCache = false;

        CaptureBufferSource captureSource = new CaptureBufferSource(solidVertices, translucentVertices);

        if (tile.isCovered()) {
            // Check for camouflage cover (DarkCover/LightCover with madeFrom block)
            if (tile.panelCover instanceof DarkCover darkCover && darkCover.getMadeFrom() != null) {
                // Camouflage cover: render via ModelBlockRenderer.tesselateBlock() into the cache.
                // This gives us world-aware AO and correct face shading, all captured as vertices.
                isCamouflageCache = true;
                renderCamouflageBlock(tile, darkCover.getMadeFrom(), captureSource);
            } else {
                // Non-camouflage cover: render manually in panel space (same as cells).
                // Panel-facing rotation is applied during replay.
                matrixStack.pushPose();
                tile.panelCover.render(matrixStack, captureSource, combinedLight, combinedOverlay, tile.getColor());
                matrixStack.popPose();
            }
        } else {
            boolean hasBase = tile.hasBase();

            // Render the panel base (if it has one)
            if (hasBase) {
                renderPanelBase(tile, matrixStack, captureSource, combinedLight);
            }

            // Render all cells
            List<PanelCellPos> positions = tile.getCellPositions();
            for (PanelCellPos pos : positions) {
                if (pos.getIPanelCell() != null) {
                    PanelTileRenderer.renderCellStatic(matrixStack, pos, captureSource,
                            (tile.isCrashed()) ? 0 : combinedLight, combinedOverlay,
                            (tile.isCrashed()) ? 0.5f : 1.0f, hasBase);
                }
            }

            // Render crash overlay if needed
            if (tile.isCrashed() || tile.isOverflown()) {
                renderCrashOverlay(matrixStack, captureSource, combinedLight);
            }
        }

        // Flush the last pending vertex in each consumer.
        // In 1.21.1, vertices are committed implicitly on the next addVertex() call,
        // but the very last vertex of the session has no following addVertex() to trigger it.
        captureSource.flush();

        this.lastCombinedLight = combinedLight;
        this.dirty = false;
    }

    /**
     * Replay the cached vertex data into the real MultiBufferSource.
     * The matrixStack should already have the panel's facing rotation applied.
     */
    public void replay(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight) {
        Matrix4f transform = matrixStack.last().pose();

        if (!solidVertices.isEmpty()) {
            VertexConsumer solidBuilder = buffer.getBuffer(Sheets.cutoutBlockSheet());
            replayVertices(solidBuilder, transform, solidVertices);
        }

        if (!translucentVertices.isEmpty()) {
            VertexConsumer translucentBuilder = buffer.getBuffer(Sheets.translucentBlockSheet());
            replayVertices(translucentBuilder, transform, translucentVertices);
        }
    }

    private void replayVertices(VertexConsumer builder, Matrix4f transform, List<CachedVertex> vertices) {
        replayVerticesStatic(builder, transform, vertices);
    }

    /**
     * Static version of replayVertices for use from PanelTileRenderer's submit() method.
     */
    public static void replayVerticesStatic(VertexConsumer builder, Matrix4f transform, List<CachedVertex> vertices) {
        for (CachedVertex v : vertices) {
            if (v.hasUV) {
                // 26.1: BufferBuilder strictly requires ALL vertex elements.
                // Sheets render types need: position, color, UV0 (texture), UV1 (overlay), UV2 (lightmap), normal
                // Normal set to UP (0,1,0) — shade factor 1.0 from shader.
                // Directional face shading is already baked into vertex colors by RenderHelper.drawRectangle().
                builder.addVertex(transform, v.x, v.y, v.z)
                        .setColor(v.r, v.g, v.b, v.a)
                        .setUv(v.u, v.v)
                        .setUv1(0, 10)  // overlay: OverlayTexture.NO_OVERLAY = pack(0, 10)
                        .setUv2(v.lightU, v.lightV)
                        .setNormal(0f, 1f, 0f);
            } else {
                // Non-UV vertices also need all elements when going to Sheets render types
                builder.addVertex(transform, v.x, v.y, v.z)
                        .setColor(v.r, v.g, v.b, v.a)
                        .setUv(0, 0)
                        .setUv1(0, 10)
                        .setUv2(0, 0)
                        .setNormal(0, 1, 0);
            }
        }
    }

    private void renderPanelBase(PanelTile tileEntity, PoseStack matrixStack,
                                 MultiBufferSource buffer, int combinedLight) {
        int topTextureIndex =
                ((tileEntity.getConnectedPanelNeighbor(Side.FRONT)) ? 2 : 0)
                        + ((tileEntity.getConnectedPanelNeighbor(Side.RIGHT)) ? 1 : 0)
                        + ((tileEntity.getConnectedPanelNeighbor(Side.BACK)) ? 8 : 0)
                        + ((tileEntity.getConnectedPanelNeighbor(Side.LEFT)) ? 4 : 0);

        TextureAtlasSprite sprite = RenderHelper.getSprite(PanelTileRenderer.TEXTURE);
        TextureAtlasSprite topSprite = (topTextureIndex == 0) ? sprite : RenderHelper.getSprite(PanelTileRenderer.TEXTURES[topTextureIndex]);
        int color = tileEntity.getColor();
        VertexConsumer builder = buffer.getBuffer(Sheets.cutoutBlockSheet());

        matrixStack.pushPose();
        matrixStack.mulPose(Axis.XP.rotationDegrees(270));
        matrixStack.translate(0, -1, 0.125);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, 1, topSprite, combinedLight, color, 1.0f);

        matrixStack.mulPose(Axis.XP.rotationDegrees(90));
        matrixStack.translate(0, -0.125, 0);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        matrixStack.translate(0, 0, 1);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        matrixStack.translate(0, 0, 1);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        matrixStack.translate(0, 0, 1);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

        matrixStack.mulPose(Axis.XP.rotationDegrees(90));
        matrixStack.translate(0, -1, 0);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, 1, sprite, combinedLight, color, 1.0f);

        matrixStack.popPose();
    }

    private void renderCrashOverlay(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight) {
        matrixStack.pushPose();
        matrixStack.translate(0, 0.126, 1);
        matrixStack.mulPose(Axis.XP.rotationDegrees(270f));

        TextureAtlasSprite sprite = RenderHelper.getSprite(PanelTileRenderer.TEXTURE_CRASHED);
        RenderHelper.drawRectangle(
                buffer.getBuffer(Sheets.translucentBlockSheet()),
                matrixStack, 0, 1, 0, 1, sprite, combinedLight, 0.9f);
        matrixStack.popPose();
    }

    /**
     * Render a camouflage cover block via ModelBlockRenderer.tesselateBlock() into the cache.
     * Uses the same vertex unpacking as vanilla's putBlockBakedQuad, but sets normals to UP
     * to prevent the Sheets shader from applying a second round of face shading — tesselateBlock
     * already bakes the correct directional shade and world-aware AO into the QuadInstance colors.
     */
    private void renderCamouflageBlock(PanelTile tile, Identifier madeFrom, CaptureBufferSource captureSource) {
        if (tile.getLevel() == null) return;

        BlockState blockState = BuiltInRegistries.BLOCK.getValue(madeFrom).defaultBlockState();
        if (blockState == null || blockState.isAir()) return;

        var modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        var model = modelSet.get(blockState);
        if (model == null) return;

        ModelBlockRenderer modelRenderer = getOrCreateModelRenderer();
        VertexConsumer builder = captureSource.getBuffer(Sheets.cutoutBlockSheet());

        // Unpack quads exactly like putBlockBakedQuad does (same color multiply, UV, light),
        // but with normal forced to UP so the shader doesn't apply a second face shade pass.
        modelRenderer.tesselateBlock(
                (var x, var y, var z, var quad, var instance) -> {
                    int lightEmission = quad.materialInfo().lightEmission();
                    for (int vertex = 0; vertex < 4; vertex++) {
                        Vector3fc pos = quad.position(vertex);
                        long packedUv = quad.packedUV(vertex);
                        int vertexColor = ARGB.multiply(instance.getColor(vertex), quad.bakedColors().color(vertex));
                        int light = instance.getLightCoordsWithEmission(vertex, lightEmission);
                        float u = UVPair.unpackU(packedUv);
                        float v = UVPair.unpackV(packedUv);
                        // Normal set to UP (0,1,0) — shade factor 1.0 from shader.
                        // Face shading is already in vertexColor from tesselateBlock.
                        builder.addVertex(pos.x() + x, pos.y() + y, pos.z() + z, vertexColor,
                                u, v, instance.overlayCoords(), light, 0f, 1f, 0f);
                    }
                },
                0f, 0f, 0f,
                (BlockAndTintGetter) tile.getLevel(), tile.getBlockPos(),
                blockState, model,
                blockState.getSeed(tile.getBlockPos())
        );
    }

    private static ModelBlockRenderer getOrCreateModelRenderer() {
        if (cachedModelRenderer == null) {
            BlockColors blockColors = Minecraft.getInstance().getBlockColors();
            cachedModelRenderer = new ModelBlockRenderer(true, false, blockColors);
        }
        return cachedModelRenderer;
    }

    /**
     * Release any resources. Called when the tile entity is removed.
     */
    public void close() {
        solidVertices.clear();
        translucentVertices.clear();
    }

    /**
     * A proxy MultiBufferSource that captures vertex data into lists instead
     * of submitting it to the GPU. Each getBuffer() call returns a CapturingVertexConsumer
     * that records vertex attributes.
     */
    private static class CaptureBufferSource implements MultiBufferSource {
        private final CapturingVertexConsumer solidConsumer;
        private final CapturingVertexConsumer translucentConsumer;

        CaptureBufferSource(List<CachedVertex> solidVertices, List<CachedVertex> translucentVertices) {
            this.solidConsumer = new CapturingVertexConsumer(solidVertices);
            this.translucentConsumer = new CapturingVertexConsumer(translucentVertices);
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            if (renderType == Sheets.translucentBlockSheet()) {
                return translucentConsumer;
            }
            return solidConsumer;
        }

        /**
         * Flush any pending vertex data from both consumers.
         * Must be called after all rendering is complete.
         */
        void flush() {
            solidConsumer.flushVertex();
            translucentConsumer.flushVertex();
        }
    }

    /**
     * A VertexConsumer that captures vertex data into a list of CachedVertex objects.
     * <p>
     * Implements the 1.21.1 VertexConsumer contract:
     * - addVertex() starts a new vertex (and implicitly commits the previous one)
     * - setColor(), setUv(), setUv2(), setNormal() set attributes on the current vertex
     * - There is no endVertex() call in 1.21.1
     * <p>
     * Vertex positions submitted via addVertex(Matrix4f, x, y, z) are stored pre-transformed
     * (the matrix is applied at capture time). On replay, only the panel's world-space
     * facing transform needs to be applied.
     */
    private static class CapturingVertexConsumer implements VertexConsumer {
        private final List<CachedVertex> vertices;

        // Current vertex being assembled
        private float x, y, z;
        private float r = 1f, g = 1f, b = 1f, a = 1f;
        private float u, v;
        private int lightU, lightV;
        private float normalX, normalY, normalZ;
        private boolean hasUV = false;
        private boolean hasPosition = false;

        CapturingVertexConsumer(List<CachedVertex> vertices) {
            this.vertices = vertices;
        }

        /**
         * Flush the current vertex to the list (if one is in progress).
         * Called before starting a new vertex.
         */
        private void flushVertex() {
            if (hasPosition) {
                if (hasUV) {
                    vertices.add(new CachedVertex(x, y, z, r, g, b, a, u, v, lightU, lightV, normalX, normalY, normalZ));
                } else {
                    vertices.add(new CachedVertex(x, y, z, r, g, b, a));
                }
                hasPosition = false;
            }
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            flushVertex();
            this.x = x;
            this.y = y;
            this.z = z;
            this.hasPosition = true;
            this.hasUV = false;
            // Reset to defaults
            this.r = 1f; this.g = 1f; this.b = 1f; this.a = 1f;
            this.u = 0; this.v = 0;
            this.lightU = 0; this.lightV = 0;
            this.normalX = 0; this.normalY = 0; this.normalZ = 0;
            return this;
        }

        /**
         * Vertex with pre-multiplied matrix. This is the path used by RenderHelper.add()
         * and Comparator's custom vertex emission.
         * The position is transformed by the matrix and stored pre-transformed.
         */
        public VertexConsumer addVertex(Matrix4f matrix, float x, float y, float z) {
            flushVertex();
            // Apply the matrix to get transformed coordinates
            float tx = matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30();
            float ty = matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31();
            float tz = matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32();
            this.x = tx;
            this.y = ty;
            this.z = tz;
            this.hasPosition = true;
            this.hasUV = false;
            // Reset to defaults
            this.r = 1f; this.g = 1f; this.b = 1f; this.a = 1f;
            this.u = 0; this.v = 0;
            this.lightU = 0; this.lightV = 0;
            this.normalX = 0; this.normalY = 0; this.normalZ = 0;
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            this.r = r / 255f; this.g = g / 255f; this.b = b / 255f; this.a = a / 255f;
            return this;
        }

        @Override
        public VertexConsumer setColor(int packedColor) {
            this.a = (packedColor >> 24 & 0xFF) / 255f;
            this.r = (packedColor >> 16 & 0xFF) / 255f;
            this.g = (packedColor >> 8 & 0xFF) / 255f;
            this.b = (packedColor & 0xFF) / 255f;
            return this;
        }

        @Override
        public VertexConsumer setColor(float r, float g, float b, float a) {
            this.r = r; this.g = g; this.b = b; this.a = a;
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.u = u;
            this.v = v;
            this.hasUV = true;
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            // Overlay UV coords — not used in tiny redstone rendering
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.lightU = u;
            this.lightV = v;
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.normalX = x;
            this.normalY = y;
            this.normalZ = z;
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }
}
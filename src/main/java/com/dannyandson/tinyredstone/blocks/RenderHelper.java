package com.dannyandson.tinyredstone.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RenderHelper {

    private static final Identifier BLOCK_ATLAS = Identifier.withDefaultNamespace("textures/atlas/blocks.png");

    private static int[] textureDiffusedColors = {
            16383998,
            16351261,
            13061821,
            3847130,
            16701501,
            8439583,
            15961002,
            4673362,
            10329495,
            1481884,
            8991416,
            3949738,
            8606770,
            6192150,
            11546150,
            1908001
    };

    public static void drawCube(PoseStack poseStack, VertexConsumer builder, TextureAtlasSprite sprite, int combinedLight, int color, float alpha) {
        drawCube(poseStack, builder, sprite, sprite, sprite, sprite, sprite, sprite, combinedLight, color, alpha);
    }

    public static void drawCube(PoseStack poseStack, VertexConsumer builder, TextureAtlasSprite sprite_top, TextureAtlasSprite sprite_front, TextureAtlasSprite sprite_right, TextureAtlasSprite sprite_back, TextureAtlasSprite sprite_left, TextureAtlasSprite sprite_bottom, int combinedLight, int color, float alpha) {
        RenderHelper.drawRectangle(builder, poseStack, 0, 1, 0, 1, sprite_top, combinedLight, color, alpha);

        //back
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.translate(0, 0, 1);
        RenderHelper.drawRectangle(builder, poseStack, 0, 1, 0, 1, sprite_back, combinedLight, color, alpha);

        //left
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        poseStack.translate(0, 0, 1);
        RenderHelper.drawRectangle(builder, poseStack, 0, 1, 0, 1, sprite_left, combinedLight, color, alpha);

        //front
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        poseStack.translate(0, 0, 1);
        RenderHelper.drawRectangle(builder, poseStack, 0, 1, 0, 1, sprite_front, combinedLight, color, alpha);

        //right
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        poseStack.translate(0, 0, 1);
        RenderHelper.drawRectangle(builder, poseStack, 0, 1, 0, 1, sprite_right, combinedLight, color, alpha);

        //bottom
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
        poseStack.translate(-1, 0, 1);
        RenderHelper.drawRectangle(builder, poseStack, 0, 1, 0, 1, sprite_bottom, combinedLight, color, alpha);
    }

    public static void drawRectangle(VertexConsumer builder, PoseStack matrixStack, float x1, float x2, float y1, float y2, TextureAtlasSprite sprite, int combinedLight, float alpha) {
        drawRectangle(builder, matrixStack, x1, x2, y1, y2, sprite, combinedLight, 0xFFFFFFFF, alpha);
    }

    public static void drawRectangle(VertexConsumer builder, PoseStack matrixStack, float x1, float x2, float y1, float y2, TextureAtlasSprite sprite, int combinedLight, int color, float alpha) {
        drawRectangle(builder, matrixStack, x1, x2, y1, y2, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), combinedLight, color, alpha);
    }

    public static void drawRectangle(VertexConsumer builder, PoseStack matrixStack, float x1, float x2, float y1, float y2, float u0, float u1, float v0, float v1, int combinedLight, int color, float alpha) {
        // Compute world-space face normal from the PoseStack normal matrix.
        // All quads are drawn in the XY plane at z=0; the local normal direction
        // depends on the winding order of the quad.
        float localNz = Math.signum((x2 - x1) * (y2 - y1));
        Vector3f normal = matrixStack.last().normal().transform(new Vector3f(0, 0, localNz));
        normal.normalize();

        // Apply Minecraft's standard directional face shading to the vertex color
        int shadedColor = applyShade(color, getShadeFromNormal(normal.x(), normal.y(), normal.z()));

        Matrix4f matrix4f = matrixStack.last().pose();
        add(builder, matrix4f, x1, y1, 0, u0, v0, combinedLight, shadedColor, alpha, normal.x(), normal.y(), normal.z());
        add(builder, matrix4f, x2, y1, 0, u1, v0, combinedLight, shadedColor, alpha, normal.x(), normal.y(), normal.z());
        add(builder, matrix4f, x2, y2, 0, u1, v1, combinedLight, shadedColor, alpha, normal.x(), normal.y(), normal.z());
        add(builder, matrix4f, x1, y2, 0, u0, v1, combinedLight, shadedColor, alpha, normal.x(), normal.y(), normal.z());
    }

    public static void drawTriangle(VertexConsumer builder, PoseStack matrixStack, float x1, float y1, float x2, float y2, float x3, float y3, int color, float alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();
        add(builder, matrix4f, x1, y1, 0.0F, color, alpha);
        add(builder, matrix4f, x2, y2, 0.0F, color, alpha);
        add(builder, matrix4f, x3, y3, 0.0F, color, alpha);
    }

    public static void drawTriangleRectangle(VertexConsumer builder, PoseStack matrixStack, float x1, float x2, float y1, float y2, int color, float alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();
        add(builder, matrix4f, x1, y1, 0.0F, color, alpha);
        add(builder, matrix4f, x2, y2, 0.0F, color, alpha);
        add(builder, matrix4f, x2, y1, 0.0F, color, alpha);

        add(builder, matrix4f, x1, y1, 0.0F, color, alpha);
        add(builder, matrix4f, x1, y2, 0.0F, color, alpha);
        add(builder, matrix4f, x2, y2, 0.0F, color, alpha);
    }

    /**
     * Adds a colored vertex (no UV/light) to a VertexConsumer.
     * 26.1: BufferBuilder requires ALL vertex elements. Must provide UV, UV1, UV2, normal.
     */
    public static void add(VertexConsumer renderer, Matrix4f matrix4f, float x, float y, float z, int color, float alpha) {
        renderer.addVertex(matrix4f, x, y, z)
                .setColor(color >> 16 & 255, color >> 8 & 255, color & 255, (int) (alpha * 255f))
                .setUv(0, 0)
                .setUv1(0, 10)  // OverlayTexture.NO_OVERLAY
                .setUv2(0, 0)
                .setNormal(0, 1, 0);
    }

    /**
     * Adds a full vertex (UV + light + normal) to a VertexConsumer.
     * 26.1: Must include setUv1 (overlay) — BufferBuilder requires all elements.
     */
    public static void add(VertexConsumer renderer, Matrix4f matrix4f, float x, float y, float z, float u, float v, int combinedLightIn, int color, float alpha, float nx, float ny, float nz) {
        renderer.addVertex(matrix4f, x, y, z)
                .setColor(color >> 16 & 255, color >> 8 & 255, color & 255, (int) (alpha * 255f))
                .setUv(u, v)
                .setUv1(0, 10)  // OverlayTexture.NO_OVERLAY
                .setUv2(combinedLightIn & 0xFFFF, (combinedLightIn >> 16) & 0xFFFF)
                .setNormal(nx, ny, nz);
    }

    /**
     * Backward-compatible overload that defaults to the old hardcoded normal.
     * Prefer the normal-aware overload when the correct face normal is known.
     */
    public static void add(VertexConsumer renderer, Matrix4f matrix4f, float x, float y, float z, float u, float v, int combinedLightIn, int color, float alpha) {
        add(renderer, matrix4f, x, y, z, u, v, combinedLightIn, color, alpha, 1, 0, 0);
    }

    /**
     * Returns Minecraft's standard directional shade multiplier for the given world-space
     * face normal. Matches vanilla block face shading: top=1.0, bottom=0.5,
     * north/south=0.8, east/west=0.6.
     */
    public static float getShadeFromNormal(float nx, float ny, float nz) {
        float ax = Math.abs(nx), ay = Math.abs(ny), az = Math.abs(nz);
        if (ay >= ax && ay >= az) {
            return ny > 0 ? 1.0f : 0.5f;  // UP or DOWN
        } else if (az >= ax) {
            return 0.8f;  // NORTH or SOUTH
        } else {
            return 0.6f;  // EAST or WEST
        }
    }

    /**
     * Multiplies the RGB channels of a packed ARGB color by the given shade factor.
     * Alpha is preserved unchanged.
     */
    public static int applyShade(int color, float shade) {
        int r = (int) ((color >> 16 & 255) * shade);
        int g = (int) ((color >> 8 & 255) * shade);
        int b = (int) ((color & 255) * shade);
        int a = color >> 24 & 255;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int getColor(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static int getTextureDiffusedColor(DyeColor dyeColor) {
        return textureDiffusedColors[dyeColor.getId()];
    }

    public static int getRed(int packedColor) {
        return packedColor >> 16 & 255;
    }

    public static int getGreen(int packedColor) {
        return packedColor >> 8 & 255;
    }

    public static int getBlue(int packedColor) {
        return packedColor & 255;
    }

    public static int getAlpha(int packedColor) {
        return packedColor >>> 24;
    }

    public static TextureAtlasSprite getSprite(Identifier resourceLocation) {
        return Minecraft.getInstance().getAtlasManager().get(new SpriteId(BLOCK_ATLAS,resourceLocation));
    }
}
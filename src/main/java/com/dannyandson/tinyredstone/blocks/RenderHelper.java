package com.dannyandson.tinyredstone.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderHelper {

    public static void drawRectangle(IVertexBuilder builder, MatrixStack matrixStack, float x1, float x2, float y1, float y2, TextureAtlasSprite sprite, int combinedLight , float alpha)
    {
        drawRectangle(builder, matrixStack, x1, x2, y1, y2, sprite, combinedLight, 0xFFFFFFFF, alpha);
    }

    public static void drawRectangle(IVertexBuilder builder, MatrixStack matrixStack, float x1, float x2, float y1, float y2, TextureAtlasSprite sprite, int combinedLight , int color, float alpha) {
        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        add(builder, matrix4f, x1, y1, 0, sprite.getMinU(), sprite.getMinV(), combinedLight, color, alpha);
        add(builder, matrix4f, x2, y1, 0, sprite.getMaxU(), sprite.getMinV(), combinedLight, color, alpha);
        add(builder, matrix4f, x2, y2, 0, sprite.getMaxU(), sprite.getMaxV(), combinedLight, color, alpha);
        add(builder, matrix4f, x1, y2, 0, sprite.getMinU(), sprite.getMaxV(), combinedLight, color, alpha);
    }

    public static void drawTriangle(IVertexBuilder builder, MatrixStack matrixStack, float x1, float y1, float x2, float y2, float x3, float y3, int color, float alpha) {
        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        add(builder, matrix4f, x1, y1, 0.0F, color, alpha);
        add(builder, matrix4f, x2, y2, 0.0F, color, alpha);
        add(builder, matrix4f, x3, y3, 0.0F, color, alpha);
    }

    public static void add(IVertexBuilder renderer, Matrix4f matrix4f, float x, float y, float z, int color, float alpha) {
        renderer.pos(matrix4f, x, y, z)
                .color(ColorHelper.PackedColor.getRed(color),ColorHelper.PackedColor.getGreen(color), ColorHelper.PackedColor.getBlue(color), (int)(alpha*255f))
                .endVertex();
    }

    public static void add(IVertexBuilder renderer, Matrix4f matrix4f, float x, float y, float z, float u, float v, int combinedLightIn, int color, float alpha) {
        renderer.pos(matrix4f, x, y, z)
                .color(ColorHelper.PackedColor.getRed(color),ColorHelper.PackedColor.getGreen(color), ColorHelper.PackedColor.getBlue(color), (int)(alpha*255f))
                .tex(u, v)
                .lightmap(combinedLightIn)
                .normal(1, 0, 0)
                .endVertex();
    }

    public static TextureAtlasSprite getSprite(ResourceLocation resourceLocation)
    {
        return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(resourceLocation);
    }


}

package com.dannyandson.tinyredstone.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderHelper {

    public static void drawRectangle(IVertexBuilder builder, MatrixStack matrixStack, float x1, float x2, float y1, float y2, TextureAtlasSprite sprite, int combinedLight , float alpha)
    {
        drawRectangle(builder, matrixStack, x1, x2, y1, y2, sprite, combinedLight, 0xFFFFFFFF, alpha);
    }

    public static void drawRectangle(IVertexBuilder builder, MatrixStack matrixStack, float x1, float x2, float y1, float y2, TextureAtlasSprite sprite, int combinedLight , int color, float alpha) {
        drawRectangle(builder, matrixStack, x1, x2, y1, y2, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), combinedLight, color, alpha);
    }

    public static void drawRectangle(IVertexBuilder builder, MatrixStack matrixStack, float x1, float x2, float y1, float y2, float u0, float u1, float v0, float v1, int combinedLight , int color, float alpha){
        Matrix4f matrix4f = matrixStack.last().pose();
        add(builder, matrix4f, x1, y1, 0, u0, v0, combinedLight, color, alpha);
        add(builder, matrix4f, x2, y1, 0, u1, v0, combinedLight, color, alpha);
        add(builder, matrix4f, x2, y2, 0, u1, v1, combinedLight, color, alpha);
        add(builder, matrix4f, x1, y2, 0, u0, v1, combinedLight, color, alpha);
    }

    public static void drawTriangle(IVertexBuilder builder, MatrixStack matrixStack, float x1, float y1, float x2, float y2, float x3, float y3, int color, float alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();
        add(builder, matrix4f, x1, y1, 0.0F, color, alpha);
        add(builder, matrix4f, x2, y2, 0.0F, color, alpha);
        add(builder, matrix4f, x3, y3, 0.0F, color, alpha);
    }

    public static void drawTriangleRectangle(IVertexBuilder builder, MatrixStack matrixStack, float x1, float x2, float y1, float y2, int color, float alpha) {
        // x1 = x+0.25f, x2 = x+3-0.25f, y1 = y, y2 = y+1
        Matrix4f matrix4f = matrixStack.last().pose();
        add(builder, matrix4f, x1, y1, 0.0F, color, alpha);
        add(builder, matrix4f, x2, y2, 0.0F, color, alpha);
        add(builder, matrix4f, x2, y1, 0.0F, color, alpha);

        add(builder, matrix4f, x1, y1, 0.0F, color, alpha);
        add(builder, matrix4f, x1, y2, 0.0F, color, alpha);
        add(builder, matrix4f, x2, y2, 0.0F, color, alpha);
        //drawTriangle(builder, matrixStack, x1, y1, x2, y2, x2, y1)
    }

    public static void add(IVertexBuilder renderer, Matrix4f matrix4f, float x, float y, float z, int color, float alpha) {
        renderer.vertex(matrix4f, x, y, z)
                .color(ColorHelper.PackedColor.red(color),ColorHelper.PackedColor.green(color), ColorHelper.PackedColor.blue(color), (int)(alpha*255f))
                .endVertex();
    }

    public static void add(IVertexBuilder renderer, Matrix4f matrix4f, float x, float y, float z, float u, float v, int combinedLightIn, int color, float alpha) {
        renderer.vertex(matrix4f, x, y, z)
                .color(ColorHelper.PackedColor.red(color),ColorHelper.PackedColor.green(color), ColorHelper.PackedColor.blue(color), (int)(alpha*255f))
                .uv(u, v)
                .uv2(combinedLightIn)
                .normal(1, 0, 0)
                .endVertex();
    }

    public static TextureAtlasSprite getSprite(ResourceLocation resourceLocation)
    {
        return Minecraft.getInstance().getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(resourceLocation);
    }


}

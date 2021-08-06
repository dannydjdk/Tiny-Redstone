package com.dannyandson.tinyredstone.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;

public class RenderHelper {

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

    public static void drawRectangle(VertexConsumer  builder, PoseStack matrixStack, float x1, float x2, float y1, float y2, TextureAtlasSprite sprite, int combinedLight , float alpha)
    {
        drawRectangle(builder, matrixStack, x1, x2, y1, y2, sprite, combinedLight, 0xFFFFFFFF, alpha);
    }

    public static void drawRectangle(VertexConsumer builder, PoseStack matrixStack, float x1, float x2, float y1, float y2, TextureAtlasSprite sprite, int combinedLight , int color, float alpha) {
        drawRectangle(builder, matrixStack, x1, x2, y1, y2, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), combinedLight, color, alpha);
    }
    public static void drawRectangle(VertexConsumer builder, PoseStack matrixStack, float x1, float x2, float y1, float y2, float u0, float u1, float v0, float v1, int combinedLight , int color, float alpha){
        Matrix4f matrix4f = matrixStack.last().pose();
        add(builder, matrix4f, x1, y1, 0, u0, v0, combinedLight, color, alpha);
        add(builder, matrix4f, x2, y1, 0, u1, v0, combinedLight, color, alpha);
        add(builder, matrix4f, x2, y2, 0, u1, v1, combinedLight, color, alpha);
        add(builder, matrix4f, x1, y2, 0, u0, v1, combinedLight, color, alpha);
    }

    public static void drawTriangle(VertexConsumer builder, PoseStack matrixStack, float x1, float y1, float x2, float y2, float x3, float y3, int color, float alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();
        add(builder, matrix4f, x1, y1, 0.0F, color, alpha);
        add(builder, matrix4f, x2, y2, 0.0F, color, alpha);
        add(builder, matrix4f, x3, y3, 0.0F, color, alpha);
    }

    public static void drawTriangleRectangle(VertexConsumer builder, PoseStack matrixStack, float x1, float x2, float y1, float y2, int color, float alpha) {
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

    public static void add(VertexConsumer renderer, Matrix4f matrix4f, float x, float y, float z, int color, float alpha) {
        renderer.vertex(matrix4f, x, y, z)
                .color(color >> 16 & 255,color >> 8 & 255, color & 255, (int)(alpha*255f))
                .endVertex();
    }

    public static void add(VertexConsumer renderer, Matrix4f matrix4f, float x, float y, float z, float u, float v, int combinedLightIn, int color, float alpha) {
        renderer.vertex(matrix4f, x, y, z)
                .color(color >> 16 & 255,color >> 8 & 255, color & 255, (int)(alpha*255f))
                .uv(u, v)
                .uv2(combinedLightIn)
                .normal(1, 0, 0)
                .endVertex();
    }

    public static int getColor (int alpha, int red, int green, int blue){
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static int getTextureDiffusedColor(DyeColor dyeColor) {
        return textureDiffusedColors[dyeColor.getId()];
    }

    public static int getRed(int packedColor){
        return packedColor >> 16 & 255;
    }
    public static int getGreen(int packedColor){
        return packedColor >> 8 & 255;
    }
    public static int getBlue(int packedColor){
        return packedColor & 255;
    }
    public static int getAlpha(int packedColor){
        return packedColor >>> 24;
    }

    public static TextureAtlasSprite getSprite(ResourceLocation resourceLocation)
    {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(resourceLocation);
    }


}

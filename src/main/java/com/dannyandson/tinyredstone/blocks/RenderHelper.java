package com.dannyandson.tinyredstone.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class RenderHelper {

    public static void drawRectangle(IVertexBuilder builder, MatrixStack matrixStack, float x1, float x2, float y1, float y2, TextureAtlasSprite sprite, int combinedLight , float alpha)
    {
        add(builder, matrixStack, x1,y1,0, sprite.getMinU(), sprite.getMinV(), combinedLight,alpha);
        add(builder, matrixStack, x2,y1,0, sprite.getMaxU(), sprite.getMinV(), combinedLight,alpha);
        add(builder, matrixStack, x2,y2,0, sprite.getMaxU(), sprite.getMaxV(), combinedLight,alpha);
        add(builder, matrixStack, x1,y2,0, sprite.getMinU(), sprite.getMaxV(), combinedLight,alpha);
    }

    public static void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v, int combinedLightIn, float alpha) {
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, alpha)
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

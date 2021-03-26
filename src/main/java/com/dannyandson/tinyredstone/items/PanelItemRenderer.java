package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.IPanelCell;
import com.dannyandson.tinyredstone.blocks.IPanelCover;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

public class PanelItemRenderer extends ItemStackTileEntityRenderer {

    public void render(ItemStack stack, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay)
    {
        func_239207_a_(stack, p_239207_2_, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay)
    {
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(PanelTileRenderer.TEXTURE);
        IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
        Integer color = DyeColor.GRAY.getColorValue();
        if (stack.getTag()!=null && stack.getTag().contains("BlockEntityTag") ) {
            CompoundNBT blockEntityTag = stack.getTag().getCompound("BlockEntityTag");
            if (blockEntityTag.contains("color")) {
                color = blockEntityTag.getInt("color");
            }
        }

        matrixStack.push();
        matrixStack.translate(0,0.125,0);


        matrixStack.push();
        matrixStack.rotate(Vector3f.XP.rotationDegrees(270));
        matrixStack.translate(0,-1,0.125);
        drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-0.125,0);
        drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color);

        matrixStack.pop();

        if (stack.getTag() !=null && stack.getTag().contains("BlockEntityTag")) {

            if (stack.getTag().getCompound("BlockEntityTag").contains("cover")) {
                String coverClass = stack.getTag().getCompound("BlockEntityTag").getString("cover");
                try {
                    IPanelCover cover = (IPanelCover) Class.forName(coverClass).getConstructor().newInstance();
                    matrixStack.push();
                    cover.render(matrixStack, buffer, combinedLight, combinedOverlay, color);
                    matrixStack.pop();
                } catch (Exception exception) {
                    TinyRedstone.LOGGER.error("Exception attempting to construct IPanelCover class for item render: " + coverClass +
                            ": " + exception.getMessage() + " " + exception.getStackTrace()[0].toString());
                }
            }
            else {
                CompoundNBT cellsNBT = stack.getTag().getCompound("BlockEntityTag").getCompound("cells");
                for (Integer i = 0; i < 64; i++) {
                    if (cellsNBT.contains(i.toString())) {
                        CompoundNBT cellNBT = cellsNBT.getCompound(i.toString());

                        if (cellNBT.contains("data")) {
                            String className = cellNBT.getString("class");
                            try {

                                IPanelCell cell = (IPanelCell) Class.forName(className).getConstructor().newInstance();
                                cell.readNBT(cellNBT.getCompound("data"));
                                Direction cellDirection = Direction.byIndex(cellNBT.getInt("direction"));
                                renderCell(matrixStack, i, cell, cellDirection, buffer, combinedLight, combinedOverlay);

                            } catch (Exception exception) {
                                TinyRedstone.LOGGER.error("Exception attempting to construct IPanelCell class for item render: " + className +
                                        ": " + exception.getMessage() + " " + exception.getStackTrace()[0].toString());
                            }
                        }
                    }
                }
            }
        }

        matrixStack.pop();

    }

    private void renderCell(MatrixStack matrixStack, Integer index, IPanelCell panelCell, Direction cellDirection, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay)
    {
        float scale = 0.125f;
        float t2X = 0.0f;
        float t2Y = -1.0f;
        float t2Z = 0.0f;
        float rotation1 = 270f;
        double cellSize = 1d/8d;

        int row = Math.round((index.floatValue()/8f)-0.5f);
        int cell = index%8;

        matrixStack.push();

        matrixStack.translate(cellSize*(double)row, 0.125, cellSize*(cell));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(rotation1));

        if (cellDirection== Direction.WEST)
        {
            matrixStack.translate(0,-cellSize,0);
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(90));
        }
        else if (cellDirection== Direction.SOUTH)
        {
            matrixStack.translate(cellSize,-cellSize,0);
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
        }
        else if (cellDirection== Direction.EAST)
        {
            matrixStack.translate(cellSize,0,0);
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(270));
        }

        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(t2X,t2Y,t2Z);

        panelCell.render(matrixStack, buffer, combinedLight, combinedOverlay,1);

        matrixStack.pop();
    }

    private void drawRectangle(IVertexBuilder builder, MatrixStack matrixStack, float x1, float x2, float y1, float y2, TextureAtlasSprite sprite, int combinedLight , Integer color)
    {
        add(builder, matrixStack, x1,y1,0, sprite.getMinU(), sprite.getMinV(), combinedLight,color);
        add(builder, matrixStack, x2,y1,0, sprite.getMaxU(), sprite.getMinV(), combinedLight,color);
        add(builder, matrixStack, x2,y2,0, sprite.getMaxU(), sprite.getMaxV(), combinedLight,color);
        add(builder, matrixStack, x1,y2,0, sprite.getMinU(), sprite.getMaxV(), combinedLight,color);
    }

    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v, int combinedLightIn, Integer color) {
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(ColorHelper.PackedColor.getRed(color),ColorHelper.PackedColor.getGreen(color), ColorHelper.PackedColor.getBlue(color),  ColorHelper.PackedColor.getAlpha(color))
                .tex(u, v)
                .lightmap(combinedLightIn)
                .normal(1, 0, 0)
                .endVertex();
    }


}
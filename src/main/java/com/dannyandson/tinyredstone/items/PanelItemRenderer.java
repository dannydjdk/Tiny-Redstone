package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCover;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class PanelItemRenderer extends BlockEntityWithoutLevelRenderer {

    public PanelItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType p_239207_2_, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
    {
        TextureAtlasSprite sprite = RenderHelper.getSprite(PanelTileRenderer.TEXTURE);
        VertexConsumer builder = buffer.getBuffer(RenderType.solid());
        Integer color = DyeColor.GRAY.getMaterialColor().col;
        if (stack.getTag()!=null && stack.getTag().contains("BlockEntityTag") ) {
            CompoundTag blockEntityTag = stack.getTag().getCompound("BlockEntityTag");
            if (blockEntityTag.contains("color")) {
                color = blockEntityTag.getInt("color");
            }
        }

        matrixStack.pushPose();
        matrixStack.translate(0,0.125,0);


        if (stack.getTag() !=null && stack.getTag().contains("BlockEntityTag")) {

            CompoundTag itemTag = stack.getTag().getCompound("BlockEntityTag");

            if (itemTag.contains("cover")) {
                String coverClass = stack.getTag().getCompound("BlockEntityTag").getString("cover");
                try {
                    IPanelCover cover = (IPanelCover) Class.forName(coverClass).getConstructor().newInstance();
                    cover.readNBT(stack.getTag().getCompound("BlockEntityTag").getCompound("coverData"));
                    matrixStack.pushPose();
                    cover.render(matrixStack, buffer, combinedLight, combinedOverlay, color);
                    matrixStack.popPose();
                } catch (Exception exception) {
                    TinyRedstone.LOGGER.error("Exception attempting to construct IPanelCover class for item render: " + coverClass +
                            ": " + exception.getMessage() + " " + exception.getStackTrace()[0].toString());
                }
            }
            else {
                boolean hasBase = !itemTag.contains("hasBase") || itemTag.getBoolean("hasBase");

                if (hasBase)
                    renderBase(matrixStack,builder,sprite,combinedLight,color);

                CompoundTag cellsNBT = itemTag.getCompound("cells");
                for (Integer i = 0; i < (hasBase?448:512); i++) {
                    if (cellsNBT.contains(i.toString())) {
                        CompoundTag cellNBT = cellsNBT.getCompound(i.toString());

                        if (cellNBT.contains("data")) {
                            String className = cellNBT.getString("class");
                            try {

                                IPanelCell cell = (IPanelCell) Class.forName(className).getConstructor().newInstance();
                                cell.readNBT(cellNBT.getCompound("data"));
                                Direction cellDirection = Direction.from3DDataValue(cellNBT.getInt("direction"));
                                renderCell(matrixStack, i, cell, cellDirection, buffer, combinedLight, combinedOverlay);

                            } catch (Exception exception) {
                                TinyRedstone.LOGGER.error("Exception attempting to construct IPanelCell class for item render: " + className +
                                        ": " + exception.getMessage() + " " + exception.getStackTrace()[0].toString());
                            }
                        }
                    }
                }
            }
        } else {
            renderBase(matrixStack,builder,sprite,combinedLight,color);
        }

        matrixStack.popPose();

    }

    private void renderBase(PoseStack matrixStack, VertexConsumer builder, TextureAtlasSprite sprite, int combinedLight, int color){
        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(270));
        matrixStack.translate(0,-1,0.125);
        drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-0.125,0);
        drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color);

        matrixStack.popPose();
    }
    private void renderCell(PoseStack matrixStack, Integer index, IPanelCell panelCell, Direction cellDirection, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
    {
        float scale = 0.125f;
        float t2X = 0.0f;
        float t2Y = -1.0f;
        float t2Z = 0.0f;
        float rotation1 = 270f;
        double cellSize = 1d/8d;

        int level = Math.round((index.floatValue()/64f)-0.5f);
        int row = Math.round(((index.floatValue()%64)/8f)-0.5f);
        int cell = index%8;

        matrixStack.pushPose();

        matrixStack.translate(cellSize*(double)row, 0.125+(cellSize*(double)level), cellSize*(cell));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(rotation1));

        if (cellDirection== Direction.WEST)
        {
            matrixStack.translate(0,-cellSize,0);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));
        }
        else if (cellDirection== Direction.SOUTH)
        {
            matrixStack.translate(cellSize,-cellSize,0);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        }
        else if (cellDirection== Direction.EAST)
        {
            matrixStack.translate(cellSize,0,0);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(270));
        }

        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(t2X,t2Y,t2Z);

        panelCell.render(matrixStack, buffer, combinedLight, combinedOverlay,1);

        matrixStack.popPose();
    }

    private void drawRectangle(VertexConsumer builder, PoseStack matrixStack, float x1, float x2, float y1, float y2, TextureAtlasSprite sprite, int combinedLight , Integer color)
    {
        add(builder, matrixStack, x1,y1,0, sprite.getU0(), sprite.getV0(), combinedLight,color);
        add(builder, matrixStack, x2,y1,0, sprite.getU1(), sprite.getV0(), combinedLight,color);
        add(builder, matrixStack, x2,y2,0, sprite.getU1(), sprite.getV1(), combinedLight,color);
        add(builder, matrixStack, x1,y2,0, sprite.getU0(), sprite.getV1(), combinedLight,color);
    }

    private void add(VertexConsumer renderer, PoseStack stack, float x, float y, float z, float u, float v, int combinedLightIn, Integer color) {
        renderer.vertex(stack.last().pose(), x, y, z)
                .color(RenderHelper.getRed(color),RenderHelper.getGreen(color),RenderHelper.getBlue(color),RenderHelper.getAlpha(color))
                .uv(u, v)
                .uv2(combinedLightIn)
                .normal(1, 0, 0)
                .endVertex();
    }


}
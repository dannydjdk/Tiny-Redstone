package com.dannyandson.tinyredstone.blocks.panelcovers;

import com.dannyandson.tinyredstone.api.IPanelCover;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;

import java.util.List;

public class TrimCover implements IPanelCover {

    private VoxelShape shape = null;
    private Float xMin = null, xMax = null, yMin = null, yMax = null, zMin = null, zMax = null;
    private TextureAtlasSprite sprite;

    @Override
    public void onPlace(PanelTile panelTile, PlayerEntity player) {
        List<PanelCellPos> cellPosList = panelTile.getCellPositions();
        if (cellPosList.size() == 0) {
            setDefaultBoundaries();
        } else {
            Integer xMin = null, xMax = null, yMin = null, yMax = null, zMin = null, zMax = null;
            boolean hasBase = panelTile.hasBase();
            for (PanelCellPos cellPos : cellPosList) {
                if (xMin == null || cellPos.getRow() < xMin)
                    xMin = cellPos.getRow();
                if (xMax == null || cellPos.getRow() > xMax)
                    xMax = cellPos.getRow();

                int y = !hasBase?cellPos.getLevel(): (cellPos.getLevel() == 0) ? 0 : cellPos.getLevel() + 1;
                if (yMin == null || y < yMin)
                    yMin = y;
                y = !hasBase?cellPos.getLevel():cellPos.getLevel() + 1;
                if (yMax == null || y > yMax)
                    yMax = y;

                if (zMin == null || cellPos.getColumn() < zMin)
                    zMin = cellPos.getColumn();
                if (zMax == null || cellPos.getColumn() > zMax)
                    zMax = cellPos.getColumn();
            }
            this.xMin = xMin / 8f;
            this.xMax = (xMax + 1) / 8f;
            this.yMin = yMin / 8f;
            this.yMax = (yMax + 1) / 8f;
            this.zMin = zMin / 8f;
            this.zMax = (zMax + 1) / 8f;
        }
    }

     /**
     * Drawing the cover on the panel
     */

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, int color) {
        if (xMin ==null)setDefaultBoundaries();
        if (sprite==null)sprite = RenderHelper.getSprite(DarkCover.TEXTURE_DEFAULT_COVER);

        //south face (+z)
        matrixStack.translate(0, 0, zMax);
        RenderHelper.drawRectangle(buffer.getBuffer(RenderType.solid()),matrixStack,xMin,xMax,yMin,yMax,sprite,combinedLight,color,1f);

        //east face (+x)
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,xMax);
        RenderHelper.drawRectangle(buffer.getBuffer(RenderType.solid()),matrixStack,0,zMax-zMin,yMin,yMax,sprite,combinedLight,color,1f);

        //north face (-z)
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,zMax-zMin);
        RenderHelper.drawRectangle(buffer.getBuffer(RenderType.solid()),matrixStack,0,xMax-xMin,0,yMax-yMin,sprite,combinedLight,color,1f);

        //west face (-x)
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,xMax-xMin);
        RenderHelper.drawRectangle(buffer.getBuffer(RenderType.solid()),matrixStack,0,zMax-zMin,0,yMax-yMin,sprite,combinedLight,color,1f);

        //bottom face (-y)
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,xMin-xMax,0);
        RenderHelper.drawRectangle(buffer.getBuffer(RenderType.solid()),matrixStack,0,zMax-zMin,0,xMax-xMin,sprite,combinedLight,color,1f);

        //top face (+y)
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
        matrixStack.translate(0,xMin-xMax,yMax-yMin);
        RenderHelper.drawRectangle(buffer.getBuffer(RenderType.solid()),matrixStack,0,zMax-zMin,0,xMax-xMin,sprite,combinedLight,color,1f);
    }

    /**
     * Does this cover allows light output?
     *
     * @return true if cells can output light, false if not.
     */
    @Override
    public boolean allowsLightOutput() {
        return false;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putFloat("xMin", xMin);
        tag.putFloat("xMax", xMax);
        tag.putFloat("yMin", yMin);
        tag.putFloat("yMax", yMax);
        tag.putFloat("zMin", zMin);
        tag.putFloat("zMax", zMax);
        return tag;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        xMin = compoundNBT.getFloat("xMin");
        xMax = compoundNBT.getFloat("xMax");
        yMin = compoundNBT.getFloat("yMin");
        yMax = compoundNBT.getFloat("yMax");
        zMin = compoundNBT.getFloat("zMin");
        zMax = compoundNBT.getFloat("zMax");
    }

    public void setDefaultBoundaries(){
        xMin = 0f;
        xMax = 1f;
        yMin = 0f;
        yMax = 1f;
        zMin = 0f;
        zMax = 1f;
    }

    /**
     * Get the shape of the cover for defining the hit box
     *
     * @return VoxelShape object defining the shape
     */
    @Override
    public VoxelShape getShape() {
        if (this.shape==null){
            if (xMin ==null)setDefaultBoundaries();
            this.shape = VoxelShapes.box(xMin, yMin, zMin, xMax, yMax, zMax);
        }
        return this.shape;
    }
}

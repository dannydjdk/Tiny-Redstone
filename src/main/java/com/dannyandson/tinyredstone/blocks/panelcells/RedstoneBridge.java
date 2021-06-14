package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.math.vector.Vector3f;

public class RedstoneBridge extends RedstoneDust {

    public RedstoneBridge()
    {
        leftEnabled=true;
        rightEnabled=true;
    }


    // super.signalStrength is left-right
    private int signalStrength2 = 0; //front-back

    //pre-calculated variables for segment points
    private static final float s6 = 0.375f;
    private static final float s7 = 0.4375f;
    private static final float s9 = 0.5625f;
    private static final float s10 = 0.625f;

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell and scaled such that length and width are 1.0 and height is 0.5 above panel base
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {

        float red1 = (signalStrength==0)?.25f:.30f + (.04f*signalStrength);
        float red2 = (signalStrength2==0)?.25f:.30f + (.04f*signalStrength2);
        int color1 = ColorHelper.PackedColor.color(255,Math.round(red1*255),0,0);
        int color2 = ColorHelper.PackedColor.color(255,Math.round(red2*255),0,0);

        TextureAtlasSprite sprite_redstone_dust = RenderHelper.getSprite(TEXTURE_REDSTONE_DUST);
        TextureAtlasSprite sprite_redstone_segment = RenderHelper.getSprite(TEXTURE_REDSTONE_DUST_SEGMENT);

        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)?RenderType.solid():RenderType.translucent());

        matrixStack.translate(0,0,0.05);
        RenderHelper.drawRectangle(builder,matrixStack,s6-.05f,s10+.05f,s6-.05f,s10+.05f,sprite_redstone_dust,combinedLight,0xFF888888, alpha);
        matrixStack.translate(0,0,-.04);

        if (rightEnabled) {
            RenderHelper.drawRectangle(builder,matrixStack,s10,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color1,alpha);
            if (crawlUpSide.contains(Side.RIGHT))
            {
                matrixStack.pushPose();
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                matrixStack.translate(0,0,1.01);
                RenderHelper.drawRectangle(builder,matrixStack,-.01f,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color1,alpha);
                matrixStack.popPose();
            }        }
        if (leftEnabled) {
            RenderHelper.drawRectangle(builder,matrixStack,-.01f,s6,s7,s9,sprite_redstone_segment,combinedLight,color1,alpha);
            if (crawlUpSide.contains(Side.LEFT))
            {
                matrixStack.pushPose();
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90));
                matrixStack.translate(-1,0,0.01);
                RenderHelper.drawRectangle(builder,matrixStack,-.01f,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color1,alpha);
                matrixStack.popPose();
            }       }
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        if (frontEnabled) {
            RenderHelper.drawRectangle(builder,matrixStack,s10,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color2,alpha);
            if (crawlUpSide.contains(Side.FRONT))
            {
                matrixStack.pushPose();
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                matrixStack.translate(0,0,1.01);
                RenderHelper.drawRectangle(builder,matrixStack,-.01f,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color2,alpha);
                matrixStack.popPose();
            }       }
        if (backEnabled) {
            RenderHelper.drawRectangle(builder,matrixStack,-.01f,s6,s7,s9,sprite_redstone_segment,combinedLight,color2,alpha);
            if (crawlUpSide.contains(Side.BACK))
            {
                matrixStack.pushPose();
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90));
                matrixStack.translate(-1,0,.01);
                RenderHelper.drawRectangle(builder,matrixStack,-.01f,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color2,alpha);
                matrixStack.popPose();
            }        }

    }


    /**
     * Called when neighboring redstone signal output changes.
     * This can be called multiple times in a tick.
     * Passes PanelCellPos object for this cell which can be used to query PanelTile for PanelCellNeighbor objects - objects wrapping another IPanelCell or a BlockState
     * @param cellPos PanelCellPos object for this cell. Can be used to query paneltile about neighbors
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean neighborChanged(PanelCellPos cellPos)    {
        int front=0, right=0,back=0, left=0;
        crawlUpSide.clear();

        //cell positions above and below cell for checking redstone stepping up or down
        PanelCellPos above=null,below;

        PanelCellNeighbor topNeighbor = cellPos.getNeighbor(Side.TOP);
        if (topNeighbor!=null) {
            if (topNeighbor.getNeighborIPanelCell() instanceof TransparentBlock)
                above = cellPos.offset(Side.TOP);
        }
        else {
            above = cellPos.offset(Side.TOP);
        }
        below=cellPos.offset(Side.BOTTOM);

        if (frontEnabled)
        {
            front = checkSideInput(cellPos,Side.FRONT,above,below);
        }
        if (rightEnabled)
        {
            right = checkSideInput(cellPos,Side.RIGHT,above,below);
        }
        if (backEnabled)
        {
            back = checkSideInput(cellPos,Side.BACK,above,below);
        }
        if (leftEnabled)
        {
            left = checkSideInput(cellPos,Side.LEFT,above,below);
        }


        boolean changed=false;
        int signal1 = Math.max( Math.max(left,right) , 0 );
        int signal2 = Math.max( Math.max(front,back) , 0 );
        if (signal1!=this.signalStrength)
        {
            this.signalStrength=signal1;
            changed = true;
        }
        if (signal2!=this.signalStrength2)
        {
            this.signalStrength2=signal2;
            changed=true;
        }

        return changed;
    }

    /**
     * Gets redstone output of the given side of the cell
     *
     * @param outputDirection side of the cell being queried
     * @return integer 0-15 indicating the strength of redstone signal
     */
    @Override
    public int getWeakRsOutput(Side outputDirection) {
        if (sideEnabled(outputDirection)) {
            if (outputDirection==Side.FRONT||outputDirection==Side.BACK)
                return Math.max(this.signalStrength2, 0);
            else if (outputDirection==Side.LEFT||outputDirection==Side.RIGHT)
                return Math.max(this.signalStrength, 0);
        }
        return 0;
    }

    @Override
    public int getStrongRsOutput(Side outputDirection) {
        return getWeakRsOutput(outputDirection);
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = super.writeNBT();
        nbt.putInt("strength2",this.signalStrength2);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        super.readNBT(compoundNBT);
        this.signalStrength2 = compoundNBT.getInt("strength2");
    }


    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos) {

    }
}

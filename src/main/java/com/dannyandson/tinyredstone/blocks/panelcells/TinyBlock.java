package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.compat.IToolTipInfo;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class TinyBlock implements IPanelCell, IColorablePanelCell, IPanelCellInfoProvider {

    public static ResourceLocation TEXTURE_TINY_BLOCK = new ResourceLocation("minecraft","block/white_wool");

    protected int weakSignalStrength = 0;
    protected int strongSignalStrength = 0;
    protected int color= DyeColor.WHITE.getColorValue();

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {
        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)?RenderType.getSolid():RenderType.getTranslucent());
        TextureAtlasSprite sprite = RenderHelper.getSprite(TEXTURE_TINY_BLOCK);



        matrixStack.translate(0,0,1.0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

    }

    /**
     * Called when neighboring redstone signal output changes.
     * This can be called multiple times in a tick.
     * Passes PanelCellPos object for this cell which can be used to query PanelTile for PanelCellNeighbor objects - objects wrapping another IPanelCell or a BlockState
     * @param cellPos PanelCellPos object for this cell. Can be used to query paneltile about neighbors
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean neighborChanged(PanelCellPos cellPos){

        PanelCellNeighbor rightNeighbor = cellPos.getNeighbor(Side.RIGHT),
                leftNeighbor = cellPos.getNeighbor(Side.LEFT),
                backNeighbor = cellPos.getNeighbor(Side.BACK),
                frontNeighbor = cellPos.getNeighbor(Side.FRONT),
                topNeighbor = cellPos.getNeighbor(Side.TOP),
                bottomNeighbor = cellPos.getNeighbor(Side.BOTTOM);

        int weak=0,strong=0;
        if (frontNeighbor!=null) {
            if (frontNeighbor.powerDrops())
                weak = frontNeighbor.getWeakRsOutput();
            else if (!(frontNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
                strong = frontNeighbor.getStrongRsOutput();
        }
        if (rightNeighbor!=null) {
            if (rightNeighbor.powerDrops())
                weak = Math.max(weak,rightNeighbor.getWeakRsOutput());
            else if (!(rightNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
                strong = Math.max(strong,rightNeighbor.getStrongRsOutput());
        }
        if (backNeighbor!=null) {
            if (backNeighbor.powerDrops())
                weak = Math.max(weak,backNeighbor.getWeakRsOutput());
            else if (!(backNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
                strong = Math.max(strong,backNeighbor.getStrongRsOutput());
        }
        if (leftNeighbor!=null) {
            if (leftNeighbor.powerDrops())
                weak = Math.max(weak,leftNeighbor.getWeakRsOutput());
            else if (!(leftNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
                strong = Math.max(strong,leftNeighbor.getStrongRsOutput());
        }
        if (topNeighbor!=null) {
            if (topNeighbor.powerDrops())
                weak = Math.max(weak,topNeighbor.getWeakRsOutput());
            else if (!(topNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
                strong = Math.max(strong,topNeighbor.getStrongRsOutput());
        }
        if (bottomNeighbor!=null) {
            if (bottomNeighbor.powerDrops())
                weak = Math.max(weak,bottomNeighbor.getWeakRsOutput());
            else if (!(bottomNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
                strong = Math.max(strong,bottomNeighbor.getStrongRsOutput());
        }

        weak=Math.max(weak,strong);

        if (weak!=this.weakSignalStrength ||strong!=this.strongSignalStrength)
        {
            this.weakSignalStrength =weak;
            this.strongSignalStrength=strong;
            return true;
        }

        return false;
    }


    /**
     * Gets redstone output of the given side of the cell
     *
     * @param outputDirection (1=Front,2=Right,3=Back,4=Left)
     * @return integer 0-15 indicating the strength of redstone signal
     */
    @Override
    public int getWeakRsOutput(Side outputDirection) {
        return this.weakSignalStrength;
    }

    @Override
    public int getStrongRsOutput(Side outputDirection) {
        return this.strongSignalStrength;
    }

     /**
     * Can this cell be pushed by a piston?
     *
     * @return true if a piston can push this block
     */
    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("strong",this.strongSignalStrength);
        nbt.putInt("weak",this.weakSignalStrength);
        nbt.putInt("color",this.color);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.strongSignalStrength=compoundNBT.getInt("strong");
        this.weakSignalStrength=compoundNBT.getInt("weak");
        this.color=compoundNBT.getInt("color");
    }

    @Override
    public void addInfo(IToolTipInfo toolTipInfo, PanelTile panelTile, PosInPanelCell pos) {
        toolTipInfo.setPowerOutput(this.weakSignalStrength);
    }
}

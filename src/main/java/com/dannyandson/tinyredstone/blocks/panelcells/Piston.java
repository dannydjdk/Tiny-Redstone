package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class Piston implements IPanelCell {

    public static ResourceLocation TEXTURE_PISTON_SIDE = new ResourceLocation("minecraft","block/piston_side");
    public static ResourceLocation TEXTURE_PISTON_SIDE_TOP = new ResourceLocation(TinyRedstone.MODID,"block/piston_side_top");
    public static ResourceLocation TEXTURE_PISTON_SIDE_BOTTOM = new ResourceLocation(TinyRedstone.MODID,"block/piston_side_bottom");
    public static ResourceLocation TEXTURE_PISTON_TOP = new ResourceLocation("minecraft","block/piston_top");
    public static ResourceLocation TEXTURE_PISTON_BOTTOM = new ResourceLocation("minecraft","block/piston_bottom");
    public static ResourceLocation TEXTURE_PISTON_INNER = new ResourceLocation("minecraft","block/piston_inner");

    protected boolean extended = false;
    protected int changePending = -1;

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {

        TextureAtlasSprite sprite_bottom = RenderHelper.getSprite(TEXTURE_PISTON_BOTTOM);
        TextureAtlasSprite sprite_inner = RenderHelper.getSprite(TEXTURE_PISTON_INNER);
        TextureAtlasSprite sprite_inner_top = RenderHelper.getSprite(TEXTURE_PISTON_TOP);


        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)?RenderType.getSolid():RenderType.getTranslucent());
        TextureAtlasSprite sprite_top = getSprite_top();

        boolean renderExtended = (extended && changePending==-1) || (!extended && changePending!=-1);


        matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
        matrixStack.translate(-1,-1,0);

        //draw top
        matrixStack.push();
        matrixStack.translate(0,0,1.0);
        drawSide(matrixStack,builder,combinedLight, alpha);
        matrixStack.pop();

        //draw right side
        matrixStack.push();
        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-1,0,1);
        drawSide(matrixStack,builder,combinedLight, alpha);
        matrixStack.pop();

        //draw left side
        matrixStack.push();
        matrixStack.rotate(Vector3f.YP.rotationDegrees(-90));
        matrixStack.translate(0,0,0);
        drawSide(matrixStack,builder,combinedLight, alpha);
        matrixStack.pop();

        //draw bottom side
        matrixStack.push();
        matrixStack.rotate(Vector3f.YP.rotationDegrees(180));
        matrixStack.translate(-1,0,0);
        drawSide(matrixStack,builder,combinedLight, alpha);
        matrixStack.pop();

        //draw front (bottom texture of piston)
        matrixStack.push();
        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,0,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite_bottom,combinedLight,alpha);

        if (renderExtended)
        {
            matrixStack.translate(0,0,-1.75);
            RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite_inner_top,combinedLight,alpha);
        }
        matrixStack.pop();

        //draw back (top texture of piston)
        matrixStack.rotate(Vector3f.XP.rotationDegrees(-90));
        matrixStack.translate(0,-1, 1);
        if (renderExtended)
        {
            matrixStack.translate(0,0,-0.25);
            RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite_inner,combinedLight,alpha);
            matrixStack.translate(0,0,1.25);
        }
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite_top,combinedLight,alpha);

    }

    private void drawSide(MatrixStack matrixStack,IVertexBuilder builder,int combinedLight, float alpha)
    {
        boolean renderExtended = (extended && changePending==-1) || (!extended && changePending!=-1);
        TextureAtlasSprite sprite_side_top = RenderHelper.getSprite(TEXTURE_PISTON_SIDE_TOP);
        TextureAtlasSprite sprite_side_bottom = (renderExtended)
                ?RenderHelper.getSprite(TEXTURE_PISTON_SIDE_BOTTOM)
                :RenderHelper.getSprite(TEXTURE_PISTON_SIDE);


        matrixStack.push();

        if (renderExtended)
            matrixStack.scale(1,.75f,1);
        matrixStack.rotate(Vector3f.XP.rotationDegrees(180));
        matrixStack.translate(0,-1,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite_side_bottom,combinedLight,alpha);
        matrixStack.pop();

        if (renderExtended)
        {
            matrixStack.push();

            matrixStack.translate(0,1.75,0);
            matrixStack.scale(1,.25f,1);
            RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite_side_top,combinedLight,alpha);

            matrixStack.scale(.25f,4,1);
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(90));
            matrixStack.translate(-1,-2.5,-0.375);
            RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite_side_top,combinedLight,alpha);

            matrixStack.pop();
        }

    }

    protected TextureAtlasSprite getSprite_top()
    {
        return RenderHelper.getSprite(TEXTURE_PISTON_TOP);
    }

    public boolean isExtended(){
        return this.extended;
    }

    @Override
    public boolean onPlace(PanelCellPos cellPos, PlayerEntity player) {
        return neighborChanged(cellPos);
    }

    /**
     * Called when neighboring redstone signal output changes.
     * This can be called multiple times in a tick.
     * Passes PanelCellPos object for this cell which can be used to query PanelTile for PanelCellNeighbor objects - objects wrapping another IPanelCell or a BlockState
     * @param cellPos PanelCellPos object for this cell. Can be used to query panel tile about neighbors
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

        boolean extend =
                (cellPos.getLevel()>0||cellPos.getCellFacing()!=Side.TOP)
                &&
                (
                        ( rightNeighbor!=null && rightNeighbor.getWeakRsOutput()>0) ||
                                ( frontNeighbor!=null && frontNeighbor.getWeakRsOutput()>0) ||
                                ( leftNeighbor!=null && leftNeighbor.getWeakRsOutput()>0) ||
                                ( topNeighbor!=null && topNeighbor.getWeakRsOutput()>0) ||
                                ( bottomNeighbor!=null && bottomNeighbor.getWeakRsOutput()>0)
                )
                &&
                        (backNeighbor == null || backNeighbor.isOnPanel())
                &&
                ( extended ||
                        (
                                backNeighbor == null || ( backNeighbor.getNeighborIPanelCell() != null &&
                                backNeighbor.isPushable() )
                        )
                );
        if (extend!=this.extended)
        {
            this.extended=extend;
            this.changePending=2;
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
        return 0;
    }

    @Override
    public int getStrongRsOutput(Side outputDirection) {
        return 0;
    }

    /**
     * Can this cell be pushed by a piston?
     *
     * @return true if a piston can push this block
     */
    @Override
    public boolean isPushable() {
        return !extended;
    }

    @Override
    public boolean canPlaceVertical(){return true;}

    /**
     * Called each each tick.
     *
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick(PanelCellPos cellPos) {
        if (changePending < 0)
            return false;
        if (changePending > 0) {
            changePending--;
            return false;
        }
        changePending--;
        return true;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("extended",this.extended);
        nbt.putInt("changePending",this.changePending);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.extended=compoundNBT.getBoolean("extended");
        this.changePending=compoundNBT.getInt("changePending");
    }
}

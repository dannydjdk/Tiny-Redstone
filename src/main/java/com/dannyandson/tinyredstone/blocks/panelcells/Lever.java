package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3f;

public class Lever implements IPanelCell, IPanelCellInfoProvider {

    public static ResourceLocation TEXTURE_LEVER = new ResourceLocation(TinyRedstone.MODID,"block/lever");
    public static ResourceLocation TEXTURE_LEVER_TOP = new ResourceLocation(TinyRedstone.MODID,"block/lever_top");
    public static ResourceLocation TEXTURE_COBBLESTONE = new ResourceLocation("minecraft","block/cobblestone");
    private boolean active = false;
    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {
        TextureAtlasSprite sprite_cobble = RenderHelper.getSprite(TEXTURE_COBBLESTONE);
        TextureAtlasSprite sprite_lever = RenderHelper.getSprite(TEXTURE_LEVER);
        TextureAtlasSprite sprite_lever_top = RenderHelper.getSprite(TEXTURE_LEVER_TOP);
        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)? RenderType.solid():RenderType.translucent());

        matrixStack.pushPose();
        float x1 = 0.3125f, x2 = .6875f, y1 = 0.25f, y2 = 0.75f;
        float w = .375f, d = 0.5f,h=0.1875f;

        matrixStack.translate(0,0,h);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_cobble,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-h,-y1);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,0,h,sprite_cobble,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1f-x1);
        RenderHelper.drawRectangle(builder,matrixStack,0,d,0,h,sprite_cobble,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,d);
        RenderHelper.drawRectangle(builder,matrixStack,0,w,0,h,sprite_cobble,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,w);
        RenderHelper.drawRectangle(builder,matrixStack,0,d,0,h,sprite_cobble,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(60));
        matrixStack.translate(0,0.03125f,0);

        matrixStack.popPose();

        x1 = 0.4375f;
        x2 = 0.5625f;
        y1 = 0;
        y2 = .625f;

        matrixStack.translate(0,0.40625,h/2f);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees((active)?45:135));

        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_lever,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_lever,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_lever,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_lever,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90));
        matrixStack.translate(0,-x1,y2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,x1,x2,sprite_lever_top,combinedLight,alpha);


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
        return (active && outputDirection!=Side.TOP)?15:0;
    }

    @Override
    public int getStrongRsOutput(Side outputDirection) {
        return (active && outputDirection==Side.BOTTOM)?15:0;
    }

    /**
     * Is this a component that does not change state based on neighbors (such as a redstone block, or potentiometer)?
     *
     * @return true if this cell's state is unaffected by neighbors
     */
    @Override
    public boolean isIndependentState() {
        return true;
    }

    @Override
    public boolean needsSolidBase(){return true;}

    /**
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param cellPos The position of the clicked IPanelCell within the panel (this IPanelCell)
     * @param segmentClicked Which of nine segment within the cell were clicked.
     * @param player
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, PlayerEntity player) {
        PanelTile panelTile = cellPos.getPanelTile();
        panelTile.getLevel().playLocalSound(
                panelTile.getBlockPos().getX(), panelTile.getBlockPos().getY(), panelTile.getBlockPos().getZ(),
                SoundEvents.LEVER_CLICK,
                SoundCategory.BLOCKS, 0.25f, 2f, false
        );
        this.active=!this.active;
        return true;
    }

    @Override
    public boolean hasActivation(){return true;}

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("active",this.active);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.active=compoundNBT.getBoolean("active");
    }

    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos) {
        overlayBlockInfo.addText("State", this.active ? "On" : "Off");
        overlayBlockInfo.setPowerOutput(0);
    }

    @Override
    public PanelCellVoxelShape getShape()
    {
        return PanelCellVoxelShape.BUTTONSHAPE;
    }
}

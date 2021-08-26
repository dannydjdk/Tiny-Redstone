package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class Lever implements IPanelCell, IPanelCellInfoProvider {

    public static ResourceLocation TEXTURE_LEVER = new ResourceLocation("minecraft","block/lever");
    public static ResourceLocation TEXTURE_COBBLESTONE = new ResourceLocation("minecraft","block/cobblestone");
    private boolean active = false;
    private Side baseSide = Side.BOTTOM;

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     * @param buffer
     */
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, float alpha) {
        TextureAtlasSprite sprite_cobble = RenderHelper.getSprite(TEXTURE_COBBLESTONE);
        TextureAtlasSprite sprite_lever = RenderHelper.getSprite(TEXTURE_LEVER);
        float lu0 = sprite_lever.getU0();
        float lu1 = sprite_lever.getU1();
        float lv0 = sprite_lever.getV0();
        float lv1 = sprite_lever.getV1();
        float lhu0 = lu0 + ((lu1-lu0)*7f/16f);
        float lhu1 = lhu0 + ((lu1-lu0)*2f/16f);
        float lhv0 = lv0 + ((lv1-lv0)*6f/16f);
        float lhv1 = lv1;

        VertexConsumer builder = buffer.getBuffer((alpha==1.0)? RenderType.solid():RenderType.translucent());

        matrixStack.pushPose();
        float x1 = 0.3125f, x2 = .6875f, y1 = 0.25f, y2 = 0.75f;
        float w = .375f, d = 0.5f,h=0.1875f;

        if (baseSide==Side.FRONT) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
            matrixStack.translate(0,0,h-1);
        }
        else if (baseSide==Side.TOP) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
            matrixStack.translate(0,-1,h-1);
        }
        else
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

        if (baseSide==Side.FRONT) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
            matrixStack.translate(0,0.40625,(h/2f)-1);
        }
        else if (baseSide==Side.TOP) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
            matrixStack.translate(0,0.40625-1,(h/2f)-1);
        }
        else
            matrixStack.translate(0,0.40625,h/2f);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees((active)?45:135));

        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,lhu1,lhu0,lhv1,lhv0,combinedLight,0xFFFFFFFF,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,lhu1,lhu0,lhv1,lhv0,combinedLight,0xFFFFFFFF,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,lhu1,lhu0,lhv1,lhv0,combinedLight,0xFFFFFFFF,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,lhu1,lhu0,lhv1,lhv0,combinedLight,0xFFFFFFFF,alpha);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90));
        matrixStack.translate(0,-x1,y2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,x1,x2,lhu1,lhu0,lhv0 + ((lv1-lv0)*2f/16f),lhv0,combinedLight,0xFFFFFFFF,alpha);


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
        return (active && outputDirection!=baseSide.getOpposite())?15:0;
    }

    @Override
    public int getStrongRsOutput(Side outputDirection) {
        return (active && outputDirection==baseSide)?15:0;
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

    @Override
    public Side getBaseSide(){return this.baseSide;}

    @Override
    public void setBaseSide(Side side){this.baseSide=side;}

    /**
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param cellPos The position of the clicked IPanelCell within the panel (this IPanelCell)
     * @param segmentClicked Which of nine segment within the cell were clicked.
     * @param player
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, Player player) {
        PanelTile panelTile = cellPos.getPanelTile();
        panelTile.getLevel().playLocalSound(
                panelTile.getBlockPos().getX(), panelTile.getBlockPos().getY(), panelTile.getBlockPos().getZ(),
                SoundEvents.LEVER_CLICK,
                SoundSource.BLOCKS, 0.25f, 2f, false
        );
        this.active=!this.active;
        return true;
    }

    @Override
    public boolean hasActivation(){return true;}

    @Override
    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("active",this.active);
        nbt.putString("baseSide",baseSide.name());
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag compoundNBT) {
        this.active=compoundNBT.getBoolean("active");
        if (compoundNBT.getString("baseSide").length()>0)
            this.baseSide=Side.valueOf(compoundNBT.getString("baseSide"));
        else
            baseSide=Side.BOTTOM;
    }

    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos) {
        overlayBlockInfo.addText("State", this.active ? "On" : "Off");
        overlayBlockInfo.setPowerOutput(0);
    }

    @Override
    public PanelCellVoxelShape getShape()
    {
        if (baseSide==Side.BOTTOM)
            return PanelCellVoxelShape.BUTTONSHAPE;
        if (baseSide==Side.TOP)
            return PanelCellVoxelShape.BUTTONSHAPE_TOP;
        return PanelCellVoxelShape.BUTTONSHAPE_FRONT;
    }
}

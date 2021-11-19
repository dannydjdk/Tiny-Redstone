package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
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

public class Button implements IPanelCell, IPanelCellInfoProvider {

    public static ResourceLocation TEXTURE_OAK_PLANKS = new ResourceLocation("minecraft","block/oak_planks");

    protected boolean active = false;
    protected Integer ticksRemaining = 0;
    protected Side baseSide=Side.BOTTOM;

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {

        TextureAtlasSprite sprite = getSprite();
        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)?RenderType.solid():RenderType.translucent());

        if (baseSide==Side.FRONT) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
            matrixStack.translate(0,0,(active)?-0.9375:-0.875);
        }
        else if (baseSide==Side.TOP) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
            matrixStack.translate(0,-1,(active)?-0.9375:-0.875);
        }
        else
            matrixStack.translate(0,0,(active)?0.0625:0.125);

        float x1 = 0.3125f, x2 = .6875f, y1 = .375f, y2 = .625f;

        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-0.125,-y1);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,0,0.125f,sprite,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,.6875);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.25f,0,0.125f,sprite,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,.25);
        RenderHelper.drawRectangle(builder,matrixStack,0,.375f,0,.125f,sprite,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,0.375);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.25f,0,0.125f,sprite,combinedLight,alpha);

    }

    @Override
    public boolean neighborChanged(PanelCellPos cellPos) {
        return false;
    }

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

    /**
     * Can this cell be pushed by a piston?
     *
     * @return true if a piston can push this block
     */
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean needsSolidBase(){return true;}

    @Override
    public boolean canAttachToBaseOnSide(Side side) {
        return true;
    }

    @Override
    public Side getBaseSide(){return this.baseSide;}

    @Override
    public void setBaseSide(Side side){this.baseSide=side;}

    /**
     * Called each each tick.
     *
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick(PanelCellPos cellPos) {
        if (active && ticksRemaining >0)
        {
            ticksRemaining--;
            if (ticksRemaining ==0)
            {
                active=false;
                return true;
            }
        }
        return false;
    }

    /**
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param cellPos The position of the clicked IPanelCell within the panel (this IPanelCell)
     * @param segmentClicked Which of nine segment within the cell were clicked.
     * @param player player who activated (right-clicked) the cell
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, PlayerEntity player) {
        if (!active)
        {
            PanelTile panelTile = cellPos.getPanelTile();
            panelTile.getLevel().playLocalSound(
                    panelTile.getBlockPos().getX(), panelTile.getBlockPos().getY(), panelTile.getBlockPos().getZ(),
                    SoundEvents.WOODEN_BUTTON_CLICK_ON,
                    SoundCategory.BLOCKS, 0.25f, 2f, false
            );
            this.active=true;
            this.ticksRemaining =30;
            return true;
        }
        return false;
    }

    @Override
    public boolean hasActivation(){return true;}

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("active",this.active);
        nbt.putInt("ticksRemaining",this.ticksRemaining);
        nbt.putString("baseSide",baseSide.name());
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.active=compoundNBT.getBoolean("active");
        this.ticksRemaining=compoundNBT.getInt("ticksRemaining");
        if (compoundNBT.getString("baseSide").length()>0)
            this.baseSide=Side.valueOf(compoundNBT.getString("baseSide"));
        else
            baseSide=Side.BOTTOM;
    }

    protected TextureAtlasSprite getSprite()
    {
        return RenderHelper.getSprite(TEXTURE_OAK_PLANKS);
    }

    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos) {
        overlayBlockInfo.setPowerOutput(this.active ? 15 : 0);
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

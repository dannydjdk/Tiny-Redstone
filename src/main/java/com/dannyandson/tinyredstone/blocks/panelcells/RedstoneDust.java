package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
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
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class RedstoneDust implements IPanelCell, IPanelCellInfoProvider {

    public static ResourceLocation TEXTURE_REDSTONE_DUST_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_redstone_dust_on");
    public static ResourceLocation TEXTURE_REDSTONE_DUST_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_redstone_dust_off");
    public static ResourceLocation TEXTURE_REDSTONE_DUST_SEGMENT_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_redstone_segment_on");
    public static ResourceLocation TEXTURE_REDSTONE_DUST_SEGMENT_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_redstone_segment_off");

    public static ResourceLocation TEXTURE_REDSTONE_DUST = new ResourceLocation(TinyRedstone.MODID,"block/panel_redstone_dust");
    public static ResourceLocation TEXTURE_REDSTONE_DUST_SEGMENT = new ResourceLocation(TinyRedstone.MODID,"block/panel_redstone_segment");


    //pre-calculated variables for segment points
    private static final float s6 = 0.375f;
    private static final float s7 = 0.4375f;
    private static final float s9 = 0.5625f;
    private static final float s10 = 0.625f;


    protected int signalStrength = 0;
    protected boolean frontEnabled = true;
    protected boolean rightEnabled = false;
    protected boolean backEnabled = true;
    protected boolean leftEnabled = false;

    protected List<Side> crawlUpSide = new ArrayList();

    private float red = .25f;
    private float green = 0;
    private float blue = 0;

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell and scaled such that length and width are 1.0 and height is 0.5 above panel base
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {

        red = (signalStrength==0)?.25f:.30f + (.04f*signalStrength);
        int color = ColorHelper.PackedColor.color(255,Math.round(red*255),0,0);

        TextureAtlasSprite sprite_redstone_dust = RenderHelper.getSprite(TEXTURE_REDSTONE_DUST);
        TextureAtlasSprite sprite_redstone_segment = RenderHelper.getSprite(TEXTURE_REDSTONE_DUST_SEGMENT);

        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)?RenderType.solid():RenderType.translucent());

        matrixStack.translate(0,0,0.01);
        RenderHelper.drawRectangle(builder,matrixStack,s6-.01f,s10+.01f,s6-.01f,s10+.01f,sprite_redstone_dust,combinedLight,color, alpha);

        if (rightEnabled) {
            RenderHelper.drawRectangle(builder,matrixStack,s10,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color,alpha);
            if (crawlUpSide.contains(Side.RIGHT))
            {
                matrixStack.pushPose();
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                matrixStack.translate(0,0,1.01);
                RenderHelper.drawRectangle(builder,matrixStack,-.01f,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color,alpha);
                matrixStack.popPose();
            }
        }
        if (leftEnabled) {
            RenderHelper.drawRectangle(builder,matrixStack,-.01f,s6,s7,s9,sprite_redstone_segment,combinedLight,color,alpha);
            if (crawlUpSide.contains(Side.LEFT))
            {
                matrixStack.pushPose();
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90));
                matrixStack.translate(-1,0,0.01);
                RenderHelper.drawRectangle(builder,matrixStack,-.01f,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color,alpha);
                matrixStack.popPose();
            }
        }
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        if (frontEnabled) {
            RenderHelper.drawRectangle(builder,matrixStack,s10,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color,alpha);
            if (crawlUpSide.contains(Side.FRONT))
            {
                matrixStack.pushPose();
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                matrixStack.translate(0,0,1.01);
                RenderHelper.drawRectangle(builder,matrixStack,-.01f,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color,alpha);
                matrixStack.popPose();
            }
        }
        if (backEnabled) {
            RenderHelper.drawRectangle(builder,matrixStack,-.01f,s6,s7,s9,sprite_redstone_segment,combinedLight,color,alpha);
            if (crawlUpSide.contains(Side.BACK))
            {
                matrixStack.pushPose();
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90));
                matrixStack.translate(-1,0,.01);
                RenderHelper.drawRectangle(builder,matrixStack,-.01f,1.01f,s7,s9,sprite_redstone_segment,combinedLight,color,alpha);
                matrixStack.popPose();
            }
        }


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

        int front=0, right=0,back=0, left=0,top=0,bottom=0;
        crawlUpSide.clear();

        //cell positions above and below cell for checking redstone stepping up or down
        PanelCellPos above=null,below;

        PanelCellNeighbor topNeighbor = cellPos.getNeighbor(Side.TOP);
        if (topNeighbor!=null) {
            top = (topNeighbor.canConnectRedstone())? topNeighbor.getWeakRsOutput():topNeighbor.getStrongRsOutputForWire();
            if (topNeighbor.getNeighborIPanelCell() instanceof TransparentBlock)
                above = cellPos.offset(Side.TOP);
        }
        else {
            above = cellPos.offset(Side.TOP);
        }

        PanelCellNeighbor bottomNeighbor = cellPos.getNeighbor(Side.BOTTOM);
        if (bottomNeighbor!=null) {
            bottom = bottomNeighbor.getStrongRsOutputForWire();
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

        int signal = Math.max(Math.max( Math.max(Math.max(front, right),Math.max(back, left)),Math.max(top,bottom)) , 0);
        if (signal!=this.signalStrength)
        {
            this.signalStrength=signal;
            return true;
        }
        return false;
    }

    protected int checkSideInput(PanelCellPos cellPos, Side side, PanelCellPos above, PanelCellPos below)
    {
        PanelCellNeighbor neighbor = cellPos.getNeighbor(side);
        int input = 0;

        if (neighbor!=null)
            input = getNeighborOutput(neighbor);

        if (!(neighbor!=null && neighbor.getNeighborIPanelCell() instanceof TransparentBlock) && above!=null)
        {
            PanelCellNeighbor aboveNeighbor =  above.getNeighbor(side,cellPos.getCellFacing());
            if (aboveNeighbor!=null && aboveNeighbor.getNeighborIPanelCell() instanceof RedstoneDust) {
                input = Math.max(input, getNeighborOutput(aboveNeighbor));
            }
        }
        if (below!=null && (neighbor==null || neighbor.getNeighborIPanelCell() instanceof TransparentBlock))
        {
            PanelCellNeighbor belowNeighbor =  below.getNeighbor(side,cellPos.getCellFacing());
            if (belowNeighbor!=null && belowNeighbor.getNeighborIPanelCell() instanceof RedstoneDust) {
                input = Math.max(input, getNeighborOutput(belowNeighbor));
                crawlUpSide.add(side);
            }
        }

        return input;
    }

    protected int getNeighborOutput(PanelCellNeighbor neighbor)
    {
        int output = neighbor.getStrongRsOutputForWire();
        if (neighbor.powerDrops())
            output--;
        else {
            int w = neighbor.getWeakRsOutput();
            if (w > output && neighbor.canConnectRedstone()) {
                output = w;
            }
        }
        return output;
    }

    protected boolean sideEnabled(Side side)
    {
        return ( side== Side.FRONT&&this.frontEnabled ) ||
                (side== Side.RIGHT&&this.rightEnabled) ||
                (side== Side.BACK&&this.backEnabled) ||
                (side== Side.LEFT&&this.leftEnabled);
    }

    /**
     * Gets redstone output of the given side of the cell
     *
     * @param outputDirection side of the cell being queried
     * @return integer 0-15 indicating the strength of redstone signal
     */
    @Override
    public int getWeakRsOutput(Side outputDirection) {
        if (sideEnabled(outputDirection)||outputDirection==Side.BOTTOM)
            return Math.max(this.signalStrength, 0);
        else
            return 0;
    }

    @Override
    public int getStrongRsOutput(Side outputDirection) {
        return getWeakRsOutput(outputDirection);
    }

    /**
     * Does the power level drop when transmitting between these cells (such as with redstone dust)?
     *
     * @return true if power level should drop, false if not
     */
    @Override
    public boolean powerDrops() {
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
        if(cellPos.getPanelTile().getLevel().isClientSide)
           return false;

        if (segmentClicked==PanelCellSegment.FRONT)
        {
            frontEnabled=!frontEnabled;
            return true;
        }
        if (segmentClicked==PanelCellSegment.RIGHT){
            rightEnabled=!rightEnabled;
            return true;
        }
        if (segmentClicked==PanelCellSegment.LEFT){
            leftEnabled=!leftEnabled;
            return true;
        }
        if (segmentClicked==PanelCellSegment.BACK){
            backEnabled=!backEnabled;
            return true;
        }
        if (segmentClicked==PanelCellSegment.CENTER){
            if (!frontEnabled||!rightEnabled||!backEnabled||!leftEnabled)
            {
                frontEnabled=true;
                rightEnabled=true;
                backEnabled=true;
                leftEnabled=true;
            }
            else
            {
                frontEnabled=false;
                rightEnabled=false;
                backEnabled=false;
                leftEnabled=false;
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean hasActivation(){return true;}

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("strength",this.signalStrength);
        nbt.putBoolean("front",frontEnabled);
        nbt.putBoolean("right",rightEnabled);
        nbt.putBoolean("back",backEnabled);
        nbt.putBoolean("left",leftEnabled);
        StringJoiner crawlUpSides = new StringJoiner(",");
        for(Side side : crawlUpSide)
        {
            crawlUpSides.add(side.name());
        }
        nbt.putString("crawlUpSides",crawlUpSides.toString());
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.signalStrength = compoundNBT.getInt("strength");
        this.frontEnabled = compoundNBT.getBoolean("front");
        this.rightEnabled = compoundNBT.getBoolean("right");
        this.backEnabled = compoundNBT.getBoolean("back");
        this.leftEnabled = compoundNBT.getBoolean("left");
        String crawlUpSides = compoundNBT.getString("crawlUpSides");
        if (!crawlUpSides.equals(""))
            for (String side : crawlUpSides.split(",")) {
                this.crawlUpSide.add(Side.valueOf(side));
            }
    }


    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos) {
        overlayBlockInfo.setPowerOutput(this.signalStrength);
    }

    @Override
    public PanelCellVoxelShape getShape()
    {
        return new PanelCellVoxelShape(new Vector3d(0d,0d,0d),new Vector3d(1d,0.05d,1d));
    }
}

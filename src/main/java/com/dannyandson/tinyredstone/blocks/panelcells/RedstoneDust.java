package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RedstoneDust implements IPanelCell, IPanelCellProbeInfoProvider {

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


    private int signalStrength = 0;
    private boolean frontEnabled = true;
    private boolean rightEnabled = false;
    private boolean backEnabled = true;
    private boolean leftEnabled = false;

    private float red = .25f;
    private float green = 0;
    private float blue = 0;

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell and scaled such that length and width are 1.0 and height is 0.5 above panel base
     * @param combinedLight
     * @param combinedOverlay
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {

        red = (signalStrength==0)?.25f:.30f + (.04f*signalStrength);

        TextureAtlasSprite sprite_redstone_dust = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_REDSTONE_DUST);
        TextureAtlasSprite sprite_redstone_segment = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_REDSTONE_DUST_SEGMENT);

        IVertexBuilder builder = buffer.getBuffer(RenderType.getTranslucent());

        matrixStack.translate(0,0,0.01);
        addSquare(builder,matrixStack,s6-.01f,s6-.01f,s10+.01f,s10+.01f,sprite_redstone_dust,combinedLight,combinedOverlay, alpha);
        //matrixStack.translate(0,0,0.5);
        if (rightEnabled) {
            addSquare(builder,matrixStack,s10,s7,1.01f,s9,sprite_redstone_segment,combinedLight,combinedOverlay, alpha);
        }
        if (leftEnabled) {
            addSquare(builder,matrixStack,-.01f,s7,s6,s9,sprite_redstone_segment,combinedLight,combinedOverlay, alpha);
        }
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        if (frontEnabled) {
            addSquare(builder,matrixStack,s10,s7,1.01f,s9,sprite_redstone_segment,combinedLight,combinedOverlay, alpha);
        }
        if (backEnabled) {
            addSquare(builder,matrixStack,-.01f,s7,s6,s9,sprite_redstone_segment,combinedLight,combinedOverlay, alpha);
        }


    }

    private void addSquare (IVertexBuilder builder, MatrixStack matrixStack, float x0, float y0, float x1, float y1,TextureAtlasSprite sprite,  int combinedLight, int combinedOverlay, float alpha){

        add(builder, matrixStack, x0,y0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay, alpha);
        add(builder, matrixStack, x1,y0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay, alpha);
        add(builder, matrixStack, x1,y1, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay, alpha);
        add(builder, matrixStack, x0,y1, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay, alpha);
    }

    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float u, float v, int combinedLightIn, int combinedOverlayIn, float alpha) {
        renderer.pos(stack.getLast().getMatrix(), x, y, 0)
                .color(red, green, blue, alpha)
                .tex(u, v)
                .lightmap(combinedLightIn)
                .normal(1, 0, 0)
                .endVertex();
    }

    /**
     * Called when neighboring redstone signal output changes.
     * This can be called multiple times in a tick.
     * Passes PanelCellNeighbor objects - an object wrapping another IPanelCell or a BlockState
     * @param frontNeighbor object to access info about front neighbor
     * @param rightNeighbor object to access info about right neighbor
     * @param backNeighbor object to access info about back neighbor
     * @param leftNeighbor object to access info about left neighbor
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean neighborChanged(PanelCellNeighbor frontNeighbor, PanelCellNeighbor rightNeighbor, PanelCellNeighbor backNeighbor, PanelCellNeighbor leftNeighbor)
    {
        int front=0, right=0,back=0, left=0;
        if (frontEnabled && frontNeighbor!=null)
        {
            front = getNeighborOutput(frontNeighbor);
        }
        if (rightEnabled && rightNeighbor!=null)
        {
            right=getNeighborOutput(rightNeighbor);
        }
        if (backEnabled && backNeighbor!=null)
        {
            back=getNeighborOutput(backNeighbor);
        }
        if (leftEnabled && leftNeighbor!=null)
        {
            left=getNeighborOutput(leftNeighbor);
        }


        int signal = Math.max( Math.max(Math.max(front, right),Math.max(back, left)) , 0);
        if (signal!=this.signalStrength)
        {
            this.signalStrength=signal;
            return true;
        }
        return false;
    }

    private int getNeighborOutput(PanelCellNeighbor neighbor)
    {
        int s = neighbor.getStrongRsOutput();
        int w = neighbor.getWeakRsOutput();
        int input=(neighbor.powerDrops())?s-1:s;
        if (w>input)
        {
            if (neighbor.canConnectRedstone())
                input=neighbor.getWeakRsOutput();
        }
        return input;
    }

    private boolean sideEnabled(PanelCellSide side)
    {
        return ( side==PanelCellSide.FRONT&&this.frontEnabled ) ||
                (side==PanelCellSide.RIGHT&&this.rightEnabled) ||
                (side==PanelCellSide.BACK&&this.backEnabled) ||
                (side==PanelCellSide.LEFT&&this.leftEnabled);
    }

    /**
     * Gets redstone output of the given side of the cell
     *
     * @param outputDirection side of the cell being queried
     * @return integer 0-15 indicating the strength of redstone signal
     */
    @Override
    public int getWeakRsOutput(PanelCellSide outputDirection) {
        if (sideEnabled(outputDirection))
            return Math.max(this.signalStrength, 0);
        else
            return 0;
    }

    @Override
    public int getStrongRsOutput(PanelCellSide outputDirection) {
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
     * Is this a component that does not change state based on neighbors (such as a redstone block, or potentiometer)?
     *
     * @return true if this cell's state is unaffected by neighbors
     */
    @Override
    public boolean isIndependentState() {
        return false;
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

    /**
     * If this cell outputs light, return the level here. Otherwise, return 0.
     *
     * @return Light level to output 0-15
     */
    @Override
    public int lightOutput() {
        return 0;
    }

    /**
     * Called at the beginning of each tick if isTicking() returned true on last call.
     *
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick() {
        return false;
    }

    /**
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param panelTile the activated PanelTile tile entity that contains this cell
     * @param cellIndex The index of the clicked IPanelCell within the panel
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelTile panelTile, Integer cellIndex, PanelCellSegment segmentClicked) {
        if(panelTile.getWorld().isRemote)
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
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("strength",this.signalStrength);
        nbt.putBoolean("front",frontEnabled);
        nbt.putBoolean("right",rightEnabled);
        nbt.putBoolean("back",backEnabled);
        nbt.putBoolean("left",leftEnabled);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.signalStrength = compoundNBT.getInt("strength");
        this.frontEnabled = compoundNBT.getBoolean("front");
        this.rightEnabled = compoundNBT.getBoolean("right");
        this.backEnabled = compoundNBT.getBoolean("back");
        this.leftEnabled = compoundNBT.getBoolean("left");
    }


    @Override
    public boolean addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PanelTile panelTile, PanelCellPos pos, PanelCellSegment segment) {
        if (signalStrength > 0) {
            probeInfo.horizontal()
                    .item(new ItemStack(Items.REDSTONE), probeInfo.defaultItemStyle().width(14).height(14))
                    .text(CompoundText.createLabelInfo("Power: ", signalStrength));
        }
        return true;
    }
}

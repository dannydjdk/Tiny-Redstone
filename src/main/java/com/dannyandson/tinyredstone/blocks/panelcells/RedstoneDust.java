package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RedstoneDust implements IPanelCell {

    public static ResourceLocation TEXTURE_REDSTONE_DUST = new ResourceLocation(TinyRedstone.MODID,"block/panel_redstone_dust");
    public static ResourceLocation TEXTURE_REDSTONE_DUST_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_redstone_dust_off");
    public static ResourceLocation TEXTURE_REDSTONE_DUST_SEGMENT = new ResourceLocation(TinyRedstone.MODID,"block/panel_redstone_segment_on");
    public static ResourceLocation TEXTURE_REDSTONE_DUST_SEGMENT_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_redstone_segment_off");

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

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell and scaled such that length and width are 1.0 and height is 0.5 above panel base
     * @param combinedLight
     * @param combinedOverlay
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {

        TextureAtlasSprite sprite_redstone_dust;
        TextureAtlasSprite sprite_redstone_segment;

        if (this.signalStrength>0) {
            sprite_redstone_dust = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_REDSTONE_DUST);
            sprite_redstone_segment = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_REDSTONE_DUST_SEGMENT);
        }
        else
        {
            sprite_redstone_dust = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_REDSTONE_DUST_OFF);
            sprite_redstone_segment = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_REDSTONE_DUST_SEGMENT_OFF);
        }

        IVertexBuilder builder = buffer.getBuffer(RenderType.getTranslucent());

        matrixStack.translate(0,0,0.01);
        addSquare(builder,matrixStack,s6-.01f,s6-.01f,s10+.01f,s10+.01f,sprite_redstone_dust,combinedLight,combinedOverlay);
        //matrixStack.translate(0,0,0.5);
        if (rightEnabled) {
            addSquare(builder,matrixStack,s10,s7,1.01f,s9,sprite_redstone_segment,combinedLight,combinedOverlay);
        }
        if (leftEnabled) {
            addSquare(builder,matrixStack,-.01f,s7,s6,s9,sprite_redstone_segment,combinedLight,combinedOverlay);
        }
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        if (frontEnabled) {
            addSquare(builder,matrixStack,s10,s7,1.01f,s9,sprite_redstone_segment,combinedLight,combinedOverlay);
        }
        if (backEnabled) {
            addSquare(builder,matrixStack,-.01f,s7,s6,s9,sprite_redstone_segment,combinedLight,combinedOverlay);
        }


    }

    private void addSquare (IVertexBuilder builder, MatrixStack matrixStack, float x0, float y0, float x1, float y1,TextureAtlasSprite sprite,  int combinedLight, int combinedOverlay){

        add(builder, matrixStack, x0,y0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, x1,y0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, x1,y1, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, x0,y1, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);
    }

    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float u, float v, int combinedLightIn, int combinedOverlayIn) {
        renderer.pos(stack.getLast().getMatrix(), x, y, 0)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .tex(u, v)
                .lightmap(combinedLightIn)
                .normal(1, 0, 0)
                .endVertex();
    }

    /**
     * Responding to the redstone signal output of an adjacent cells.
     * This can be called up to 16 times in a redstone tick (1/10th second).
     * Numbers correspond to the Direction enum ordinals
     *
     * @param rsFrontStrong strength of incoming redstone signal from direction 2 (North)
     * @param rsRightStrong strength of incoming redstone signal from direction 3 (South)
     * @param rsBackStrong strength of incoming redstone signal from direction 4 (West)
     * @param rsLeftStrong strength of incoming redstone signal from direction 5 (East)
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean inputRs(int rsFrontStrong, int rsRightStrong, int rsBackStrong, int rsLeftStrong,int rsFrontWeak, int rsRightWeak, int rsBackWeak, int rsLeftWeak)
    {
        int front = (frontEnabled)?rsFrontStrong:0;
        int right = (rightEnabled)?rsRightStrong:0;
        int back = (backEnabled)?rsBackStrong:0;
        int left = (leftEnabled)?rsLeftStrong:0;

        int signal = Math.max(Math.max(front, right),Math.max(back, left));
        if (signal!=this.signalStrength)
        {
            this.signalStrength=signal;
            return true;
        }
        return false;
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
    public boolean onBlockActivated(PanelTile panelTile, Integer cellIndex, Integer segmentClicked) {
        if(panelTile.getWorld().isRemote)
           return false;

        if (segmentClicked==1)
        {
            frontEnabled=!frontEnabled;
            return true;
        }
        if (segmentClicked==3){
            rightEnabled=!rightEnabled;
            return true;
        }
        if (segmentClicked==5){
            leftEnabled=!leftEnabled;
            return true;
        }
        if (segmentClicked==7){
            backEnabled=!backEnabled;
            return true;
        }
        if (segmentClicked==4){
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

}
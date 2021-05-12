package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.compat.IOverlayBlockInfo;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class Comparator implements IPanelCell, IPanelCellInfoProvider {
    private Integer input1 = 0;
    private Integer input2 = 0;
    private Integer output = 0;
    private Boolean subtract = false;
    protected int changePending = -1;

    public static ResourceLocation TEXTURE_COMPARATOR_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_comparator_on");
    public static ResourceLocation TEXTURE_COMPARATOR_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_comparator_off");
    public static ResourceLocation TEXTURE_Comparator_SUBTRACT_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_comparator_subtract_on");
    public static ResourceLocation TEXTURE_COMPARATOR_SUBTRACT_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_comparator_subtract_off");

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     * @param buffer
     * @param combinedLight
     * @param combinedOverlay
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {
        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)?RenderType.getSolid():RenderType.getTranslucent());
        TextureAtlasSprite sprite = RenderHelper.getSprite(PanelTileRenderer.TEXTURE);
        TextureAtlasSprite sprite_repeater = RenderHelper.getSprite(TEXTURE_COMPARATOR_OFF);

        if (this.output>0 && this.subtract) {
            sprite_repeater = RenderHelper.getSprite(TEXTURE_Comparator_SUBTRACT_ON);
        }
        else if(this.output>0){
            sprite_repeater = RenderHelper.getSprite(TEXTURE_COMPARATOR_ON);
        }
        else if(this.subtract){
            sprite_repeater = RenderHelper.getSprite(TEXTURE_COMPARATOR_SUBTRACT_OFF);
        }



        matrixStack.translate(0,0,0.25);

        add(builder, matrixStack, 0,0,0, sprite_repeater.getMinU(), sprite_repeater.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite_repeater.getMaxU(), sprite_repeater.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,1,0, sprite_repeater.getMaxU(), sprite_repeater.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,1,0, sprite_repeater.getMinU(), sprite_repeater.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-0.25,0);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

    }

    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v, int combinedLightIn, int combinedOverlayIn, float alpha) {
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, alpha)
                .tex(u, v)
                .lightmap(combinedLightIn)
                .normal(1, 0, 0)
                .endVertex();
    }

    /**
     * Called when neighboring redstone signal output changes.
     * This can be called multiple times in a tick.
     * Passes PanelCellPos object for this cell which can be used to query PanelTile for PanelCellNeighbor objects - objects wrapping another IPanelCell or a BlockState
     * @param cellPos PanelCellPos object for this cell. Can be used to query paneltile about neighbors
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean neighborChanged(PanelCellPos cellPos)
    {
        PanelCellNeighbor backNeighbor = cellPos.getNeighbor(Side.BACK),
                leftNeighbor = cellPos.getNeighbor(Side.LEFT),
                rightNeighbor = cellPos.getNeighbor(Side.RIGHT);

        this.input1 = (backNeighbor==null)?0: Math.max(Math.max(backNeighbor.getStrongRsOutput(), backNeighbor.getWeakRsOutput()), backNeighbor.getComparatorOverride());
        this.input2 = Math.max((leftNeighbor==null)?0: leftNeighbor.getStrongRsOutput(), (rightNeighbor==null)?0:rightNeighbor.getStrongRsOutput());

        this.changePending=1;

        return false;
    }

    private boolean updateOutput()
    {
        Integer output1 = 0;

        if (this.subtract)
        {
            output1=Math.max(0,input1-input2);
        }
        else
        {
            output1=(input1>input2)?input1:0;
        }

        if (output1==this.output)
            return false;
        else{
            this.output=output1;
            return true;
        }
    }

    /**
     * Gets redstone output of the given side of the cell
     *
     * @param outputDirection
     * @return integer 0-15 indicating the strengh of redstone signal
     */
    @Override
    public int getWeakRsOutput(Side outputDirection)
    {
        return getStrongRsOutput(outputDirection);
    }
    @Override
    public int getStrongRsOutput(Side outputDirection) {
        if (outputDirection== Side.FRONT && this.output>0)
            return this.output;

        return 0;
    }

    /**
     * Does the power level drop when transmitting between these cells (such as with redstone dust)?
     *
     * @return true if power level should drop, false if not
     */
    @Override
    public boolean powerDrops() {
        return false;
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

    @Override
    public boolean needsSolidBase(){return true;}

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
     * Called at the beginning of each game tick if isTicking() returned true on last call.
     * Note: 1 redstone tick is 2 game ticks, so this is called 2x per redstone tick, 20x per second.
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick() {
        if (changePending < 0)
            return false;
        if (changePending > 0) {
            changePending--;
            return false;
        }
        changePending--;
        return updateOutput();

    }

    /**
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param cellPos The position of the clicked IPanelCell within the panel (this IPanelCell)
     * @param segmentClicked Which of nine segment within the cell were clicked.
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked) {
        this.subtract=!this.subtract;
        return updateOutput();
    }

    @Override
    public boolean hasActivation(){return true;}

    @Override
    public CompoundNBT writeNBT() {

        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("output",output);
        nbt.putInt("input1",input1);
        nbt.putInt("input2",input2);
        nbt.putInt("changePending",changePending);
        nbt.putBoolean("subtract",subtract);

        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.output = compoundNBT.getInt("output");
        this.input1 = compoundNBT.getInt("input1");
        this.input2 = compoundNBT.getInt("input2");
        this.changePending=compoundNBT.getInt("changePending");
        this.subtract = compoundNBT.getBoolean("subtract");
    }

    @Override
    public void addInfo(IOverlayBlockInfo toolTipInfo, PanelTile panelTile, PosInPanelCell pos) {
        toolTipInfo.addText("Mode", this.subtract ? ComparatorMode.SUBTRACT.getString() : ComparatorMode.COMPARE.getString());
    }

    @Override
    public PanelCellVoxelShape getShape()
    {
        return PanelCellVoxelShape.QUARTERCELLSLAB;
    }
}

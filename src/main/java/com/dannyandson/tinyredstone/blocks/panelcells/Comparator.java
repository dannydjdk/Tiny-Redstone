package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.ComparatorMode;

public class Comparator implements IPanelCell, IPanelCellInfoProvider {
    private Integer input1 = 0;
    private Integer input2 = 0;
    private Integer output = 0;
    private Boolean subtract = false;
    private Boolean comparatorOverride = false;
    private Integer comparatorInput = 0;
    protected int changePending = -1;

    private int changedTick = -1;

    public static ResourceLocation TEXTURE_COMPARATOR_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_comparator_on");
    public static ResourceLocation TEXTURE_COMPARATOR_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_comparator_off");
    public static ResourceLocation TEXTURE_COMPARATOR_SUBTRACT_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_comparator_subtract_on");
    public static ResourceLocation TEXTURE_COMPARATOR_SUBTRACT_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_comparator_subtract_off");

    /**
     * Drawing the cell on the panel
     *  @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     * @param buffer
     * @param combinedLight
     * @param combinedOverlay
     */
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, float alpha) {
        VertexConsumer builder = buffer.getBuffer((alpha==1.0)?RenderType.solid():RenderType.translucent());
        TextureAtlasSprite sprite = RenderHelper.getSprite(PanelTileRenderer.TEXTURE);
        TextureAtlasSprite sprite_repeater = RenderHelper.getSprite(TEXTURE_COMPARATOR_OFF);

        if (this.output>0 && this.subtract) {
            sprite_repeater = RenderHelper.getSprite(TEXTURE_COMPARATOR_SUBTRACT_ON);
        }
        else if(this.output>0){
            sprite_repeater = RenderHelper.getSprite(TEXTURE_COMPARATOR_ON);
        }
        else if(this.subtract){
            sprite_repeater = RenderHelper.getSprite(TEXTURE_COMPARATOR_SUBTRACT_OFF);
        }



        matrixStack.translate(0,0,0.25);

        add(builder, matrixStack, 0,0,0, sprite_repeater.getU0(), sprite_repeater.getV1(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite_repeater.getU1(), sprite_repeater.getV1(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,1,0, sprite_repeater.getU1(), sprite_repeater.getV0(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,1,0, sprite_repeater.getU0(), sprite_repeater.getV0(),combinedLight,combinedOverlay,alpha);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-0.25,0);
        add(builder, matrixStack, 0,0,0, sprite.getU0(), sprite.getV1(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getU1(), sprite.getV1(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0.25f,0, sprite.getU1(), sprite.getV0(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,0.25f,0, sprite.getU0(), sprite.getV0(),combinedLight,combinedOverlay,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getU0(), sprite.getV1(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getU1(), sprite.getV1(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0.25f,0, sprite.getU1(), sprite.getV0(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,0.25f,0, sprite.getU0(), sprite.getV0(),combinedLight,combinedOverlay,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getU0(), sprite.getV1(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getU1(), sprite.getV1(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0.25f,0, sprite.getU1(), sprite.getV0(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,0.25f,0, sprite.getU0(), sprite.getV0(),combinedLight,combinedOverlay,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getU0(), sprite.getV1(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getU1(), sprite.getV1(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0.25f,0, sprite.getU1(), sprite.getV0(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,0.25f,0, sprite.getU0(), sprite.getV0(),combinedLight,combinedOverlay,alpha);

    }

    private void add(VertexConsumer renderer, PoseStack stack, float x, float y, float z, float u, float v, int combinedLightIn, int combinedOverlayIn, float alpha) {
        renderer.vertex(stack.last().pose(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, alpha)
                .uv(u, v)
                .uv2(combinedLightIn)
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
    public boolean neighborChanged(PanelCellPos cellPos) {
        this.changedTick = cellPos.getPanelTile().getRelTickTime();
        return false;
    }

    private void checkInputs(PanelCellPos cellPos){
        PanelCellNeighbor backNeighbor = cellPos.getNeighbor(Side.BACK),
                leftNeighbor = cellPos.getNeighbor(Side.LEFT),
                rightNeighbor = cellPos.getNeighbor(Side.RIGHT);
        int i1, i2;

        boolean co = (backNeighbor!=null && backNeighbor.hasComparatorOverride());
        i1 = (backNeighbor==null)?0: Math.max(backNeighbor.getStrongRsOutput(), backNeighbor.getWeakRsOutput());
        i2 = Math.max((leftNeighbor==null)?0: leftNeighbor.getStrongRsOutput(), (rightNeighbor==null)?0:rightNeighbor.getStrongRsOutput());

        if (i1!=input1 || i2!=input2 || co!=comparatorOverride){
            this.input1=i1;
            this.input2=i2;
            this.comparatorOverride=co;
            this.changePending=1;
        }
    }

    private boolean updateOutput()
    {
        Integer output1 = 0;
        Integer input = (comparatorOverride)?comparatorInput:input1;

        if (this.subtract)
        {
            output1=Math.max(0,input-input2);
        }
        else
        {
            output1=(input>=input2)?input:0;
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

    @Override
    public boolean needsSolidBase(){return true;}

    @Override
    public boolean canAttachToBaseOnSide(Side side) {
        return side==Side.BOTTOM;
    }

    @Override
    public Side getBaseSide(){return Side.BOTTOM;}


    /**
     * Called at the beginning of each game tick if isTicking() returned true on last call.
     * Note: 1 redstone tick is 2 game ticks, so this is called 2x per redstone tick, 20x per second.
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick(PanelCellPos cellPos) {

        if (this.changedTick>-1 && this.changedTick<cellPos.getPanelTile().getRelTickTime()){
            this.checkInputs(cellPos);
            this.changedTick=-1;
        }

        boolean changed = false;

        if (!cellPos.getPanelTile().getLevel().isClientSide) {
            if (comparatorOverride) {
                PanelCellNeighbor backNeighbor = cellPos.getNeighbor(Side.BACK);
                int cInput = backNeighbor.getComparatorOverride();
                if (cInput != comparatorInput) {
                    comparatorInput = cInput;
                    changePending = 1;
                }
            }

            if (changePending > 0) {
                changePending--;
            }else if (changePending==0) {
                changePending--;
                changed = updateOutput();
            }
        }

        if (this.changedTick>-1){
            this.checkInputs(cellPos);
            this.changedTick=-1;
        }

        return changed;
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
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, Player player) {
        this.subtract=!this.subtract;
        return updateOutput();
    }

    @Override
    public boolean hasActivation(){return true;}

    @Override
    public CompoundTag writeNBT() {

        CompoundTag nbt = new CompoundTag();
        nbt.putInt("output",output);
        nbt.putInt("input1",input1);
        nbt.putInt("input2",input2);
        nbt.putInt("changePending",changePending);
        nbt.putInt("comparatorInput",comparatorInput);
        nbt.putBoolean("subtract",subtract);
        nbt.putBoolean("comparatorOverride",comparatorOverride);
        nbt.putInt("changedTick",this.changedTick);

        return nbt;
    }

    @Override
    public void readNBT(CompoundTag compoundNBT) {
        this.output = compoundNBT.getInt("output");
        this.input1 = compoundNBT.getInt("input1");
        this.input2 = compoundNBT.getInt("input2");
        this.changePending=compoundNBT.getInt("changePending");
        this.comparatorInput=compoundNBT.getInt("comparatorInput");
        this.subtract = compoundNBT.getBoolean("subtract");
        this.comparatorOverride = compoundNBT.getBoolean("comparatorOverride");
        this.changedTick=compoundNBT.getInt("changedTick");
    }

    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos) {
        overlayBlockInfo.addText("Mode", this.subtract ? ComparatorMode.SUBTRACT.toString() : ComparatorMode.COMPARE.toString());
    }

    @Override
    public PanelCellVoxelShape getShape()
    {
        return PanelCellVoxelShape.QUARTERCELLSLAB;
    }
}

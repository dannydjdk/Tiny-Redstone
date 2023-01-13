package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.Config;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

public class Repeater implements IPanelCell, IPanelCellInfoProvider {
    protected boolean input = false;
    protected boolean output = false;
    protected boolean locked = false;
    private int onPending = -1;
    private int offPending = -1;
    protected Integer ticks = 2;

    public static ResourceLocation TEXTURE_REPEATER_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_repeater_on");
    public static ResourceLocation TEXTURE_REPEATER_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_repeater_off");

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
        VertexConsumer builder = buffer.getBuffer((alpha==1.0)?RenderType.solid():RenderType.translucent());
        TextureAtlasSprite sprite = RenderHelper.getSprite(PanelTileRenderer.TEXTURE);
        TextureAtlasSprite sprite_repeater = this.getRepeaterTexture();
        TextureAtlasSprite sprite_torch_head = RenderHelper.getSprite(Torch.TEXTURE_TORCH_TOP_ON);


        if (!this.output){
            sprite_torch_head = RenderHelper.getSprite(Torch.TEXTURE_TORCH_TOP_OFF);
        }

        if (locked)
            sprite_torch_head=RenderHelper.getSprite(new ResourceLocation("minecraft","block/bedrock"));

        float tU0 = sprite_torch_head.getU0();
        float tU1 = tU0 + ((sprite_torch_head.getU1()-tU0)/8);
        float tV0 = sprite_torch_head.getV0();
        float tV1 = tV0 + ((sprite_torch_head.getV1()-tV0)/8);

        matrixStack.translate(0,0,0.25);

        //draw base top
        matrixStack.pushPose();
        matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
        matrixStack.translate(-1,-1,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite_repeater,combinedLight,alpha);
        matrixStack.popPose();


        if (ticks>8) {
            matrixStack.pushPose();
            matrixStack.translate(0, 0, 0.01);
            RenderHelper.drawRectangle(builder,matrixStack,0.25f,0.75f,0.125f,0.25f,tU0,tU1,tV0,tV1,combinedLight,0xFFFFFFFF,alpha);
            matrixStack.popPose();
        }

        matrixStack.pushPose();
        //draw static torch top
        matrixStack.translate(0,0,0.125);
        RenderHelper.drawRectangle(builder,matrixStack,0.4375f,0.5625f,0.75f,0.875f,tU0,tU1,tV0,tV1,combinedLight,0xFFFFFFFF,alpha);

        //draw moving torch top
        float torch2Y = (ticks<8)? 0.75f - ticks.floatValue()*0.0625f : 0.25f;
        RenderHelper.drawRectangle(builder,matrixStack,0.4375f,0.5625f,torch2Y-0.125f,torch2Y,tU0,tU1,tV0,tV1,combinedLight,0xFFFFFFFF,alpha);

        matrixStack.popPose();

        //draw back side
        matrixStack.mulPose(Axis.XP.rotationDegrees(90));
        matrixStack.translate(0,-0.25,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,0.25f,sprite,combinedLight,alpha);

        matrixStack.pushPose();
        //draw static torch side
        matrixStack.translate(.4375f,.25,-0.75f);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,tU0,tU1,tV0,tV1,combinedLight,0xFFFFFFFF,alpha);

        //draw moving torch side
        matrixStack.translate(0,0,.875-torch2Y);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,tU0,tU1,tV0,tV1,combinedLight,0xFFFFFFFF,alpha);

        matrixStack.popPose();

        //right side
        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,0.25f,sprite,combinedLight,alpha);

        matrixStack.pushPose();
        //draw static torch side
        matrixStack.translate(.75,0.25f,-.4375f);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,tU0,tU1,tV0,tV1,combinedLight,0xFFFFFFFF,alpha);

        //draw moving torch side
        matrixStack.translate(torch2Y-.875,0,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,tU0,tU1,tV0,tV1,combinedLight,0xFFFFFFFF,alpha);

        matrixStack.popPose();

        //front side
        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,0.25f,sprite,combinedLight,alpha);
        matrixStack.pushPose();
        //draw static torch front
        matrixStack.translate(.4375f,.25,-0.125f);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,tU0,tU1,tV0,tV1,combinedLight,0xFFFFFFFF,alpha);

        //draw moving torch front
        matrixStack.translate(0,0,torch2Y-.875);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,tU0,tU1,tV0,tV1,combinedLight,0xFFFFFFFF,alpha);

        matrixStack.popPose();


        //left side
        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,0.25f,sprite,combinedLight,alpha);

        matrixStack.pushPose();
        //draw static torch side
        matrixStack.translate(.125,0.25f,-.4375f);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,tU0,tU1,tV0,tV1,combinedLight,0xFFFFFFFF,alpha);

        //draw moving torch side
        matrixStack.translate(.875-torch2Y,0,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,tU0,tU1,tV0,tV1,combinedLight,0xFFFFFFFF,alpha);

        matrixStack.popPose();


    }

    protected TextureAtlasSprite getRepeaterTexture()
    {
        if (this.output)
            return RenderHelper.getSprite(TEXTURE_REPEATER_ON);
        return RenderHelper.getSprite(TEXTURE_REPEATER_OFF);

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
                backNeighbor = cellPos.getNeighbor(Side.BACK);

        if (backNeighbor!=null && backNeighbor.getWeakRsOutput() >0 && !input)
        {
            //input has switched from off to on
            input=true;
            if(offPending==1)
            {
                onPending=ticks;
            }
        }
        else if ((backNeighbor==null || backNeighbor.getWeakRsOutput() ==0 ) && input)
        {
            input=false;
        }

        boolean wasLocked = this.locked;
        this.locked= (leftNeighbor != null && leftNeighbor.getStrongRsOutput() > 0 &&
                (leftNeighbor.getNeighborIPanelCell() instanceof Repeater || leftNeighbor.getNeighborIPanelCell() instanceof Comparator ||
                        (leftNeighbor.getNeighborBlockState() != null && (leftNeighbor.getNeighborBlockState().getBlock() == Blocks.REPEATER || leftNeighbor.getNeighborBlockState().getBlock() == Blocks.COMPARATOR))
                ))
                ||
                (rightNeighbor != null && rightNeighbor.getStrongRsOutput() > 0 &&
                        (rightNeighbor.getNeighborIPanelCell() instanceof Repeater || rightNeighbor.getNeighborIPanelCell() instanceof Comparator ||
                                (rightNeighbor.getNeighborBlockState() != null && (rightNeighbor.getNeighborBlockState().getBlock() == Blocks.REPEATER || rightNeighbor.getNeighborBlockState().getBlock() == Blocks.COMPARATOR))
                        ));
        if (wasLocked && !locked && output!=input)
            if (input)onPending=ticks;
            else offPending=ticks;

        return false;
    }

    /**
     * Gets redstone output of the given side of the cell
     *
     * @return integer 0-15 indicating the strength of redstone signal
     */
    @Override
    public int getWeakRsOutput(Side outputDirection)
    {
        return getStrongRsOutput(outputDirection);
    }
    @Override
    public int getStrongRsOutput(Side outputDirection) {
        if (output&&outputDirection== Side.FRONT)
            return 15;
        else
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
     * Called each tick.
     *
     * @param cellPos The PanelCellPos of this IPanelCell
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick(PanelCellPos cellPos) {

        if (this.input!=this.output){
            if (this.input && this.onPending==-1)
            {
                this.onPending=this.ticks;
            }
            if (!this.input && this.offPending==-1){
                this.offPending=this.ticks;
            }
        }

        if (this.onPending>=0)
            onPending--;
        if (this.offPending>=0)
            offPending--;
        if (this.onPending == 0 && !this.locked) {
            this.output = true;
            if (this.input)
                this.offPending=-1;
            else
                this.offPending=this.ticks;
            return true;
        }
        if (this.offPending==0 && !this.locked && (!input||(this.output && this.onPending>-1)) ){
            if ((this.output && this.onPending>-1))this.onPending=this.ticks;
            this.output=false;
            return true;
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
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, Player player) {
        if (ticks<8)
        {
            ticks+=2;
            return true;
        }
        else
            ticks=2;
        return false;
    }

    @Override
    public boolean hasActivation(){return true;}

    @Override
    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("output",output);
        nbt.putBoolean("input",input);
        nbt.putBoolean("locked",locked);
        nbt.putInt("offPending",this.offPending);
        nbt.putInt("onPending",this.onPending);

        nbt.putInt("ticks",this.ticks);

        return nbt;
    }

    @Override
    public void readNBT(CompoundTag compoundNBT) {
        this.output = compoundNBT.getBoolean("output");
        this.input = compoundNBT.getBoolean("input");
        this.locked = compoundNBT.getBoolean("locked");
        this.ticks = compoundNBT.getInt("ticks");
        this.offPending=compoundNBT.getInt("offPending");
        this.onPending=compoundNBT.getInt("onPending");

    }

    public Integer getTicks() {
        return this.ticks;
    }
    public void setTicks(Integer ticks){
        if (ticks<0)this.ticks=0;
        else if(ticks>Config.SUPER_REPEATER_MAX.get()*2)this.ticks=Config.SUPER_REPEATER_MAX.get()*2;
        else this.ticks=ticks;
    }

    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos) {
        overlayBlockInfo.addText("Delay", this.ticks/2 + " ticks");
        if(this.locked) {
            overlayBlockInfo.addInfo("Locked");
        }
    }

    @Override
    public PanelCellVoxelShape getShape()
    {
        return PanelCellVoxelShape.QUARTERCELLSLAB;
    }
}

package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.Config;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.compat.IOverlayBlockInfo;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.LinkedList;

public class Repeater implements IPanelCell, IPanelCellInfoProvider {
    protected boolean input = false;
    protected boolean output = false;
    protected boolean locked = false;
    private final LinkedList<Boolean> queue = new LinkedList<>();
    protected Integer ticks = 2;

    public static ResourceLocation TEXTURE_REPEATER_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_repeater_on");
    public static ResourceLocation TEXTURE_REPEATER_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_repeater_off");

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {
        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)?RenderType.getSolid():RenderType.getTranslucent());
        TextureAtlasSprite sprite = RenderHelper.getSprite(PanelTileRenderer.TEXTURE);
        TextureAtlasSprite sprite_repeater = this.getRepeaterTexture();
        TextureAtlasSprite sprite_torch_head = RenderHelper.getSprite(RedstoneDust.TEXTURE_REDSTONE_DUST_SEGMENT_ON);


        if (!this.output){
            sprite_torch_head = RenderHelper.getSprite(RedstoneDust.TEXTURE_REDSTONE_DUST_SEGMENT_OFF);
        }

        if (locked)
            sprite_torch_head=RenderHelper.getSprite(new ResourceLocation("minecraft","block/bedrock"));


        matrixStack.translate(0,0,0.25);

        //draw base top
        matrixStack.push();
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
        matrixStack.translate(-1,-1,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite_repeater,combinedLight,alpha);
        matrixStack.pop();


        if (ticks>8) {
            matrixStack.push();
            matrixStack.translate(0, 0, 0.01);
            RenderHelper.drawRectangle(builder,matrixStack,0.25f,0.75f,0.125f,0.25f,sprite_torch_head,combinedLight,alpha);
            matrixStack.pop();
        }

        matrixStack.push();
        //draw static torch top
        matrixStack.translate(0,0,0.125);
        RenderHelper.drawRectangle(builder,matrixStack,0.4375f,0.5625f,0.75f,0.875f,sprite_torch_head,combinedLight,alpha);

        //draw moving torch top
        float torch2Y = (ticks<8)? 0.75f - ticks.floatValue()*0.0625f : 0.25f;
        RenderHelper.drawRectangle(builder,matrixStack,0.4375f,0.5625f,torch2Y-0.125f,torch2Y,sprite_torch_head,combinedLight,alpha);

        matrixStack.pop();

        //draw back side
        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-0.25,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,0.25f,sprite,combinedLight,alpha);

        matrixStack.push();
        //draw static torch side
        matrixStack.translate(.4375f,.25,-0.75f);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,sprite_torch_head,combinedLight,alpha);

        //draw moving torch side
        matrixStack.translate(0,0,.875-torch2Y);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,sprite_torch_head,combinedLight,alpha);

        matrixStack.pop();

        //right side
        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,0.25f,sprite,combinedLight,alpha);

        matrixStack.push();
        //draw static torch side
        matrixStack.translate(.75,0.25f,-.4375f);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,sprite_torch_head,combinedLight,alpha);

        //draw moving torch side
        matrixStack.translate(torch2Y-.875,0,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,sprite_torch_head,combinedLight,alpha);

        matrixStack.pop();

        //front side
        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,0.25f,sprite,combinedLight,alpha);
        matrixStack.push();
        //draw static torch front
        matrixStack.translate(.4375f,.25,-0.125f);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,sprite_torch_head,combinedLight,alpha);

        //draw moving torch front
        matrixStack.translate(0,0,torch2Y-.875);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,sprite_torch_head,combinedLight,alpha);

        matrixStack.pop();


        //left side
        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,0.25f,sprite,combinedLight,alpha);

        matrixStack.push();
        //draw static torch side
        matrixStack.translate(.125,0.25f,-.4375f);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,sprite_torch_head,combinedLight,alpha);

        //draw moving torch side
        matrixStack.translate(.875-torch2Y,0,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,0.125f,0,0.125f,sprite_torch_head,combinedLight,alpha);

        matrixStack.pop();


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
            input=true;
        }
        else if ((backNeighbor==null || backNeighbor.getWeakRsOutput() ==0 ) && input)
        {
            input=false;
        }

        this.locked= (leftNeighbor != null && leftNeighbor.getStrongRsOutput() > 0 &&
                (leftNeighbor.getNeighborIPanelCell() instanceof Repeater || leftNeighbor.getNeighborIPanelCell() instanceof Comparator ||
                        (leftNeighbor.getNeighborBlockState() != null && (leftNeighbor.getNeighborBlockState().getBlock() == Blocks.REPEATER || leftNeighbor.getNeighborBlockState().getBlock() == Blocks.COMPARATOR))
                ))
                ||
                (rightNeighbor != null && rightNeighbor.getStrongRsOutput() > 0 &&
                        (rightNeighbor.getNeighborIPanelCell() instanceof Repeater || rightNeighbor.getNeighborIPanelCell() instanceof Comparator ||
                                (rightNeighbor.getNeighborBlockState() != null && (rightNeighbor.getNeighborBlockState().getBlock() == Blocks.REPEATER || rightNeighbor.getNeighborBlockState().getBlock() == Blocks.COMPARATOR))
                        ));

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
     * Called at the beginning of each tick if isTicking() returned true on last call.
     *
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick() {
        while (this.queue.size()<this.ticks || this.queue.size()<1)
            this.queue.add(this.input);

        while (this.queue.size()>this.ticks)
            this.queue.remove();

        if (this.ticks>0) {
            Boolean newOutput = this.queue.remove();
            this.queue.add(input);

            if (!this.locked && this.output != newOutput) {
                this.output = newOutput;
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
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked) {
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
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("output",output);
        nbt.putBoolean("input",input);
        nbt.putBoolean("locked",locked);

        StringBuilder queueString = new StringBuilder();
        for (Object b : queue.toArray())
        {
            queueString.append(((Boolean) b) ? "1" : "0");
        }
        nbt.putString("queue", queueString.toString());


        nbt.putInt("ticks",this.ticks);

        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.output = compoundNBT.getBoolean("output");
        this.input = compoundNBT.getBoolean("input");
        this.locked = compoundNBT.getBoolean("locked");
        this.ticks = compoundNBT.getInt("ticks");

        String queueString = compoundNBT.getString("queue");
        for (Byte b : queueString.getBytes())
        {
            queue.add(b==49);
        }

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

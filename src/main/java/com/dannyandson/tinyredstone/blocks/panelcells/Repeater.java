package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellNeighbor;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.gui.RepeaterCellGUI;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.LinkedList;

public class Repeater implements IPanelCell
{
    private boolean input = false;
    private boolean output = false;
    private boolean locked = false;
    private LinkedList<Boolean> queue = new LinkedList<>();
    private Integer ticks = 2;

    public static ResourceLocation TEXTURE_REPEATER_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_repeater_on");
    public static ResourceLocation TEXTURE_REPEATER_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_repeater_off");

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
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(PanelTileRenderer.TEXTURE);
        TextureAtlasSprite sprite_repeater = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_REPEATER_ON);
        TextureAtlasSprite sprite_torch_head = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(RedstoneDust.TEXTURE_REDSTONE_DUST_SEGMENT);;


        if (!this.output){
            sprite_repeater = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_REPEATER_OFF);
            sprite_torch_head = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(RedstoneDust.TEXTURE_REDSTONE_DUST_SEGMENT_OFF);
        }

        if (locked)
            sprite_torch_head=Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(new ResourceLocation("minecraft","block/bedrock"));


        matrixStack.translate(0,0,0.25);

        //draw base top
        add(builder, matrixStack, 0,0,0, sprite_repeater.getMinU(), sprite_repeater.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite_repeater.getMaxU(), sprite_repeater.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,1,0, sprite_repeater.getMaxU(), sprite_repeater.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,1,0, sprite_repeater.getMinU(), sprite_repeater.getMinV(),combinedLight,combinedOverlay);

        if (ticks>8) {
            matrixStack.push();
            matrixStack.translate(0, 0, 0.01);
            add(builder, matrixStack, 0.25f, 0.125f, 0, sprite_torch_head.getMinU(), sprite_torch_head.getMaxV(), combinedLight, combinedOverlay);
            add(builder, matrixStack, 0.75f, 0.125f, 0, sprite_torch_head.getMaxU(), sprite_torch_head.getMaxV(), combinedLight, combinedOverlay);
            add(builder, matrixStack, 0.75f, 0.25f, 0, sprite_torch_head.getMaxU(), sprite_torch_head.getMinV(), combinedLight, combinedOverlay);
            add(builder, matrixStack, 0.25f, 0.25f, 0, sprite_torch_head.getMinU(), sprite_torch_head.getMinV(), combinedLight, combinedOverlay);

            matrixStack.pop();
        }

        matrixStack.push();
        //draw static torch top
        matrixStack.translate(0,0,0.125);
        add(builder,matrixStack,0.4375f,0.75f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.5625f,0.75f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.5625f,0.875f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.4375f,0.875f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);

        //draw moving torch top
        float torch2Y = (ticks<8)? 0.75f - ticks.floatValue()*0.0625f : 0.25f;

        add(builder,matrixStack,0.4375f,torch2Y-0.125f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.5625f,torch2Y-0.125f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.5625f,torch2Y,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.4375f,torch2Y,0,sprite_torch_head.getMinU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);
        matrixStack.pop();

        //draw back side
        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-0.25,0);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

        matrixStack.push();
        //draw static torch side
        matrixStack.translate(.4375f,.25,-0.75f);
        add(builder,matrixStack,0f,0f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0.125f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0f,0.125f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);

        //draw moving torch side
        matrixStack.translate(0,0,.875-torch2Y);
        add(builder,matrixStack,0f,0f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0.125f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0f,0.125f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);

        matrixStack.pop();

        //right side
        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

        matrixStack.push();
        //draw static torch side
        matrixStack.translate(.75,0.25f,-.4375f);
        add(builder,matrixStack,0f,0f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0.125f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0f,0.125f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);

        //draw moving torch side
        matrixStack.translate(torch2Y-.875,0,0);
        add(builder,matrixStack,0f,0f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0.125f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0f,0.125f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);

        matrixStack.pop();

        //front side
        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);
        matrixStack.push();
        //draw static torch front
        matrixStack.translate(.4375f,.25,-0.125f);
        add(builder,matrixStack,0f,0f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0.125f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0f,0.125f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);

        //draw moving torch front
        matrixStack.translate(0,0,torch2Y-.875);
        add(builder,matrixStack,0f,0f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0.125f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0f,0.125f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);

        matrixStack.pop();


        //left side
        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

        matrixStack.push();
        //draw static torch side
        matrixStack.translate(.125,0.25f,-.4375f);
        add(builder,matrixStack,0f,0f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0.125f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0f,0.125f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);

        //draw moving torch side
        matrixStack.translate(.875-torch2Y,0,0);
        add(builder,matrixStack,0f,0f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMaxV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0.125f,0.125f,0,sprite_torch_head.getMaxU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);
        add(builder,matrixStack,0f,0.125f,0,sprite_torch_head.getMinU(), sprite_torch_head.getMinV(), combinedLight,combinedOverlay);

        matrixStack.pop();


    }

    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v, int combinedLightIn, int combinedOverlayIn) {
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
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
    public boolean neighborChanged(@Nullable PanelCellNeighbor frontNeighbor,@Nullable PanelCellNeighbor rightNeighbor,@Nullable  PanelCellNeighbor backNeighbor,@Nullable  PanelCellNeighbor leftNeighbor)
    {
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
     * @param outputDirection
     * @return integer 0-15 indicating the strengh of redstone signal
     */
    @Override
    public int getWeakRsOutput(PanelCellSide outputDirection)
    {
        return getStrongRsOutput(outputDirection);
    }
    @Override
    public int getStrongRsOutput(PanelCellSide outputDirection) {
        if (output&&outputDirection==PanelCellSide.FRONT)
            return 15;
        else
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
        while (this.queue.size()<this.ticks || this.queue.size()<1)
            this.queue.add(this.input);

        while (this.queue.size()>this.ticks)
            this.queue.remove();

        Boolean newOutput = this.queue.remove();
        this.queue.add(input);

        if (!this.locked && this.output!=newOutput)
        {
            this.output=newOutput;
            return true;
        }

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
        if (ticks<8)
        {
            ticks+=2;
            return true;
        }
        else if (panelTile.getWorld().isRemote)
            RepeaterCellGUI.open(panelTile,cellIndex,this);
        return false;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("output",output);
        nbt.putBoolean("input",input);
        nbt.putBoolean("locked",locked);

        String queueString = "";
        int i=0;
        for (Object b : queue.toArray())
        {
            queueString += ((Boolean)b)?"1":"0";
        }
        nbt.putString("queue",queueString);


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
        if (ticks<2)this.ticks=2;
        else if(ticks>200)this.ticks=200;
        else this.ticks=ticks;
    }
}

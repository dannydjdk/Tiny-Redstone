package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.gui.RepeaterCellGUI;
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

import java.util.LinkedList;

public class Repeater implements IPanelCell
{
    private boolean input = false;
    private boolean output = false;
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

        if (!this.output){
            sprite_repeater = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_REPEATER_OFF);
        }


        matrixStack.translate(0,0,0.25);

        add(builder, matrixStack, 0,0,0, sprite_repeater.getMinU(), sprite_repeater.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite_repeater.getMaxU(), sprite_repeater.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,1,0, sprite_repeater.getMaxU(), sprite_repeater.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,1,0, sprite_repeater.getMinU(), sprite_repeater.getMinV(),combinedLight,combinedOverlay);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-0.25,0);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0.25f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,0.25f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

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
     * Responding to the redstone signal output of an adjacent cells.
     * This can be called up to 16 times in a redstone tick (1/10th second).
     *
     * @param rsFrontStrong strength of incoming redstone signal from Front
     * @param rsRightStrong strength of incoming redstone signal from Right
     * @param rsBackStrong  strength of incoming redstone signal from Back
     * @param rsLeftStrong  strength of incoming redstone signal from Left
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean inputRs(int rsFrontStrong, int rsRightStrong, int rsBackStrong, int rsLeftStrong,int rsFrontWeak, int rsRightWeak, int rsBackWeak, int rsLeftWeak)
    {
        if (rsBackStrong+rsBackWeak >0 && !input)
        {
            input=true;
        }
        else if (rsBackStrong+rsBackWeak ==0 && input)
        {
            input=false;
        }

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

        if (this.output!=newOutput)
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
        if (panelTile.getWorld().isRemote)
            RepeaterCellGUI.open(panelTile,cellIndex,this);
        return false;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("output",output);
        nbt.putBoolean("input",input);
        CompoundNBT queueNBT = new CompoundNBT();
        int i=0;
        for (Object b : queue.toArray())
        {
            if (b instanceof Boolean){
                queueNBT.putBoolean(i+"",(Boolean)b);
                i++;
            }
        }
        nbt.put("queue",queueNBT);
        nbt.putInt("ticks",this.ticks);

        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.output = compoundNBT.getBoolean("output");
        this.input = compoundNBT.getBoolean("input");
        this.ticks = compoundNBT.getInt("ticks");
        if (compoundNBT.contains("queue"))
        {
            CompoundNBT queueNBT = compoundNBT.getCompound("queue");
            for(int i=0 ; i<this.ticks ; i++)
            {
                if (queueNBT.contains(i+"")) {
                    this.queue.add(queueNBT.getBoolean(i + ""));
                }
                else
                {
                    this.queue.add(false);
                }
            }
        }

    }

    public Integer getTicks() {
        return this.ticks;
    }
    public void setTicks(Integer ticks){
        if (ticks<1)this.ticks=1;
        else this.ticks=ticks;
    }
}

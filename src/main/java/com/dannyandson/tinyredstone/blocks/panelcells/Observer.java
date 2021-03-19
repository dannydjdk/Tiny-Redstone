package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.blocks.IObservingPanelCell;
import com.dannyandson.tinyredstone.blocks.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellNeighbor;
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

import java.util.LinkedList;

public class Observer implements IPanelCell, IObservingPanelCell {

    public static ResourceLocation TEXTURE_OBSERVER_TOP      = new ResourceLocation("minecraft","block/observer_top");
    public static ResourceLocation TEXTURE_OBSERVER_BACK_ON  = new ResourceLocation("minecraft","block/observer_back_on");
    public static ResourceLocation TEXTURE_OBSERVER_BACK     = new ResourceLocation("minecraft","block/observer_back");
    public static ResourceLocation TEXTURE_OBSERVER_FRONT    = new ResourceLocation("minecraft","block/observer_front");
    public static ResourceLocation TEXTURE_OBSERVER_SIDE     = new ResourceLocation("minecraft","block/observer_side");


    boolean output = false;
    private LinkedList<Boolean> queue = new LinkedList<>();
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

        TextureAtlasSprite sprite_top = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_OBSERVER_TOP);
        TextureAtlasSprite sprite_back;
        if (output)
            sprite_back = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_OBSERVER_BACK_ON);
        else
            sprite_back = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_OBSERVER_BACK);
        TextureAtlasSprite sprite_front = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_OBSERVER_FRONT);
        TextureAtlasSprite sprite_side = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_OBSERVER_SIDE);

        matrixStack.translate(0,0,1.0);
        matrixStack.push();
        matrixStack.translate(1,1,0);
        matrixStack.rotate(Vector3f.ZN.rotationDegrees(180));
        addRectangle(builder,matrixStack,sprite_top,combinedLight,combinedOverlay,alpha);
        matrixStack.pop();

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        addRectangle(builder,matrixStack,sprite_back,combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        addRectangle(builder,matrixStack,sprite_side,combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        addRectangle(builder,matrixStack,sprite_front,combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        addRectangle(builder,matrixStack,sprite_side,combinedLight,combinedOverlay,alpha);

    }

    private void addRectangle(IVertexBuilder builder, MatrixStack matrixStack, TextureAtlasSprite sprite,int combinedLight, int combinedOverlay, float alpha)
    {
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,1,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,1,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
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
     * Passes PanelCellNeighbor objects - an object wrapping another IPanelCell or a BlockState
     * WARNING! Check for null values!
     *
     * @param frontNeighbor object to access info about front neighbor or NULL if no neighbor exists
     * @param rightNeighbor object to access info about right neighbor or NULL if no neighbor exists
     * @param backNeighbor  object to access info about back neighbor or NULL if no neighbor exists
     * @param leftNeighbor  object to access info about left neighbor or NULL if no neighbor exists
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean neighborChanged(PanelCellNeighbor frontNeighbor, PanelCellNeighbor rightNeighbor, PanelCellNeighbor backNeighbor, PanelCellNeighbor leftNeighbor) {
        return false;
    }

    /**
     * Gets redstone output of the given side of the cell
     *
     * @param outputDirection (1=Front,2=Right,3=Back,4=Left)
     * @return integer 0-15 indicating the strength of redstone signal
     */
    @Override
    public int getWeakRsOutput(PanelCellSide outputDirection) {
        return (outputDirection==PanelCellSide.BACK && output)?15:0;
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
        return false;
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
        return true;
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
     * Called each each tick.
     *
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick()
    {
        if (queue.size()>0)
        {
            return setOutput(queue.remove());
        }
        if (output)
        {
            output=false;
            return true;
        }

        return false;
    }

    private boolean setOutput(boolean output)
    {
        if (this.output!=output)
        {
            this.output=output;
            return true;
        }
        return false;
    }

    /**
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param panelTile      the activated PanelTile tile entity that contains this cell
     * @param cellIndex      The index of the clicked IPanelCell within the panel (this IPanelCell)
     * @param segmentClicked Which of nine segment within the cell were clicked. 0 through 8 where 0 is front-right and 8 is back-left;
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelTile panelTile, Integer cellIndex, Integer segmentClicked) {
        return false;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("output",output);

        String queueString = "";
        int i=0;
        for (Object b : queue.toArray())
        {
            queueString += ((Boolean)b)?"1":"0";
        }
        nbt.putString("queue",queueString);

        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.output = compoundNBT.getBoolean("output");
        String queueString = compoundNBT.getString("queue");
        for (Byte b : queueString.getBytes())
        {
            queue.add(b==49);
        }
    }

    @Override
    public boolean frontNeighborUpdated() {
        queue.clear();

        queue.add(output);
        queue.add(output);
        queue.add(true);
        queue.add(true);
        queue.add(true);
        queue.add(true);

        return false;
    }
}

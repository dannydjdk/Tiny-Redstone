package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
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

public class Piston implements IPanelCell {

    public static ResourceLocation TEXTURE_PISTON_SIDE = new ResourceLocation("minecraft","block/piston_side");
    public static ResourceLocation TEXTURE_PISTON_SIDE_TOP = new ResourceLocation(TinyRedstone.MODID,"block/piston_side_top");
    public static ResourceLocation TEXTURE_PISTON_SIDE_BOTTOM = new ResourceLocation(TinyRedstone.MODID,"block/piston_side_bottom");
    public static ResourceLocation TEXTURE_PISTON_TOP = new ResourceLocation("minecraft","block/piston_top");
    public static ResourceLocation TEXTURE_PISTON_BOTTOM = new ResourceLocation("minecraft","block/piston_bottom");
    public static ResourceLocation TEXTURE_PISTON_INNER = new ResourceLocation("minecraft","block/piston_inner");

    protected boolean extended = false;
    protected int changePending = -1;

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {

        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_PISTON_SIDE);
        TextureAtlasSprite sprite_bottom = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_PISTON_BOTTOM);
        TextureAtlasSprite sprite_inner = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_PISTON_INNER);
        TextureAtlasSprite sprite_inner_top = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_PISTON_TOP);


        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)?RenderType.getSolid():RenderType.getTranslucent());
        TextureAtlasSprite sprite_top = getSprite_top();

        boolean renderExtended = (extended && changePending==-1) || (!extended && changePending!=-1);


        matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
        matrixStack.translate(-1,-1,0);

        //draw top
        matrixStack.push();
        matrixStack.translate(0,0,1.0);
        drawSide(matrixStack,builder,sprite,combinedLight,combinedOverlay, alpha);
        matrixStack.pop();

        //draw right side
        matrixStack.push();
        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-1,0,1);
        drawSide(matrixStack,builder,sprite,combinedLight,combinedOverlay, alpha);
        matrixStack.pop();

        //draw left side
        matrixStack.push();
        matrixStack.rotate(Vector3f.YP.rotationDegrees(-90));
        matrixStack.translate(0,0,0);
        drawSide(matrixStack,builder,sprite,combinedLight,combinedOverlay, alpha);
        matrixStack.pop();

        //draw front (bottom texture of piston)
        matrixStack.push();
        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,0,0);
        add(builder, matrixStack, 0,0,0, sprite_bottom.getMinU(), sprite_bottom.getMaxV(),combinedLight,combinedOverlay, alpha);
        add(builder, matrixStack, 1,0,0, sprite_bottom.getMaxU(), sprite_bottom.getMaxV(),combinedLight,combinedOverlay, alpha);
        add(builder, matrixStack, 1,1,0, sprite_bottom.getMaxU(), sprite_bottom.getMinV(),combinedLight,combinedOverlay, alpha);
        add(builder, matrixStack, 0,1,0, sprite_bottom.getMinU(), sprite_bottom.getMinV(),combinedLight,combinedOverlay, alpha);
        if (renderExtended)
        {
            matrixStack.translate(0,0,-1.75);
            add(builder, matrixStack, 0,0,0, sprite_inner_top.getMinU(), sprite_inner_top.getMaxV(),combinedLight,combinedOverlay, alpha);
            add(builder, matrixStack, 1,0,0, sprite_inner_top.getMaxU(), sprite_inner_top.getMaxV(),combinedLight,combinedOverlay, alpha);
            add(builder, matrixStack, 1,1,0, sprite_inner_top.getMaxU(), sprite_inner_top.getMinV(),combinedLight,combinedOverlay, alpha);
            add(builder, matrixStack, 0,1,0, sprite_inner_top.getMinU(), sprite_inner_top.getMinV(),combinedLight,combinedOverlay, alpha);
        }
        matrixStack.pop();

        //draw back (top texture of piston)
        matrixStack.rotate(Vector3f.XP.rotationDegrees(-90));
        matrixStack.translate(0,-1, 1);
        if (renderExtended)
        {
            matrixStack.translate(0,0,-0.25);
            add(builder, matrixStack, 0, 0, 0, sprite_inner.getMinU(), sprite_inner.getMaxV(), combinedLight, combinedOverlay, alpha);
            add(builder, matrixStack, 1, 0, 0, sprite_inner.getMaxU(), sprite_inner.getMaxV(), combinedLight, combinedOverlay, alpha);
            add(builder, matrixStack, 1, 1, 0, sprite_inner.getMaxU(), sprite_inner.getMinV(), combinedLight, combinedOverlay, alpha);
            add(builder, matrixStack, 0, 1, 0, sprite_inner.getMinU(), sprite_inner.getMinV(), combinedLight, combinedOverlay, alpha);
            matrixStack.translate(0,0,1.25);
        }
        add(builder, matrixStack, 0, 0, 0, sprite_top.getMinU(), sprite_top.getMaxV(), combinedLight, combinedOverlay, alpha);
        add(builder, matrixStack, 1, 0, 0, sprite_top.getMaxU(), sprite_top.getMaxV(), combinedLight, combinedOverlay, alpha);
        add(builder, matrixStack, 1, 1, 0, sprite_top.getMaxU(), sprite_top.getMinV(), combinedLight, combinedOverlay, alpha);
        add(builder, matrixStack, 0, 1, 0, sprite_top.getMinU(), sprite_top.getMinV(), combinedLight, combinedOverlay, alpha);

    }

    private void drawSide(MatrixStack matrixStack,IVertexBuilder builder,TextureAtlasSprite sprite,int combinedLight,int combinedOverlay, float alpha)
    {
        boolean renderExtended = (extended && changePending==-1) || (!extended && changePending!=-1);
        TextureAtlasSprite sprite_side_top = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_PISTON_SIDE_TOP);
        TextureAtlasSprite sprite_side_bottom = (renderExtended)
                ?Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_PISTON_SIDE_BOTTOM)
                :Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_PISTON_SIDE);


        matrixStack.push();

        if (renderExtended)
            matrixStack.scale(1,.75f,1);
        add(builder, matrixStack, 0,0,0, sprite_side_bottom.getMinU(), sprite_side_bottom.getMaxV(),combinedLight,combinedOverlay, alpha);
        add(builder, matrixStack, 1,0,0, sprite_side_bottom.getMaxU(), sprite_side_bottom.getMaxV(),combinedLight,combinedOverlay, alpha);
        add(builder, matrixStack, 1,1,0, sprite_side_bottom.getMaxU(), sprite_side_bottom.getMinV(),combinedLight,combinedOverlay, alpha);
        add(builder, matrixStack, 0,1,0, sprite_side_bottom.getMinU(), sprite_side_bottom.getMinV(),combinedLight,combinedOverlay, alpha);
        matrixStack.pop();

        if (renderExtended)
        {
            matrixStack.push();

            matrixStack.translate(0,1.75,0);
            matrixStack.scale(1,.25f,1);
            add(builder, matrixStack, 0,0,0, sprite_side_top.getMinU(), sprite_side_top.getMaxV(),combinedLight,combinedOverlay, alpha);
            add(builder, matrixStack, 1,0,0, sprite_side_top.getMaxU(), sprite_side_top.getMaxV(),combinedLight,combinedOverlay, alpha);
            add(builder, matrixStack, 1,1,0, sprite_side_top.getMaxU(), sprite_side_top.getMinV(),combinedLight,combinedOverlay, alpha);
            add(builder, matrixStack, 0,1,0, sprite_side_top.getMinU(), sprite_side_top.getMinV(),combinedLight,combinedOverlay, alpha);

            matrixStack.scale(.25f,4,1);
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(90));
            matrixStack.translate(-1,-2.5,-0.375);
            add(builder, matrixStack, 0,0,0, sprite_side_top.getMinU(), sprite_side_top.getMaxV(),combinedLight,combinedOverlay, alpha);
            add(builder, matrixStack, 1,0,0, sprite_side_top.getMaxU(), sprite_side_top.getMaxV(),combinedLight,combinedOverlay, alpha);
            add(builder, matrixStack, 1,1,0, sprite_side_top.getMaxU(), sprite_side_top.getMinV(),combinedLight,combinedOverlay, alpha);
            add(builder, matrixStack, 0,1,0, sprite_side_top.getMinU(), sprite_side_top.getMinV(),combinedLight,combinedOverlay, alpha);

            matrixStack.pop();
        }

    }
    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v, int combinedLightIn, int combinedOverlayIn, float alpha) {
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, alpha)
                .tex(u, v)
                .lightmap(combinedLightIn)
                .normal(1, 0, 0)
                .overlay(combinedOverlayIn)
                .endVertex();

    }
    protected TextureAtlasSprite getSprite_top()
    {
        return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_PISTON_TOP);
    }

    public boolean isExtended(){
        return this.extended;
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
        boolean extend =
                (
                        ( rightNeighbor!=null && rightNeighbor.getWeakRsOutput()>0) ||
                                ( frontNeighbor!=null && frontNeighbor.getWeakRsOutput()>0) ||
                                ( leftNeighbor!=null && leftNeighbor.getWeakRsOutput()>0)
                )
                &&
                        (backNeighbor == null || backNeighbor.isOnPanel())
                &&
                ( extended ||
                        (
                                backNeighbor == null || ( backNeighbor.getNeighborIPanelCell() != null &&
                                backNeighbor.isPushable() )
                        )
                );
        if (extend!=this.extended)
        {
            this.extended=extend;
            this.changePending=2;
        }
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
        return 0;
    }

    @Override
    public int getStrongRsOutput(PanelCellSide outputDirection) {
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
        return !extended;
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
    public boolean tick() {
        if (changePending < 0)
            return false;
        if (changePending > 0) {
            changePending--;
            return false;
        }
        changePending--;
        return true;
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
        nbt.putBoolean("extended",this.extended);
        nbt.putInt("changePending",this.changePending);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.extended=compoundNBT.getBoolean("extended");
        this.changePending=compoundNBT.getInt("changePending");
    }
}

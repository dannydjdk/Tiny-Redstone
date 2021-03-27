package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.compat.theoneprobe.ProbeInfoHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class Button implements IPanelCell, IPanelCellProbeInfoProvider {

    public static ResourceLocation TEXTURE_OAK_PLANKS = new ResourceLocation("minecraft","block/oak_planks");

    protected boolean active = false;
    protected Integer ticksRemaining = 0;

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

        TextureAtlasSprite sprite = getSprite();
        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)?RenderType.getSolid():RenderType.getTranslucent());

        matrixStack.translate(0,0,(active)?0.0625:0.125);
        float x1 = 0.3125f, x2 = .6875f, y1 = .375f, y2 = .625f;
        //matrixStack.scale(.375f,.375f,1);

        add(builder, matrixStack, x1,y1,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,y1,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,y2,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x1,y2,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-0.125,-y1);
        add(builder, matrixStack, x1,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,0.125f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x1,0.125f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,.6875);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0.25f,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0.25f,0.125f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,0.125f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,.25);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, .375f,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, .375f,0.125f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,0.125f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,0.375);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0.25f,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0.25f,0.125f,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,0.125f,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

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
        return (active)?15:0;
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
        return true;
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
     * Called each each tick.
     *
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick() {
        if (active && ticksRemaining >0)
        {
            ticksRemaining--;
            if (ticksRemaining ==0)
            {
                active=false;
                return true;
            }
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
    public boolean onBlockActivated(PanelTile panelTile, Integer cellIndex, PanelCellSegment segmentClicked) {
        if (!active)
        {
            this.active=true;
            this.ticksRemaining =30;
            return true;
        }
        return false;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("active",this.active);
        nbt.putInt("ticksRemaining",this.ticksRemaining);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.active=compoundNBT.getBoolean("active");
        this.ticksRemaining=compoundNBT.getInt("ticksRemaining");
    }

    protected TextureAtlasSprite getSprite()
    {
        return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_OAK_PLANKS);
    }

    @Override
    public boolean addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PanelTile panelTile, PosInPanelCell pos) {
        ProbeInfoHelper.addPower(probeInfo, this.active ? 15 : 0);
        return true;
    }
}

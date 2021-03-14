package com.dannyandson.tinyredstone.blocks.panelcells;

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

public class RedstoneLamp  implements IPanelCell {

    public static ResourceLocation TEXTURE_REDSTONE_LAMP = new ResourceLocation("minecraft","block/redstone_lamp");
    public static ResourceLocation TEXTURE_REDSTONE_LAMP_ON = new ResourceLocation("minecraft","block/redstone_lamp_on");

    private int weakSignalStrength = 0;
    private int strongSignalStrength = 0;

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
        TextureAtlasSprite sprite;

        if (weakSignalStrength+strongSignalStrength>0) sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_REDSTONE_LAMP_ON);
        else sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_REDSTONE_LAMP);

        matrixStack.translate(0,0,1.0);

        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,1,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,1,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,1,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,1,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,1,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,1,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,1,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,1,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,1,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,1,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay);

    }

    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v, int combinedLightIn, int combinedOverlayIn) {
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .tex(u, v)
                .lightmap((weakSignalStrength+strongSignalStrength>0)?15728880:combinedLightIn)
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

        int weak=0,strong=0;
        if (frontNeighbor!=null) {
            if (frontNeighbor.powerDrops())
                weak = frontNeighbor.getWeakRsOutput();
            else if (!frontNeighbor.isPushable())
                strong = frontNeighbor.getStrongRsOutput();
        }
        if (rightNeighbor!=null) {
            if (rightNeighbor.powerDrops())
                weak = Math.max(weak,rightNeighbor.getWeakRsOutput());
            else if (!rightNeighbor.isPushable())
                strong = Math.max(strong,rightNeighbor.getStrongRsOutput());
        }
        if (backNeighbor!=null) {
            if (backNeighbor.powerDrops())
                weak = Math.max(weak,backNeighbor.getWeakRsOutput());
            else if (!backNeighbor.isPushable())
                strong = Math.max(strong,backNeighbor.getStrongRsOutput());
        }
        if (leftNeighbor!=null) {
            if (leftNeighbor.powerDrops())
                weak = Math.max(weak,leftNeighbor.getWeakRsOutput());
            else if (!leftNeighbor.isPushable())
                strong = Math.max(strong,leftNeighbor.getStrongRsOutput());
        }

        weak=Math.max(weak,strong);

        if (weak!=this.weakSignalStrength ||strong!=this.strongSignalStrength)
        {
            this.weakSignalStrength =weak;
            this.strongSignalStrength=strong;
            return true;
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
        return this.weakSignalStrength;
    }

    @Override
    public int getStrongRsOutput(PanelCellSide outputDirection) {
        return this.strongSignalStrength;
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
        return true;
    }

    /**
     * If this cell outputs light, return the level here. Otherwise, return 0.
     *
     * @return Light level to output 0-15
     */
    @Override
    public int lightOutput() {
        return (weakSignalStrength+strongSignalStrength>0)?1:0;
    }

    /**
     * Called each each tick.
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
        nbt.putInt("strong",this.strongSignalStrength);
        nbt.putInt("weak",this.weakSignalStrength);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.strongSignalStrength=compoundNBT.getInt("strong");
        this.weakSignalStrength=compoundNBT.getInt("weak");
    }
}

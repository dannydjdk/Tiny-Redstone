package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RedstoneLamp extends TinyBlock implements IPanelCell {

    private boolean lit = false;

    public static ResourceLocation TEXTURE_REDSTONE_LAMP = new ResourceLocation("minecraft","block/redstone_lamp");
    public static ResourceLocation TEXTURE_REDSTONE_LAMP_ON = new ResourceLocation("minecraft","block/redstone_lamp_on");

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
        TextureAtlasSprite sprite;

        if (lit) sprite = RenderHelper.getSprite(TEXTURE_REDSTONE_LAMP_ON);
        else sprite = RenderHelper.getSprite(TEXTURE_REDSTONE_LAMP);

        matrixStack.translate(0,0,1.0);

        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,1,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,1,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,1,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,1,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,1,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,1,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,1,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,1,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        add(builder, matrixStack, 0,0,0, sprite.getMinU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,0,0, sprite.getMaxU(), sprite.getMaxV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 1,1,0, sprite.getMaxU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, 0,1,0, sprite.getMinU(), sprite.getMinV(),combinedLight,combinedOverlay,alpha);

    }

    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v, int combinedLightIn, int combinedOverlayIn, float alpha) {
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(ColorHelper.PackedColor.getRed(color),ColorHelper.PackedColor.getGreen(color), ColorHelper.PackedColor.getBlue(color), (int)(alpha*255f))
                .tex(u, v)
                .lightmap((weakSignalStrength+strongSignalStrength>0)?15728880:combinedLightIn)
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
    public boolean neighborChanged(PanelCellPos cellPos){

        PanelCellNeighbor rightNeighbor = cellPos.getNeighbor(Side.BACK),
                leftNeighbor = cellPos.getNeighbor(Side.LEFT),
                backNeighbor = cellPos.getNeighbor(Side.BACK),
                frontNeighbor = cellPos.getNeighbor(Side.FRONT),
                topNeighbor = cellPos.getNeighbor(Side.TOP),
                bottomNeighbor = cellPos.getNeighbor(Side.BOTTOM);

        boolean change = super.neighborChanged(cellPos);

        if (
                (weakSignalStrength + strongSignalStrength > 0) ||
                        ((
                                ((frontNeighbor != null) ? frontNeighbor.getWeakRsOutput() : 0) +
                                        ((rightNeighbor != null) ? rightNeighbor.getWeakRsOutput() : 0) +
                                        ((backNeighbor != null) ? backNeighbor.getWeakRsOutput() : 0) +
                                        ((leftNeighbor != null) ? leftNeighbor.getWeakRsOutput() : 0)+
                                        ((topNeighbor != null) ? topNeighbor.getWeakRsOutput() : 0)+
                                        ((bottomNeighbor != null) ? bottomNeighbor.getWeakRsOutput() : 0)) > 0)
        ) {
            if (!this.lit) {
                this.lit = true;
                return true;
            }
        } else if (this.lit) {
            this.lit = false;
            return true;
        }

        return change;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = super.writeNBT();
        nbt.putBoolean("lit",this.lit);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        super.readNBT(compoundNBT);
        this.lit= compoundNBT.getBoolean("lit");
    }

    /**
     * If this cell outputs light, return the level here. Otherwise, return 0.
     *
     * @return Light level to output 0-15
     */
    @Override
    public int lightOutput() {
        return (this.lit)?1:0;
    }

}

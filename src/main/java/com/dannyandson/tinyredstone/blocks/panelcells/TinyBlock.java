package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.compat.theoneprobe.ProbeInfoHelper;
import com.dannyandson.tinyredstone.gui.TinyBlockGUI;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class TinyBlock implements IPanelCell, IColorablePanelCell, IPanelCellProbeInfoProvider {

    public static ResourceLocation TEXTURE_TINY_BLOCK = new ResourceLocation("minecraft","block/white_wool");

    protected int weakSignalStrength = 0;
    protected int strongSignalStrength = 0;
    protected int color= DyeColor.WHITE.getColorValue();

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
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_TINY_BLOCK);



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

        int weak=0,strong=0;
        if (frontNeighbor!=null) {
            if (frontNeighbor.powerDrops())
                weak = frontNeighbor.getWeakRsOutput();
            else if (!(frontNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
                strong = frontNeighbor.getStrongRsOutput();
        }
        if (rightNeighbor!=null) {
            if (rightNeighbor.powerDrops())
                weak = Math.max(weak,rightNeighbor.getWeakRsOutput());
            else if (!(rightNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
                strong = Math.max(strong,rightNeighbor.getStrongRsOutput());
        }
        if (backNeighbor!=null) {
            if (backNeighbor.powerDrops())
                weak = Math.max(weak,backNeighbor.getWeakRsOutput());
            else if (!(backNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
                strong = Math.max(strong,backNeighbor.getStrongRsOutput());
        }
        if (leftNeighbor!=null) {
            if (leftNeighbor.powerDrops())
                weak = Math.max(weak,leftNeighbor.getWeakRsOutput());
            else if (!(leftNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
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
    public int getWeakRsOutput(Side outputDirection) {
        return this.weakSignalStrength;
    }

    @Override
    public int getStrongRsOutput(Side outputDirection) {
        return this.strongSignalStrength;
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
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param cellPos The position of the clicked IPanelCell within the panel (this IPanelCell)
     * @param segmentClicked Which of nine segment within the cell were clicked.
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked) {
        PanelTile panelTile = cellPos.getPanelTile();
        if(panelTile.getWorld().isRemote)
            TinyBlockGUI.open(panelTile, cellPos.getIndex(), this);
        return false;
    }

    @Override
    public boolean hasActivation(){return true;}

    @Override
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("strong",this.strongSignalStrength);
        nbt.putInt("weak",this.weakSignalStrength);
        nbt.putInt("color",this.color);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.strongSignalStrength=compoundNBT.getInt("strong");
        this.weakSignalStrength=compoundNBT.getInt("weak");
        this.color=compoundNBT.getInt("color");
    }

    @Override
    public boolean addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PanelTile panelTile, PosInPanelCell pos) {
        ProbeInfoHelper.addPower(probeInfo, this.weakSignalStrength);
        return true;
    }
}

package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.Config;
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

public class Torch implements IPanelCell
{
    private boolean output = false;

    public static ResourceLocation TEXTURE_TORCH_ON = new ResourceLocation("minecraft","block/redstone_torch");
    public static ResourceLocation TEXTURE_TORCH_OFF = new ResourceLocation("minecraft","block/redstone_torch_off");


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
        TextureAtlasSprite sprite_torch = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_TORCH_ON);

        if (this.output) {
            sprite_torch = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_TORCH_ON);
        }
        else
        {
            sprite_torch = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_TORCH_OFF);
        }

        float u1 = sprite_torch.getMinU() + 0.0068359375f;
        float u2 = sprite_torch.getMaxU() - 0.0068359375f;
        float v1 = sprite_torch.getMinV() + 0.01171875f;
        float v2 = sprite_torch.getMaxV();

        float x1 = 0.375f;
        float x2 = 0.625f;
        float y1 = 0;
        float y2 = 1f;

        matrixStack.rotate(Vector3f.XP.rotationDegrees(60));
        matrixStack.translate(0,0.03125f,0);


        add(builder, matrixStack, x1,y1,0, u1, v2,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,y1,0, u2, v2,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,y2,0, u2, v1,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x1,y2,0, u1, v1,combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        add(builder, matrixStack, x1,y1,0, u1, v2,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,y1,0, u2, v2,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,y2,0, u2, v1,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x1,y2,0, u1, v1,combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        add(builder, matrixStack, x1,y1,0, u1, v2,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,y1,0, u2, v2,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,y2,0, u2, v1,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x1,y2,0, u1, v1,combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        add(builder, matrixStack, x1,y1,0, u1, v2,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,y1,0, u2, v2,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,y2,0, u2, v1,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x1,y2,0, u1, v1,combinedLight,combinedOverlay,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(-90));
        matrixStack.translate(0,-x1,y2);
        add(builder, matrixStack, x1,x1,0, u1, v2-0.01953125f,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,x1,0, u2, v2-0.01953125f,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x2,x2,0, u2, v1,combinedLight,combinedOverlay,alpha);
        add(builder, matrixStack, x1,x2,0, u1, v1,combinedLight,combinedOverlay,alpha);


    }

    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v, int combinedLightIn, int combinedOverlayIn, float alpha) {
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, alpha)
                .tex(u, v)
                .lightmap((output)?15728880:combinedLightIn)
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
    public boolean neighborChanged(PanelCellNeighbor frontNeighbor, PanelCellNeighbor rightNeighbor, PanelCellNeighbor backNeighbor, PanelCellNeighbor leftNeighbor)
    {
        if (backNeighbor!=null && backNeighbor.getWeakRsOutput() >0 && output)
        {
            output=false;
            return true;
        }
        else if ((backNeighbor==null || backNeighbor.getWeakRsOutput() ==0) && !output)
        {
            output=true;
            return true;
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
        if (output&&outputDirection!=PanelCellSide.BACK)
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
        return (output && Config.TORCH_LIGHT.get())?1:0;
    }

    /**
     * Called at the beginning of each tick if isTicking() returned true on last call.
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
     * @param panelTile the activated PanelTile tile entity that contains this cell
     * @param cellIndex The index of the clicked IPanelCell within the panel
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
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.output = compoundNBT.getBoolean("output");
    }
}

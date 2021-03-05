package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.IPanelCell;
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

public class Torch implements IPanelCell
{
    private boolean output = false;

    public static ResourceLocation TEXTURE_TORCH_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_torch_on");
    public static ResourceLocation TEXTURE_TORCH_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_torch_off");


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
        //TODO 3d Torch render

        IVertexBuilder builder = buffer.getBuffer(RenderType.getTranslucent());
        TextureAtlasSprite sprite_torch = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_TORCH_ON);

        if (this.output) {
            sprite_torch = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_TORCH_ON);
        }
        else
        {
            sprite_torch = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_TORCH_OFF);
        }


        matrixStack.translate(0,0,0.01);

        add(builder, matrixStack, 0,0,0, sprite_torch.getMinU(), sprite_torch.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,0,0, sprite_torch.getMaxU(), sprite_torch.getMaxV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 1,1,0, sprite_torch.getMaxU(), sprite_torch.getMinV(),combinedLight,combinedOverlay);
        add(builder, matrixStack, 0,1,0, sprite_torch.getMinU(), sprite_torch.getMinV(),combinedLight,combinedOverlay);



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
        if (rsBackStrong+rsBackWeak >0 && output)
        {
            output=false;
            return true;
        }
        else if (rsBackStrong+rsBackWeak ==0 && !output)
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

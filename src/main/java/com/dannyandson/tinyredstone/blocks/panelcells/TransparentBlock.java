package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.setup.Registration;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class TransparentBlock extends TinyBlock {
    public static ResourceLocation TEXTURE_TRANSPARENT_BLOCK = new ResourceLocation("minecraft","block/glass");
    private TextureAtlasSprite sprite;
    private int color= 16777215;

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {

        IVertexBuilder builder = buffer.getBuffer((Minecraft.useShaderTransparency())?RenderType.solid():RenderType.translucent());
        if (sprite==null)
            sprite =(madeFrom!=null)? Registration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom,Side.FRONT):RenderHelper.getSprite(TEXTURE_TRANSPARENT_BLOCK);

        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrixStack.translate(-1, -1, 1);
        RenderHelper.drawCube(matrixStack,builder,sprite,combinedLight,combinedOverlay,color,alpha-.01f);
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
        return false;
    }

    /**
     * Gets redstone output of the given side of the cell
     *
     * @param outputDirection side of the panel toward which output is being queried
     * @return integer 0-15 indicating the strength of redstone signal
     */
    @Override
    public int getWeakRsOutput(Side outputDirection)
    {
        return 0;
    }
    @Override
    public int getStrongRsOutput(Side outputDirection) {
        return 0;
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

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("color",color);
        if (this.madeFrom!=null) {
            nbt.putString("made_from_namespace", this.madeFrom.getNamespace());
            nbt.putString("made_from_path", this.madeFrom.getPath());
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.color=compoundNBT.getInt("color");
        if (compoundNBT.contains("made_from_namespace"))
            this.madeFrom = new ResourceLocation(compoundNBT.getString("made_from_namespace"), compoundNBT.getString("made_from_path").replace("block/","") );
        //TODO: ".replace("block/","")" above added for backward compatibility. Can be removed after a couple releases.
    }

}

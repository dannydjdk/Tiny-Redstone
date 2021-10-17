package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.api.IColorablePanelCell;
import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TransparentBlock  implements IPanelCell, IColorablePanelCell, IPanelCellInfoProvider
{
    public static ResourceLocation TEXTURE_TRANSPARENT_BLOCK = new ResourceLocation("minecraft","block/glass");
    private ResourceLocation madeFrom;
    private TextureAtlasSprite sprite;
    private int color= 16777215;

    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     * @param buffer
     */
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, float alpha) {

        VertexConsumer builder = buffer.getBuffer((Minecraft.useShaderTransparency())?RenderType.solid():RenderType.translucent());
        if (sprite==null)
            sprite = RenderHelper.getSprite(madeFrom!=null?madeFrom:TEXTURE_TRANSPARENT_BLOCK);

        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrixStack.translate(-1, -1, 1);
        RenderHelper.drawCube(matrixStack,builder,sprite,combinedLight,color,alpha-.01f);

    }

    @Override
    public boolean onPlace(PanelCellPos cellPos, Player player) {
        ItemStack stack = ItemStack.EMPTY;
        if (player.getUsedItemHand() != null)
            stack = player.getItemInHand(player.getUsedItemHand());
        if (stack == ItemStack.EMPTY)
            stack = player.getMainHandItem();
        if (stack.hasTag()) {
            CompoundTag itemNBT = stack.getTag();
            CompoundTag madeFromTag = itemNBT.getCompound("made_from");
            if (madeFromTag.contains("namespace")) {
                this.madeFrom = new ResourceLocation(madeFromTag.getString("namespace"), "block/" + madeFromTag.getString("path"));
            }
        }

        return IPanelCell.super.onPlace(cellPos, player);
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

    /**
     * Can this cell be pushed by a piston?
     *
     * @return true if a piston can push this block
     */
    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("color",color);
        if (this.madeFrom!=null) {
            nbt.putString("made_from_namespace", this.madeFrom.getNamespace());
            nbt.putString("made_from_path", this.madeFrom.getPath());
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag compoundNBT) {
        this.color=compoundNBT.getInt("color");
        if (compoundNBT.contains("made_from_namespace"))
            this.madeFrom = new ResourceLocation(compoundNBT.getString("made_from_namespace"), compoundNBT.getString("made_from_path"));
    }

    @Override
    public void setColor(int color) {
        this.color=color;
    }

    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos) {
        overlayBlockInfo.setPowerOutput(0);
    }

    @Override
    public CompoundTag getItemTag() {
        if (this.madeFrom != null) {
            CompoundTag madeFromTag = new CompoundTag();
            madeFromTag.putString("namespace", this.madeFrom.getNamespace());
            madeFromTag.putString("path", this.madeFrom.getPath().substring(6));
            CompoundTag itemTag = new CompoundTag();
            itemTag.put("made_from", madeFromTag);
            return itemTag;
        }
        return null;
    }
}

package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IColorablePanelCell;
import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TinyBlock implements IPanelCell, IColorablePanelCell, IPanelCellInfoProvider {

    public static final Identifier TEXTURE_GRASS_BLOCK_TOP = Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "block/grass_block_top");
    public static final Identifier TEXTURE_TINY_BLOCK = Identifier.fromNamespaceAndPath("minecraft","block/white_wool");

    protected int weakSignalStrength = 0;
    protected int strongSignalStrength = 0;
    protected int color= 0xFFFFFFFF;
    protected Identifier madeFrom;
    protected TextureAtlasSprite sprite_top, sprite_front, sprite_right, sprite_back, sprite_left, sprite_bottom;

    /**
     * Returns the block Identifier this tiny block is made from, or null if using default textures.
     */
    public Identifier getMadeFrom() {
        return madeFrom;
    }

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
        VertexConsumer builder = buffer.getBuffer((alpha==1.0)? Sheets.cutoutBlockSheet():Sheets.translucentBlockSheet());
        if (sprite_top==null) {
            if (madeFrom != null){
                TextureAtlasSprite[] sprites = ModRegistration.TINY_BLOCK_OVERRIDES.getSprites(madeFrom);
                sprite_top    = sprites[Side.TOP.ordinal()];
                sprite_front  = sprites[Side.FRONT.ordinal()];
                sprite_right  = sprites[Side.RIGHT.ordinal()];
                sprite_back   = sprites[Side.BACK.ordinal()];
                sprite_left   = sprites[Side.LEFT.ordinal()];
                sprite_bottom = sprites[Side.BOTTOM.ordinal()];
            }else{
                sprite_top=sprite_front=sprite_right=sprite_back=sprite_left=sprite_bottom=RenderHelper.getSprite(TEXTURE_TINY_BLOCK);
            }
        }

        matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
        matrixStack.translate(-1, -1, 1);
        RenderHelper.drawCube(matrixStack,builder,sprite_top, sprite_front, sprite_right, sprite_back, sprite_left, sprite_bottom,combinedLight,color,alpha);
    }

    @Override
    public boolean onPlace(PanelCellPos cellPos, Player player) {
        ItemStack stack = ItemStack.EMPTY;
        if (player.getUsedItemHand() != null)
            stack = player.getItemInHand(player.getUsedItemHand());
        if (stack == ItemStack.EMPTY)
            stack = player.getMainHandItem();
        if (ItemStackHelper.getCustomTag(stack) != null) {
            CompoundTag itemNBT = ItemStackHelper.getCustomTag(stack);
            CompoundTag madeFromTag = itemNBT.getCompound("made_from").orElseGet(CompoundTag::new);
            if (madeFromTag.contains("namespace")) {
                this.madeFrom = Identifier.fromNamespaceAndPath(madeFromTag.getStringOr("namespace", ""), madeFromTag.getStringOr("path", ""));
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

        PanelCellNeighbor rightNeighbor = cellPos.getNeighbor(Side.RIGHT),
                leftNeighbor = cellPos.getNeighbor(Side.LEFT),
                backNeighbor = cellPos.getNeighbor(Side.BACK),
                frontNeighbor = cellPos.getNeighbor(Side.FRONT),
                topNeighbor = cellPos.getNeighbor(Side.TOP),
                bottomNeighbor = cellPos.getNeighbor(Side.BOTTOM);

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
        if (topNeighbor!=null) {
            if (topNeighbor.powerDrops())
                weak = Math.max(weak,topNeighbor.getWeakRsOutput());
            else if (!(topNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
                strong = Math.max(strong,topNeighbor.getStrongRsOutput());
        }
        if (bottomNeighbor!=null) {
            if (bottomNeighbor.powerDrops())
                weak = Math.max(weak,bottomNeighbor.getWeakRsOutput());
            else if (!(bottomNeighbor.getNeighborIPanelCell() instanceof TinyBlock))
                strong = Math.max(strong,bottomNeighbor.getStrongRsOutput());
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

    @Override
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("strong",this.strongSignalStrength);
        nbt.putInt("weak",this.weakSignalStrength);
        nbt.putInt("color",this.color);
        if (this.madeFrom!=null) {
            nbt.putString("made_from_namespace",this.madeFrom.getNamespace());
            nbt.putString("made_from_path",this.madeFrom.getPath());
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag compoundNBT) {
        this.strongSignalStrength=compoundNBT.getIntOr("strong", 0);
        this.weakSignalStrength=compoundNBT.getIntOr("weak", 0);
        this.color=compoundNBT.getIntOr("color", 0);
        if (compoundNBT.contains("made_from_namespace"))
            this.madeFrom=Identifier.fromNamespaceAndPath(compoundNBT.getStringOr("made_from_namespace", ""),compoundNBT.getStringOr("made_from_path", ""));
    }

    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos) {
        overlayBlockInfo.setPowerOutput(this.weakSignalStrength);
    }

    @Override
    public CompoundTag getItemTag() {
        if (this.madeFrom != null) {
            CompoundTag madeFromTag = new CompoundTag();
            madeFromTag.putString("namespace", this.madeFrom.getNamespace());
            madeFromTag.putString("path", this.madeFrom.getPath());
            CompoundTag itemTag = new CompoundTag();
            itemTag.put("made_from", madeFromTag);
            return itemTag;
        }
        return null;
    }
}
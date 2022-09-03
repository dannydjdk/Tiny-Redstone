package com.dannyandson.tinyredstone.blocks.panelcovers;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCover;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.dannyandson.tinyredstone.setup.Registration;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class DarkCover implements IPanelCover {

    public static ResourceLocation TEXTURE_DEFAULT_COVER = new ResourceLocation(TinyRedstone.MODID,"block/dark_cover");
    protected ResourceLocation madeFrom;
    protected TextureAtlasSprite sprite_top, sprite_front, sprite_right, sprite_back, sprite_left, sprite_bottom;

    private float x1 = 0, x2 = 1, y1 = 0, y2 = 1;

    /**
     * Drawing the cover on the panel
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, int color) {

        if (sprite_top == null) {
            if (madeFrom != null) {
                sprite_top = Registration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.TOP);
                sprite_front = Registration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.FRONT);
                sprite_right = Registration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.RIGHT);
                sprite_back = Registration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.BACK);
                sprite_left = Registration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.LEFT);
                sprite_bottom = Registration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.BOTTOM);
            } else {
                sprite_top = sprite_front = sprite_right = sprite_back = sprite_left = sprite_bottom = RenderHelper.getSprite(getDefaultResourceLocation());
            }
        }

        TextureAtlasSprite sprite = RenderHelper.getSprite(TEXTURE_DEFAULT_COVER);
        matrixStack.translate(0, y2, 1);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(270));
        RenderHelper.drawCube(matrixStack,buffer.getBuffer(RenderType.solid()),sprite_top, sprite_front, sprite_right, sprite_back, sprite_left, sprite_bottom,combinedLight, combinedOverlay, madeFrom != null?0x00FFFFFF:color,1f);

    }

    protected ResourceLocation getDefaultResourceLocation(){
        return TEXTURE_DEFAULT_COVER;
    }

    @Override
    public void onPlace(PanelTile panelTile, PlayerEntity player) {
        ItemStack stack = ItemStack.EMPTY;
        if (player.getUsedItemHand() != null)
            stack = player.getItemInHand(player.getUsedItemHand());
        if (stack == ItemStack.EMPTY)
            stack = player.getMainHandItem();
        if (stack.hasTag()) {
            CompoundNBT itemNBT = stack.getTag();
            CompoundNBT madeFromTag = itemNBT.getCompound("made_from");
            if (madeFromTag.contains("namespace")) {
                this.madeFrom = new ResourceLocation(madeFromTag.getString("namespace"), madeFromTag.getString("path"));
            }
        }

    }

    /**
     * Does this cover allows light output?
     *
     * @return true if cells can output light, false if not.
     */
    @Override
    public boolean allowsLightOutput() {
        return false;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (this.madeFrom!=null) {
            nbt.putString("made_from_namespace",this.madeFrom.getNamespace());
            nbt.putString("made_from_path",this.madeFrom.getPath());
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        if (compoundNBT.contains("made_from_namespace"))
            this.madeFrom=new ResourceLocation(compoundNBT.getString("made_from_namespace"),compoundNBT.getString("made_from_path"));
    }


    @Override
    public CompoundNBT getItemTag() {
        if (this.madeFrom != null) {
            CompoundNBT madeFromTag = new CompoundNBT();
            madeFromTag.putString("namespace", this.madeFrom.getNamespace());
            madeFromTag.putString("path", this.madeFrom.getPath());
            CompoundNBT itemTag = new CompoundNBT();
            itemTag.put("made_from", madeFromTag);
            return itemTag;
        }
        return null;
    }
}

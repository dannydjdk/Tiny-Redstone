package com.dannyandson.tinyredstone.blocks.panelcovers;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCover;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DarkCover implements IPanelCover {

    public static Identifier TEXTURE_DEFAULT_COVER = Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/dark_cover");
    protected Identifier madeFrom;
    protected TextureAtlasSprite sprite_top, sprite_front, sprite_right, sprite_back, sprite_left, sprite_bottom;

    private float x1 = 0, x2 = 1, y1 = 0, y2 = 1;

    /**
     * Drawing the cover on the panel
     */
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, int color) {


        if (sprite_top == null) {
            if (madeFrom != null) {
                sprite_top = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.TOP);
                sprite_front = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.FRONT);
                sprite_right = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.RIGHT);
                sprite_back = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.BACK);
                sprite_left = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.LEFT);
                sprite_bottom = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.BOTTOM);
            } else {
                sprite_top = sprite_front = sprite_right = sprite_back = sprite_left = sprite_bottom = RenderHelper.getSprite(getDefaultResourceLocation());
            }
        }

        TextureAtlasSprite sprite = RenderHelper.getSprite(TEXTURE_DEFAULT_COVER);
        matrixStack.translate(0, y2, 1);
        matrixStack.mulPose(Axis.XP.rotationDegrees(270));
        RenderHelper.drawCube(matrixStack,buffer.getBuffer(Sheets.cutoutBlockSheet()),sprite_top, sprite_front, sprite_right, sprite_back, sprite_left, sprite_bottom,combinedLight, madeFrom != null?0x00FFFFFF:color,1f);
    }

    protected Identifier getDefaultResourceLocation(){
        return TEXTURE_DEFAULT_COVER;
    }

    /**
     * Returns the block Identifier this cover is camouflaging as, or null if using default textures.
     */
    public Identifier getMadeFrom() {
        return madeFrom;
    }

    @Override
    public void onPlace(PanelTile panelTile, Player player) {
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
    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (this.madeFrom!=null) {
            nbt.putString("made_from_namespace",this.madeFrom.getNamespace());
            nbt.putString("made_from_path",this.madeFrom.getPath());
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag compoundNBT) {
        if (compoundNBT.contains("made_from_namespace"))
            this.madeFrom=Identifier.fromNamespaceAndPath(compoundNBT.getStringOr("made_from_namespace", ""),compoundNBT.getStringOr("made_from_path", ""));
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
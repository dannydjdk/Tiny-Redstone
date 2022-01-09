package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.dannyandson.tinyredstone.blocks.panelcovers.DarkCover;
import com.dannyandson.tinyredstone.blocks.panelcovers.LightCover;
import com.dannyandson.tinyredstone.setup.Registration;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class PanelCoverItemRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack poseStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {

        boolean isTransparent = stack.getItem() == Registration.PANEL_COVER_LIGHT.get();
        TextureAtlasSprite sprite_top, sprite_front, sprite_right, sprite_back, sprite_left, sprite_bottom;

        if (stack.hasTag()) {
            CompoundNBT itemNBT = stack.getTag();
            CompoundNBT madeFromTag = itemNBT.getCompound("made_from");
            if (madeFromTag.contains("namespace")) {
                ResourceLocation itemId = new ResourceLocation(madeFromTag.getString("namespace"), madeFromTag.getString("path"));
                sprite_top = Registration.TINY_BLOCK_OVERRIDES.getSprite(itemId, Side.TOP);
                sprite_front = Registration.TINY_BLOCK_OVERRIDES.getSprite(itemId, Side.FRONT);
                sprite_right = Registration.TINY_BLOCK_OVERRIDES.getSprite(itemId, Side.RIGHT);
                sprite_back = Registration.TINY_BLOCK_OVERRIDES.getSprite(itemId, Side.BACK);
                sprite_left = Registration.TINY_BLOCK_OVERRIDES.getSprite(itemId, Side.LEFT);
                sprite_bottom = Registration.TINY_BLOCK_OVERRIDES.getSprite(itemId, Side.BOTTOM);
            } else
                sprite_top = sprite_front = sprite_right = sprite_back = sprite_left = sprite_bottom = RenderHelper.getSprite(isTransparent ? LightCover.TEXTURE_LIGHT_COVER : DarkCover.TEXTURE_DEFAULT_COVER);

        } else
            sprite_top = sprite_front = sprite_right = sprite_back = sprite_left = sprite_bottom = RenderHelper.getSprite(isTransparent ? LightCover.TEXTURE_LIGHT_COVER : DarkCover.TEXTURE_DEFAULT_COVER);

        IVertexBuilder builder = buffer.getBuffer(isTransparent ? RenderType.translucent() : RenderType.solid());
        float alpha = isTransparent ? .99f : 1.0f;

        poseStack.pushPose();

        poseStack.mulPose(Vector3f.XP.rotationDegrees(-90));
        poseStack.translate(1, 0, 0);
        RenderHelper.drawCube(poseStack, builder, sprite_top, sprite_front, sprite_right, sprite_back, sprite_left, sprite_bottom, combinedLight, 0xFFFFFFFF, alpha);

        poseStack.popPose();

    }
}

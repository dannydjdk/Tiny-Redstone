package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.dannyandson.tinyredstone.blocks.panelcovers.DarkCover;
import com.dannyandson.tinyredstone.blocks.panelcovers.LightCover;
import com.dannyandson.tinyredstone.setup.Registration;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PanelCoverItemRenderer extends BlockEntityWithoutLevelRenderer {


    public PanelCoverItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

        boolean isTransparent = stack.getItem() == Registration.PANEL_COVER_LIGHT.get();
        TextureAtlasSprite sprite_top, sprite_front, sprite_right, sprite_back, sprite_left, sprite_bottom;

        if (stack.hasTag()) {
            CompoundTag itemNBT = stack.getTag();
            CompoundTag madeFromTag = itemNBT.getCompound("made_from");
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

        VertexConsumer builder = buffer.getBuffer(isTransparent ? RenderType.translucent() : RenderType.solid());
        float alpha = isTransparent ? .99f : 1.0f;

        poseStack.pushPose();

        poseStack.mulPose(Vector3f.XP.rotationDegrees(-90));
        poseStack.translate(1, 0, 0);
        RenderHelper.drawCube(poseStack, builder, sprite_top, sprite_front, sprite_right, sprite_back, sprite_left, sprite_bottom, combinedLight, 0xFFFFFFFF, alpha);

        poseStack.popPose();

    }
}

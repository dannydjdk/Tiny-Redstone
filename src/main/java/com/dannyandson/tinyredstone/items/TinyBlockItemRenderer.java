package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.panelcells.TinyBlock;
import com.dannyandson.tinyredstone.blocks.panelcells.TransparentBlock;
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
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TinyBlockItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final TextureAtlasSprite brokenSprite = RenderHelper.getSprite(TextureManager.INTENTIONAL_MISSING_TEXTURE);

    public TinyBlockItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

        boolean isTransparent = stack.getItem() == Registration.TINY_TRANSPARENT_BLOCK.get();
        TextureAtlasSprite sprite = null;

        if (stack.hasTag()) {
            CompoundTag itemNBT = stack.getTag();
            CompoundTag madeFromTag = itemNBT.getCompound("made_from");
            if (madeFromTag.contains("namespace")) {
                ResourceLocation itemId = new ResourceLocation(madeFromTag.getString("namespace"), madeFromTag.getString("path"));
                ResourceLocation texture = Registration.TINY_BLOCK_OVERRIDES.getTexture(itemId);
                sprite = RenderHelper.getSprite(texture);
            }
        }

        if (sprite == null)
            sprite = RenderHelper.getSprite(isTransparent ? TransparentBlock.TEXTURE_TRANSPARENT_BLOCK : TinyBlock.TEXTURE_TINY_BLOCK);

        VertexConsumer builder = buffer.getBuffer(isTransparent ? RenderType.translucent() : RenderType.solid());
        float alpha = isTransparent ? .99f : 1.0f;

        poseStack.pushPose();

        poseStack.mulPose(Vector3f.XP.rotationDegrees(-90));
        poseStack.translate(1, 0, 0);
        RenderHelper.drawCube(poseStack,builder,sprite,combinedLight,0xFFFFFFFF,alpha);

        poseStack.popPose();


    }
}

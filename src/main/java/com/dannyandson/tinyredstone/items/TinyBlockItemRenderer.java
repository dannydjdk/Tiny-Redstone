package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.dannyandson.tinyredstone.blocks.panelcells.TinyBlock;
import com.dannyandson.tinyredstone.blocks.panelcells.TransparentBlock;
import com.dannyandson.tinyredstone.setup.Registration;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TinyBlockItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final TextureAtlasSprite brokenSprite = RenderHelper.getSprite(TextureManager.INTENTIONAL_MISSING_TEXTURE);

    public TinyBlockItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

        boolean isTransparent = stack.getItem() == Registration.TINY_TRANSPARENT_BLOCK.get();
        ResourceLocation madeFrom = null;

        if (ItemStackHelper.getCustomTag(stack) != null) {
            CompoundTag itemNBT = ItemStackHelper.getCustomTag(stack);
            CompoundTag madeFromTag = itemNBT.getCompound("made_from");
            if (madeFromTag.contains("namespace")) {
                madeFrom = ResourceLocation.fromNamespaceAndPath(madeFromTag.getString("namespace"), madeFromTag.getString("path"));
            }
        }

        if (madeFrom != null) {
            // Use the block's actual BakedModel for rendering (flat-lit, no Level needed)
            BlockState blockState = BuiltInRegistries.BLOCK.get(madeFrom).defaultBlockState();
            if (blockState != null && !blockState.isAir()) {
                var blockRenderer = Minecraft.getInstance().getBlockRenderer();
                BakedModel model = blockRenderer.getBlockModel(blockState);
                VertexConsumer builder = buffer.getBuffer(isTransparent ? RenderType.translucent() : RenderType.solid());

                poseStack.pushPose();
                blockRenderer.getModelRenderer().renderModel(
                        poseStack.last(), builder, blockState, model,
                        1.0f, 1.0f, 1.0f, combinedLight, combinedOverlay
                );
                poseStack.popPose();
                return;
            }
        }

        // Fallback: default texture rendering for blocks without madeFrom
        TextureAtlasSprite sprite = RenderHelper.getSprite(isTransparent ? TransparentBlock.TEXTURE_TRANSPARENT_BLOCK : TinyBlock.TEXTURE_TINY_BLOCK);
        VertexConsumer builder = buffer.getBuffer(isTransparent ? RenderType.translucent() : RenderType.solid());
        float alpha = isTransparent ? .99f : 1.0f;

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.translate(1, 0, 0);
        RenderHelper.drawCube(poseStack, builder, sprite, sprite, sprite, sprite, sprite, sprite, combinedLight, 0xFFFFFFFF, alpha);
        poseStack.popPose();
    }
}
package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.dannyandson.tinyredstone.blocks.panelcovers.DarkCover;
import com.dannyandson.tinyredstone.blocks.panelcovers.LightCover;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
  * Renders panel cover items showing either the default cover texture
 * or the block they're disguised as (camouflage covers).
 */
public record PanelCoverItemRenderer() implements SpecialModelRenderer<ItemStack> {

    @Nullable
    @Override
    public ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    @Override
    public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        boolean isTransparent = stack.getItem() == ModRegistration.PANEL_COVER_LIGHT.get();

        // Per-face sprites, matching DarkCover.render() logic
        TextureAtlasSprite sprite_top, sprite_front, sprite_right, sprite_back, sprite_left, sprite_bottom;

        Identifier madeFrom = null;
        CompoundTag customTag = ItemStackHelper.getCustomTag(stack);
        if (customTag != null) {
            CompoundTag madeFromTag = customTag.getCompound("made_from").orElseGet(CompoundTag::new);
            if (madeFromTag.contains("namespace")) {
                madeFrom = Identifier.fromNamespaceAndPath(
                        madeFromTag.getStringOr("namespace", ""),
                        madeFromTag.getStringOr("path", ""));
            }
        }

        if (madeFrom != null) {
            sprite_top    = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.TOP);
            sprite_front  = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.FRONT);
            sprite_right  = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.RIGHT);
            sprite_back   = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.BACK);
            sprite_left   = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.LEFT);
            sprite_bottom = ModRegistration.TINY_BLOCK_OVERRIDES.getSprite(madeFrom, Side.BOTTOM);
        } else {
            TextureAtlasSprite defaultSprite = RenderHelper.getSprite(
                    isTransparent ? LightCover.TEXTURE_LIGHT_COVER : DarkCover.TEXTURE_DEFAULT_COVER);
            sprite_top = sprite_front = sprite_right = sprite_back = sprite_left = sprite_bottom = defaultSprite;
        }

        VertexConsumer builder = bufferSource.getBuffer(
                isTransparent ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet());
        float alpha = isTransparent ? 0.99f : 1.0f;
        int color = madeFrom != null ? 0x00FFFFFF : 0xFFFFFFFF;

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.translate(1, 0, 0);
        // Skip directional face shading for item rendering — the lightmap (UV2) provides
        // proper environmental lighting. Vanilla block items don't apply face shading either.
        // Full BlockModelResolver pipeline (Session 8) will handle this properly.
        RenderHelper.drawCube(poseStack, builder, sprite_top, sprite_front, sprite_right,
                sprite_back, sprite_left, sprite_bottom, lightCoords, color, alpha, false);
        poseStack.popPose();

        // 26.1: Must explicitly flush the immediate buffer source in SpecialModelRenderer.
        bufferSource.endBatch();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        // Provide bounding box corners so the engine knows this renderer's geometry extent.
        // Our drawCube renders a unit cube, so provide its 8 corners.
        consumer.accept(new Vector3f(0, 0, 0));
        consumer.accept(new Vector3f(1, 1, 1));
    }

    /**
     * Unbaked form for JSON deserialization and registration.
     */
    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public @Nullable SpecialModelRenderer<?> bake(BakingContext bakingContext) {
            return new PanelCoverItemRenderer();
        }
    }
}
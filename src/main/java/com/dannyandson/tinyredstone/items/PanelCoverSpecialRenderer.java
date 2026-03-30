package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.panelcovers.DarkCover;
import com.dannyandson.tinyredstone.blocks.panelcovers.LightCover;
import com.dannyandson.tinyredstone.setup.ModRegistration;
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
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * 26.1: SpecialModelRenderer replacement for PanelCoverItemRenderer (BEWLR).
 *
 * Renders panel cover items showing either the default cover texture
 * or the block they're disguised as (camouflage covers).
 */
public record PanelCoverSpecialRenderer() implements SpecialModelRenderer<ItemStack> {

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

        // TODO 26.1: BakedModel/BlockStateModel rendering for camouflage covers.
        // For now, fall through to sprite-based rendering.

        TextureAtlasSprite sprite = RenderHelper.getSprite(
                isTransparent ? LightCover.TEXTURE_LIGHT_COVER : DarkCover.TEXTURE_DEFAULT_COVER);
        VertexConsumer builder = bufferSource.getBuffer(
                isTransparent ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet());
        float alpha = isTransparent ? 0.99f : 1.0f;

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.translate(1, 0, 0);
        RenderHelper.drawCube(poseStack, builder, sprite, sprite, sprite, sprite, sprite, sprite,
                lightCoords, 0xFFFFFFFF, alpha);
        poseStack.popPose();

        // 26.1: Must explicitly flush the immediate buffer source in SpecialModelRenderer.
        // Unlike BER submit() where the level renderer manages buffer lifecycle, the item
        // rendering pipeline does not flush the immediate buffer source automatically.
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
            return new PanelCoverSpecialRenderer();
        }
    }
}
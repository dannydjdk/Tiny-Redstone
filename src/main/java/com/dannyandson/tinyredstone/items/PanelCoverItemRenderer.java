package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.RenderHelper;
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
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Renders panel cover items. Camouflage covers (with made_from) defer to
 * vanilla's item pipeline via ItemModelResolver so the source block's actual model handles texture
 * resolution. Falls back to a default cover-texture cube when made_from is absent.
 */
public record PanelCoverItemRenderer() implements SpecialModelRenderer<ItemStack> {

    // Shared per the migration guide pattern. Render is single-threaded.
    private static final ItemStackRenderState ITEM_STATE = new ItemStackRenderState();

    @Nullable
    @Override
    public ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    @Override
    public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        Identifier madeFrom = readMadeFrom(stack);
        if (madeFrom != null) {
            Block block = BuiltInRegistries.BLOCK.getValue(madeFrom);
            if (block != null) {
                ItemStack sourceStack = new ItemStack(block);
                if (!sourceStack.isEmpty()) {
                    submitSourceBlock(sourceStack, poseStack, collector, lightCoords, overlayCoords, outlineColor);
                    return;
                }
            }
        }

        renderDefault(stack, poseStack, lightCoords);
    }

    /** Reads the made_from Identifier from the stack's CUSTOM_DATA. Returns null if absent. */
    @Nullable
    private static Identifier readMadeFrom(ItemStack stack) {
        CompoundTag customTag = ItemStackHelper.getCustomTag(stack);
        if (customTag == null) return null;
        CompoundTag madeFromTag = customTag.getCompound("made_from").orElseGet(CompoundTag::new);
        if (!madeFromTag.contains("namespace")) return null;
        return Identifier.fromNamespaceAndPath(
                madeFromTag.getStringOr("namespace", ""),
                madeFromTag.getStringOr("path", ""));
    }

    /**
     * Submit the source block as a regular item. Outer SpecialModelRenderer
     * pipeline already applied the icon's display transform; ItemDisplayContext.NONE
     * keeps ItemStackRenderState.submit from layering a second one on top.
     */
    private static void submitSourceBlock(ItemStack sourceStack, PoseStack poseStack, SubmitNodeCollector collector,
                                          int lightCoords, int overlayCoords, int outlineColor) {
        var resolver = Minecraft.getInstance().getItemModelResolver();
        resolver.updateForTopItem(ITEM_STATE, sourceStack,
                ItemDisplayContext.NONE,
                Minecraft.getInstance().level, null, 0);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        ITEM_STATE.submit(poseStack, collector, lightCoords, overlayCoords, outlineColor);
        poseStack.popPose();
    }

    /** Default cover-texture cube shown when the stack has no made_from data. */
    private static void renderDefault(ItemStack stack, PoseStack poseStack, int lightCoords) {
        boolean isTransparent = stack.getItem() == ModRegistration.PANEL_COVER_LIGHT.get();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        TextureAtlasSprite sprite = RenderHelper.getSprite(
                isTransparent ? LightCover.TEXTURE_LIGHT_COVER : DarkCover.TEXTURE_DEFAULT_COVER);
        VertexConsumer builder = bufferSource.getBuffer(
                isTransparent ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet());
        float alpha = isTransparent ? 0.99f : 1.0f;

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.translate(1, 0, 0);
        RenderHelper.drawCube(poseStack, builder, sprite, sprite, sprite, sprite, sprite, sprite,
                lightCoords, 0xFFFFFFFF, alpha, false);
        poseStack.popPose();
        bufferSource.endBatch();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0, 0, 0));
        consumer.accept(new Vector3f(1, 1, 1));
    }

    /** Unbaked form for JSON deserialization and registration. */
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
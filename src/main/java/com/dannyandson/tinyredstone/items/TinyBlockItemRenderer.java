package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.CachedPanelRenderer;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.panelcells.TinyBlock;
import com.dannyandson.tinyredstone.blocks.panelcells.TransparentBlock;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Renders tiny block items as their source block. Defers to the vanilla item
 * pipeline via ItemModelResolver — that handles the source block's actual
 * model, display transforms, render type, and lighting.
 * Falls back to a default cube when the stack carries no made_from data.
 */
public record TinyBlockItemRenderer() implements SpecialModelRenderer<ItemStack> {

    // Shared per the migration guide's pattern. Render is single-threaded
    // (main thread), so reuse is safe.
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

        renderDefault(stack, poseStack, collector, lightCoords);
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
     * Submit the source block as a regular item via ItemModelResolver.
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

    /** Default-texture cube shown when the stack has no made_from data, matching prior behavior. */
    private static void renderDefault(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
        boolean isTransparent = stack.getItem() == ModRegistration.TINY_TRANSPARENT_BLOCK.get();
        TextureAtlasSprite sprite = RenderHelper.getSprite(
                isTransparent ? TransparentBlock.TEXTURE_TRANSPARENT_BLOCK : TinyBlock.TEXTURE_TINY_BLOCK);
        float alpha = isTransparent ? 0.99f : 1.0f;

        // Capture the cube into a panel-local buffer (construction transforms on a fresh
        // stack), then submit through the feature pipeline. The provided item poseStack supplies
        // the display transform once, at submit time.
        List<CachedPanelRenderer.CachedVertex> solid = new ArrayList<>();
        List<CachedPanelRenderer.CachedVertex> translucent = new ArrayList<>();
        CachedPanelRenderer.VertexCapture capture = new CachedPanelRenderer.VertexCapture(solid, translucent);
        VertexConsumer builder = isTransparent ? capture.translucent() : capture.solid();

        PoseStack captureStack = new PoseStack();
        captureStack.pushPose();
        captureStack.mulPose(Axis.XP.rotationDegrees(-90));
        captureStack.translate(1, 0, 0);
        RenderHelper.drawCube(captureStack, builder, sprite, sprite, sprite, sprite, sprite, sprite,
                lightCoords, 0xFFFFFFFF, alpha, false);
        captureStack.popPose();
        capture.flush();

        PanelTileRenderer.submitCachedVertices(poseStack, collector, solid, translucent);
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
            return new TinyBlockItemRenderer();
        }
    }
}
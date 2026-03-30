package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCover;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * 26.1: SpecialModelRenderer replacement for PanelItemRenderer (BEWLR).
 *
 * Renders redstone panel items showing their cell layout when they contain
 * stored circuit data (from blueprints or broken panels with contents).
 */
public record PanelSpecialRenderer() implements SpecialModelRenderer<ItemStack> {

    @Nullable
    @Override
    public ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    @Override
    public void submit(ItemStack stack, PoseStack matrixStack, SubmitNodeCollector collector,
                       int combinedLight, int combinedOverlay, boolean hasFoil, int outlineColor) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        TextureAtlasSprite sprite = RenderHelper.getSprite(PanelTileRenderer.TEXTURE);
        VertexConsumer builder = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        int color = DyeColor.GRAY.getMapColor().col;
        CompoundTag blockEntityTag = ItemStackHelper.getBlockEntityTag(stack);
        if (blockEntityTag != null) {
            if (blockEntityTag.contains("color")) {
                color = blockEntityTag.getIntOr("color", 0);
            }
        }

        matrixStack.pushPose();
        matrixStack.translate(0, 0.125, 0);

        if (blockEntityTag != null) {

            CompoundTag itemTag = blockEntityTag;

            if (itemTag.contains("cover")) {
                String coverClass = blockEntityTag.getStringOr("cover", "");
                try {
                    IPanelCover cover = (IPanelCover) Class.forName(coverClass).getConstructor().newInstance();
                    cover.readNBT(blockEntityTag.getCompound("coverData").orElseGet(CompoundTag::new));
                    matrixStack.pushPose();
                    cover.render(matrixStack, bufferSource, combinedLight, combinedOverlay, color);
                    matrixStack.popPose();
                } catch (Exception exception) {
                    TinyRedstone.LOGGER.error("Exception attempting to construct IPanelCover class for item render: " + coverClass +
                            ": " + exception.getMessage() + " " + exception.getStackTrace()[0].toString());
                }
            } else {
                boolean hasBase = !itemTag.contains("hasBase") || itemTag.getBooleanOr("hasBase", false);

                if (hasBase)
                    renderBase(matrixStack, builder, sprite, combinedLight, color);

                CompoundTag cellsNBT = itemTag.getCompound("cells").orElseGet(CompoundTag::new);
                for (Integer i = 0; i < (hasBase ? 448 : 512); i++) {
                    if (cellsNBT.contains(i.toString())) {
                        CompoundTag cellNBT = cellsNBT.getCompound(i.toString()).orElseGet(CompoundTag::new);

                        if (cellNBT.contains("data")) {
                            String className = cellNBT.getStringOr("class", "");
                            try {
                                IPanelCell cell = (IPanelCell) Class.forName(className).getConstructor().newInstance();
                                cell.readNBT(cellNBT.getCompound("data").orElseGet(CompoundTag::new));
                                Side cellDirection = Side.valueOf(cellNBT.getStringOr("facing", ""));
                                renderCell(matrixStack, i, cell, cellDirection, bufferSource, combinedLight, combinedOverlay);
                            } catch (Exception exception) {
                                TinyRedstone.LOGGER.error("Exception attempting to construct IPanelCell class for item render: " + className +
                                        ": " + exception.getMessage() + " " + exception.getStackTrace()[0].toString());
                            }
                        }
                    }
                }
            }
        } else {
            renderBase(matrixStack, builder, sprite, combinedLight, color);
        }

        matrixStack.popPose();

        // 26.1: Must explicitly flush in SpecialModelRenderer — item rendering pipeline
        // does not flush the immediate buffer source automatically.
        bufferSource.endBatch();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        // Panel geometry: base is 0-1 x, 0-0.25 y, 0-1 z; cells can extend up to 1.0 y.
        // Use a full unit cube to be safe.
        consumer.accept(new Vector3f(0, 0, 0));
        consumer.accept(new Vector3f(1, 1, 1));
    }

    private void renderBase(PoseStack matrixStack, VertexConsumer builder, TextureAtlasSprite sprite, int combinedLight, int color) {
        matrixStack.pushPose();
        matrixStack.mulPose(Axis.XP.rotationDegrees(270));
        matrixStack.translate(0, -1, 0.125);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, 1, sprite, combinedLight, color, 1.0f);

        matrixStack.mulPose(Axis.XP.rotationDegrees(90));
        matrixStack.translate(0, -0.125, 0);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        matrixStack.translate(0, 0, 1);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        matrixStack.translate(0, 0, 1);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        matrixStack.translate(0, 0, 1);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

        matrixStack.mulPose(Axis.XP.rotationDegrees(90));
        matrixStack.translate(0, -1, 0);
        RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, 1, sprite, combinedLight, color, 1.0f);

        matrixStack.popPose();
    }

    private void renderCell(PoseStack matrixStack, Integer index, IPanelCell panelCell, Side cellDirection,
                            MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        float scale = 0.125f;
        float t2X = 0.0f;
        float t2Y = -1.0f;
        float t2Z = 0.0f;
        float rotation1 = 270f;
        double cellSize = 1d / 8d;

        int level = Math.round((index.floatValue() / 64f) - 0.5f);
        int row = Math.round(((index.floatValue() % 64) / 8f) - 0.5f);
        int cell = index % 8;

        matrixStack.pushPose();

        matrixStack.translate(cellSize * (double) row, 0.125 + (cellSize * (double) level), cellSize * (cell));
        matrixStack.mulPose(Axis.XP.rotationDegrees(rotation1));

        if (cellDirection == Side.LEFT) {
            matrixStack.translate(0, -cellSize, 0);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(90));
        } else if (cellDirection == Side.BACK) {
            matrixStack.translate(cellSize, -cellSize, 0);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
        } else if (cellDirection == Side.RIGHT) {
            matrixStack.translate(cellSize, 0, 0);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(270));
        }

        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(t2X, t2Y, t2Z);

        panelCell.render(matrixStack, buffer, combinedLight, combinedOverlay, 1);

        matrixStack.popPose();
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
            return new PanelSpecialRenderer();
        }
    }
}
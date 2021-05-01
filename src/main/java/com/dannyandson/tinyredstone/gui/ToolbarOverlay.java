package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.blocks.Side;
import com.dannyandson.tinyredstone.items.PanelCellItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT)
public class ToolbarOverlay {
    @SubscribeEvent
    public static void onRenderGUI(final RenderGameOverlayEvent.Post event) {
        if(event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            final Minecraft mcInstance = Minecraft.getInstance();
            final ClientPlayerEntity player = mcInstance.player;

            if (!player.isSpectator()) {
                final int currentSlot = player.inventory.currentItem;
                final ItemStack stack = player.inventory.mainInventory.get(currentSlot);
                if (stack.getItem() instanceof PanelCellItem) {
                    final MainWindow window = event.getWindow();
                    final Side rotationLock = RotationLock.getRotationLock();

                    // top-left corner of selected slot + 1/2 border + 2 padding
                    final int x = (window.getScaledWidth() / 2 - 180/2 + currentSlot * 20) + 2 + 2;
                    final int y = (window.getScaledHeight() - 20) + 1 + 2;

                    if(rotationLock != null) {
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferBuilder = tessellator.getBuffer();
                        MatrixStack matrixStack = event.getMatrixStack();
                        RenderSystem.enableBlend();
                        RenderSystem.disableTexture();
                        RenderSystem.defaultBlendFunc();
                        bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

                        final int color = 0xa1b29d;

                        final float arrow_height = 2f;
                        final float arrow_width = 3f;
                        final float shaft_height = 2f;
                        final float shaft_width = 1.5f;

                        final float plate_height = 1f;

                        final float shaft_on_plate_height = shaft_height - plate_height;
                        final float shaft_offset = (arrow_width - shaft_width)/2;
                        final float arrow_center = arrow_width/2;

                        switch (rotationLock) {
                            case BACK:
                                RenderHelper.drawTriangle(bufferBuilder, matrixStack, x, y+shaft_height, x+arrow_center, y+arrow_height+shaft_height, x+arrow_width, y+shaft_height, color, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x+shaft_offset, x+shaft_offset+shaft_width, y, y+shaft_height, color, 1.0f);
                                break;
                            case LEFT:
                                RenderHelper.drawTriangle(bufferBuilder, matrixStack, x, y+arrow_center, x+arrow_height, y+arrow_width, x+arrow_height, y, color, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x+arrow_height, x+arrow_height+shaft_height, y+shaft_offset, y+shaft_width+shaft_offset, color, 1.0f);
                                break;
                            case FRONT:
                                RenderHelper.drawTriangle(bufferBuilder, matrixStack, x+arrow_center, y, x, y+arrow_height, x+arrow_width, y+arrow_height, color, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x+shaft_offset, x+shaft_offset+shaft_width, y+arrow_height, y+arrow_height+shaft_height, color, 1.0f);
                                break;
                            case RIGHT:
                                RenderHelper.drawTriangle(bufferBuilder, matrixStack, x+shaft_height, y, x+shaft_height, y+arrow_width, x+arrow_height+shaft_height, y+arrow_center, color, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x, x+shaft_height, y+shaft_offset, y+shaft_offset+shaft_width, color, 1.0f);
                                break;
                            case TOP:
                                RenderHelper.drawTriangle(bufferBuilder, matrixStack, x, y+shaft_height, x+arrow_center, y+arrow_height+shaft_height, x+arrow_width, y+shaft_height, color, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x+shaft_offset, x+shaft_offset+shaft_width, y+plate_height, y+shaft_height, color, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x, x+arrow_width, y, y+plate_height, color, 1.0f);
                                break;
                            case BOTTOM:
                                RenderHelper.drawTriangle(bufferBuilder, matrixStack, x+arrow_center, y, x, y+arrow_height, x+arrow_width, y+arrow_height, color, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x+shaft_offset, x+shaft_offset+shaft_width, y+arrow_height, y+arrow_height+shaft_on_plate_height, color, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x, x+arrow_width, y+arrow_height+shaft_on_plate_height, y+arrow_height+shaft_height, color, 1.0f);
                                break;
                        }

                        bufferBuilder.finishDrawing();
                        WorldVertexBufferUploader.draw(bufferBuilder);
                        RenderSystem.enableTexture();
                        RenderSystem.disableBlend();
                    }
                }
            }
        }
    }
}

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

                        switch (rotationLock) {
                            case BACK:
                                RenderHelper.drawTriangle(bufferBuilder, matrixStack, x, y+1, x+1.5f, y+3+1, x+3, y+1, 0xaaaaaa, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x+0.75f, x+3-0.75f, y, y+1, 0xaaaaaa, 1.0f);
                                break;
                            case LEFT:
                                RenderHelper.drawTriangle(bufferBuilder, matrixStack, x, y+1.5f, x+3, y+3, x+3, y, 0xaaaaaa, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x+3, x+3+1, y+0.75f, y+3-0.75f, 0xaaaaaa, 1.0f);
                                break;
                            case FRONT:
                                RenderHelper.drawTriangle(bufferBuilder, matrixStack, x+1.5f, y, x, y+3, x+3, y+3, 0xaaaaaa, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x+0.75f, x+3-0.75f, y+3, y+3+1, 0xaaaaaa, 1.0f);
                                break;
                            case RIGHT:
                                RenderHelper.drawTriangle(bufferBuilder, matrixStack, x+1, y, x+1, y+3, x+3+1, y+1.5f, 0xaaaaaa, 1.0f);
                                RenderHelper.drawTriangleRectangle(bufferBuilder, matrixStack, x, x+1, y+0.75f, y+3-0.75f, 0xaaaaaa, 1.0f);
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

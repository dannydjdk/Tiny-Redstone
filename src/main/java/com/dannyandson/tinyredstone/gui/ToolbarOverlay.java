package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.blocks.Side;
import com.dannyandson.tinyredstone.items.PanelCellItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(Dist.CLIENT)
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
                        Matrix4f matrix4f = event.getMatrixStack().getLast().getMatrix();
                        RenderSystem.enableBlend();
                        RenderSystem.disableTexture();
                        RenderSystem.defaultBlendFunc();
                        bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

                        switch (rotationLock) {
                            case BACK:
                                bufferBuilder.pos(matrix4f, x, y, 0.0F).color(181, 181, 181, 255).endVertex();
                                bufferBuilder.pos(matrix4f, x+1.5f, y+3, 0.0F).color(181, 181, 181, 255).endVertex();
                                bufferBuilder.pos(matrix4f, x+3, y, 0.0F).color(181, 181, 181, 255).endVertex();
                                break;
                            case LEFT:
                                bufferBuilder.pos(matrix4f, x, y+1.5f, 0.0F).color(181, 181, 181, 255).endVertex();
                                bufferBuilder.pos(matrix4f, x+3, y+3, 0.0F).color(181, 181, 181, 255).endVertex();
                                bufferBuilder.pos(matrix4f, x+3, y, 0.0F).color(181, 181, 181, 255).endVertex();
                                break;
                            case FRONT:
                                bufferBuilder.pos(matrix4f, x+1.5f, y, 0.0F).color(181, 181, 181, 255).endVertex();
                                bufferBuilder.pos(matrix4f, x, y+3, 0.0F).color(181, 181, 181, 255).endVertex();
                                bufferBuilder.pos(matrix4f, x+3, y+3, 0.0F).color(181, 181, 181, 255).endVertex();
                                break;
                            case RIGHT:
                                bufferBuilder.pos(matrix4f, x, y, 0.0F).color(181, 181, 181, 255).endVertex();
                                bufferBuilder.pos(matrix4f, x, y+3, 0.0F).color(181, 181, 181, 255).endVertex();
                                bufferBuilder.pos(matrix4f, x+3, y+1.5f, 0.0F).color(181, 181, 181, 255).endVertex();
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

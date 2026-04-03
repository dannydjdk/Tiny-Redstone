package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.AbstractPanelCellItem;
import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.blocks.Side;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT)
public class ToolbarOverlay {

    private static final Identifier ROTATION_LOCK_TEXTURE = Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "textures/block/rotation_lock.png");

    @SubscribeEvent
    public static void onRenderGUI(final RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            final Minecraft mcInstance = Minecraft.getInstance();
            final LocalPlayer player = mcInstance.player;

            if (player != null && !player.isSpectator()) {
                final int currentSlot = player.getInventory().getSelectedSlot();
                final ItemStack stack = player.getInventory().getItem((currentSlot));
                if (stack.getItem() instanceof AbstractPanelCellItem) {
                    final Window window = Minecraft.getInstance().getWindow();
                    final Side rotationLock = RotationLock.getRotationLock();

                    if (rotationLock != null) {
                        // top-left corner of selected slot + 1/2 border + 1 padding
                        final int x = (window.getGuiScaledWidth() / 2 - 180 / 2 + currentSlot * 20) + 2 + 1;
                        final int y = (window.getGuiScaledHeight() - 20) + 1 + 1;

                        // 26.1: Use direct texture reference with GUI_TEXTURED pipeline.
                        // Setting textureWidth/Height equal to render size maps the full
                        // 16x16 texture onto the 5x5 screen area.
                        event.getGuiGraphics().blit(
                                RenderPipelines.GUI_TEXTURED,
                                ROTATION_LOCK_TEXTURE,
                                x, y, 0, 0, 5, 5, 5, 5
                        );
                    }
                }
            }
        }
    }
}
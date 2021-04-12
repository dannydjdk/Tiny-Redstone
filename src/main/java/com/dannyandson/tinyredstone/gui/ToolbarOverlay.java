package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.blocks.Side;
import com.dannyandson.tinyredstone.items.PanelCellItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ToolbarOverlay {
    @SubscribeEvent
    public void onRenderGUI(final RenderGameOverlayEvent.Post event) {
        if(event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            final Minecraft mcInstance = Minecraft.getInstance();
            final ClientPlayerEntity player = mcInstance.player;

            if (!player.isSpectator()) {
                final int currentSlot = player.inventory.currentItem;
                final ItemStack stack = player.inventory.mainInventory.get(currentSlot);
                if (stack.getItem() instanceof PanelCellItem) {
                    final MainWindow window = event.getWindow();
                    final Side rotationLock = RotationLock.getRotationLock();

                    if(rotationLock != null) {
                        RenderSystem.color3f(1, 1, 1);
                        RenderSystem.enableBlend();
                        // TODO:
                        RenderSystem.disableBlend();
                    }
                }
            }
        }
    }
}

package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.neoforged.fml.common.EventBusSubscriber;
import com.dannyandson.tinyredstone.api.AbstractPanelCellItem;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.blocks.Side;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
// Fix for 1.21.1: RenderGuiOverlayEvent -> RenderGuiLayerEvent, VanillaGuiOverlay -> VanillaGuiLayers
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT)
public class ToolbarOverlay {

    public static ResourceLocation TEXTURE_ROTATION_LOCK = ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID, "block/rotation_lock");

    @SubscribeEvent
    // Fix for 1.21.1: RenderGuiOverlayEvent.Post -> RenderGuiLayerEvent.Post
    public static void onRenderGUI(final RenderGuiLayerEvent.Post event) {
        // Fix for 1.21.1: VanillaGuiOverlay.HOTBAR.type() -> VanillaGuiLayers.HOTBAR
        if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            final Minecraft mcInstance = Minecraft.getInstance();
            final LocalPlayer player = mcInstance.player;

            if (player != null && !player.isSpectator()) {
                final int currentSlot = player.getInventory().selected;
                final ItemStack stack = player.getInventory().items.get(currentSlot);
                if (stack.getItem() instanceof AbstractPanelCellItem) {
                    final Window window = Minecraft.getInstance().getWindow();
                    final Side rotationLock = RotationLock.getRotationLock();

                    if (rotationLock != null) {
                        // top-left corner of selected slot + 1/2 border + 1 padding
                        final int x = (window.getGuiScaledWidth() / 2 - 180 / 2 + currentSlot * 20) + 2 + 1;
                        final int y = (window.getGuiScaledHeight() - 20) + 1 + 1;

                        Minecraft.getInstance().getTextureManager().bindForSetup(InventoryMenu.BLOCK_ATLAS);
                        TextureAtlasSprite sprite = RenderHelper.getSprite(TEXTURE_ROTATION_LOCK);

                        RenderSystem.enableBlend();
                        event.getGuiGraphics().blit(x, y, 0, 5, 5, sprite);
                        RenderSystem.disableBlend();
                    }
                }
            }
        }
    }
}

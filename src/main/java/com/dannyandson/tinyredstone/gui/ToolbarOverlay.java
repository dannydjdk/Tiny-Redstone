package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.neoforged.fml.common.EventBusSubscriber;
import com.dannyandson.tinyredstone.api.AbstractPanelCellItem;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.blocks.Side;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT)
public class ToolbarOverlay {

    public static Identifier TEXTURE_ROTATION_LOCK = Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "block/rotation_lock");

    @SubscribeEvent
    // Fix for 1.21.1: RenderGuiOverlayEvent.Post -> RenderGuiLayerEvent.Post
    public static void onRenderGUI(final RenderGuiLayerEvent.Post event) {
        // Fix for 1.21.1: VanillaGuiOverlay.HOTBAR.type() -> VanillaGuiLayers.HOTBAR
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

                        // bindForSetup removed in 26.1 - textures bound automatically
                        TextureAtlasSprite sprite = RenderHelper.getSprite(TEXTURE_ROTATION_LOCK);

                        // 26.1: Sprite-based blit overload removed.
                        // Use atlas texture with sprite UV coordinates instead.
                        event.getGuiGraphics().blit(
                                sprite.atlasLocation(),
                                x, y, 5, 5,
                                sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1()
                        );

                    }
                }
            }
        }
    }
}
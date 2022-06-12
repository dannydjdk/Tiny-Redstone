package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.AbstractPanelCellItem;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.blocks.Side;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT)
public class ToolbarOverlay {

    public static ResourceLocation TEXTURE_ROTATION_LOCK = new ResourceLocation(TinyRedstone.MODID,"block/rotation_lock");

    @SubscribeEvent
    public static void onRenderGUI(final RenderGameOverlayEvent.Post event) {
        if(event.getType() == RenderGameOverlayEvent.ElementType.LAYER) {
            final Minecraft mcInstance = Minecraft.getInstance();
            final LocalPlayer player = mcInstance.player;

            if (player!=null && !player.isSpectator()) {
                final int currentSlot = player.getInventory().selected;
                final ItemStack stack = player.getInventory().items.get(currentSlot);
                if (stack.getItem() instanceof AbstractPanelCellItem) {
                    final Window window = event.getWindow();
                    final Side rotationLock = RotationLock.getRotationLock();


                    if(rotationLock != null) {
                        // top-left corner of selected slot + 1/2 border + 1 padding
                        final int x = (window.getGuiScaledWidth() / 2 - 180/2 + currentSlot * 20) + 2 + 1;
                        final int y = (window.getGuiScaledHeight() - 20) + 1 + 1;

                        PoseStack matrixStack = event.getPoseStack();

                        Minecraft.getInstance().getTextureManager().bindForSetup(InventoryMenu.BLOCK_ATLAS);
                        TextureAtlasSprite sprite = RenderHelper.getSprite(TEXTURE_ROTATION_LOCK);

                        RenderSystem.enableBlend();
                        mcInstance.gui.blit(matrixStack,x,y,0,5,5,sprite);
                        RenderSystem.disableBlend();
                    }
                }
            }
        }
    }
}

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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT)
public class ToolbarOverlay {

    public static ResourceLocation TEXTURE_ROTATION_LOCK = new ResourceLocation(TinyRedstone.MODID,"block/rotation_lock");

    @SubscribeEvent
    public static void onRenderGUI(final RenderGameOverlayEvent.Post event) {
        if(event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            final Minecraft mcInstance = Minecraft.getInstance();
            final ClientPlayerEntity player = mcInstance.player;

            if (!player.isSpectator()) {
                final int currentSlot = player.inventory.selected;
                final ItemStack stack = player.inventory.items.get(currentSlot);
                if (stack.getItem() instanceof PanelCellItem) {
                    final MainWindow window = event.getWindow();
                    final Side rotationLock = RotationLock.getRotationLock();


                    if(rotationLock != null) {
                        // top-left corner of selected slot + 1/2 border + 1 padding
                        final int x = (window.getGuiScaledWidth() / 2 - 180/2 + currentSlot * 20) + 2 + 1;
                        final int y = (window.getGuiScaledHeight() - 20) + 1 + 1;

                        MatrixStack matrixStack = event.getMatrixStack();

                        Minecraft.getInstance().getTextureManager().bind(PlayerContainer.BLOCK_ATLAS);
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

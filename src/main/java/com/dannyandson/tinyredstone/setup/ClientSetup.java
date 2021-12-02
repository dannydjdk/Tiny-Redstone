package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.ClientBinding;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTileColor;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.blocks.panelcells.*;
import com.dannyandson.tinyredstone.blocks.panelcovers.DarkCover;
import com.dannyandson.tinyredstone.blocks.panelcovers.LightCover;
import com.dannyandson.tinyredstone.gui.ChopperScreen;
import com.dannyandson.tinyredstone.gui.ToolbarOverlay;
import com.dannyandson.tinyredstone.items.PanelItemColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static final ResourceLocation TRANSPARENT_TEXTURE_LOC = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    public static void init(final FMLClientSetupEvent event)
    {
        Minecraft.getInstance().getBlockColors().register(new PanelTileColor(), Registration.REDSTONE_PANEL_BLOCK.get());
        Minecraft.getInstance().getItemColors().register(new PanelItemColor(),Registration.REDSTONE_PANEL_ITEM.get());
        ClientBinding.registerKeyBindings();
        MenuScreens.register(Registration.CUTTER_MENU_TYPE.get(), ChopperScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event){
        TinyRedstone.LOGGER.debug("Registering Renderer for Redstone Panel block entity.", Registration.REDSTONE_PANEL_BLOCK.get());
        event.registerBlockEntityRenderer(Registration.REDSTONE_PANEL_TILE.get(), PanelTileRenderer::new);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            return;
        }

        event.addSprite(PanelTileRenderer.TEXTURE);
        event.addSprite(PanelTileRenderer.TEXTURE_CRASHED);

        event.addSprite(Repeater.TEXTURE_REPEATER_ON);
        event.addSprite(Repeater.TEXTURE_REPEATER_OFF);
        event.addSprite(RedstoneDust.TEXTURE_REDSTONE_DUST_SEGMENT);
        event.addSprite(Torch.TEXTURE_TORCH_ON);
        event.addSprite(Torch.TEXTURE_TORCH_OFF);
        event.addSprite(Torch.TEXTURE_TORCH_TOP_ON);
        event.addSprite(Torch.TEXTURE_TORCH_TOP_OFF);
        event.addSprite(SuperRepeater.TEXTURE_SUPER_REPEATER_ON);
        event.addSprite(SuperRepeater.TEXTURE_SUPER_REPEATER_OFF);
        event.addSprite(Comparator.TEXTURE_COMPARATOR_OFF);
        event.addSprite(Comparator.TEXTURE_COMPARATOR_ON);
        event.addSprite(Comparator.TEXTURE_COMPARATOR_SUBTRACT_OFF);
        event.addSprite(Comparator.TEXTURE_COMPARATOR_SUBTRACT_ON);
        event.addSprite(GhostRenderer.TEXTURE_REDSTONE_DUST_SEGMENT_GHOST);

        event.addSprite(LightCover.TEXTURE_LIGHT_COVER);
        event.addSprite(DarkCover.TEXTURE_DARK_COVER);

        event.addSprite(ToolbarOverlay.TEXTURE_ROTATION_LOCK);
        event.addSprite(TinyBlock.TEXTURE_GRASS_BLOCK_TOP);
    }

}
package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.ClientBinding;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTileColor;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.gui.ChopperScreen;
import com.dannyandson.tinyredstone.items.PanelItemColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
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
        MenuScreens.register(Registration.CUTTER_MENU_TYPE.get(), ChopperScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event){
        TinyRedstone.LOGGER.debug("Registering Renderer for Redstone Panel block entity.", Registration.REDSTONE_PANEL_BLOCK.get());
        event.registerBlockEntityRenderer(Registration.REDSTONE_PANEL_TILE.get(), PanelTileRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterKeybindings(RegisterKeyMappingsEvent event){
        ClientBinding.registerKeyBindings(event);
    }
}
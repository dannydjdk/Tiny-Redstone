package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.ClientBinding;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTileColor;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.gui.ChopperScreen;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT)
public class ClientSetup {

    public static final Identifier TRANSPARENT_TEXTURE_LOC = Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "textures/gui/transparent.png");

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModRegistration.CUTTER_MENU_TYPE.get(), ChopperScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event){
        TinyRedstone.LOGGER.debug("Registering Renderer for Redstone Panel block entity.", ModRegistration.REDSTONE_PANEL_BLOCK.get());
        event.registerBlockEntityRenderer(ModRegistration.REDSTONE_PANEL_TILE.get(), PanelTileRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterKeybindings(RegisterKeyMappingsEvent event){
        ClientBinding.registerKeyBindings(event);
    }

    /**
     * 26.1: Block tint registration via RegisterColorHandlersEvent.BlockTintSources.
     * register() takes List<BlockTintSource> and varargs of blocks.
     */
    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(java.util.List.of(new PanelTileColor()), ModRegistration.REDSTONE_PANEL_BLOCK.get());
    }

    // TODO 26.1: Item tint registration — now data-driven via ItemTintSource in item model JSONs.
    // TODO 26.1: Custom item renderers (BEWLR → SpecialModelRenderer) — excluded from build.
}
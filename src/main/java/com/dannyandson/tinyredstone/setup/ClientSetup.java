package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.ClientBinding;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTileColor;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.gui.ChopperScreen;
import com.dannyandson.tinyredstone.items.PanelCoverItemRenderer;
import com.dannyandson.tinyredstone.items.PanelItemColor;
import com.dannyandson.tinyredstone.items.PanelItemRenderer;
import com.dannyandson.tinyredstone.items.TinyBlockItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT)
public class ClientSetup {

    public static final Identifier TRANSPARENT_TEXTURE_LOC = Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "textures/gui/transparent.png");

    public static void init(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            Minecraft.getInstance().getBlockColors().register(new PanelTileColor(), ModRegistration.REDSTONE_PANEL_BLOCK.get());
            Minecraft.getInstance().getItemColors().register(new PanelItemColor(), ModRegistration.REDSTONE_PANEL_ITEM.get());
        });
    }

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

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // Custom item renderers: migrated from initializeClient(Consumer<IClientItemExtensions>)
        // to RegisterClientExtensionsEvent (NeoForge 1.21.2+)
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new PanelItemRenderer(
                            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            Minecraft.getInstance().getEntityModels());
                }
                return renderer;
            }
        }, ModRegistration.REDSTONE_PANEL_ITEM.get());

        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new TinyBlockItemRenderer(
                            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            Minecraft.getInstance().getEntityModels());
                }
                return renderer;
            }
        }, ModRegistration.TINY_SOLID_BLOCK.get(), ModRegistration.TINY_TRANSPARENT_BLOCK.get());

        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new PanelCoverItemRenderer(
                            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            Minecraft.getInstance().getEntityModels());
                }
                return renderer;
            }
        }, ModRegistration.PANEL_COVER_DARK.get(), ModRegistration.PANEL_COVER_LIGHT.get(), ModRegistration.PANEL_COVER_TRIM.get());
    }
}

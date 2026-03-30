package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.ClientBinding;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTileColor;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.gui.ChopperScreen;
import com.dannyandson.tinyredstone.items.PanelCoverSpecialRenderer;
import com.dannyandson.tinyredstone.items.PanelItemTintSource;
import com.dannyandson.tinyredstone.items.PanelSpecialRenderer;
import com.dannyandson.tinyredstone.items.TinyBlockSpecialRenderer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

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
     */
    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(java.util.List.of(new PanelTileColor()), ModRegistration.REDSTONE_PANEL_BLOCK.get());
    }

    /**
     * 26.1: Item tint source registration.
     * ItemTintSource replaces the old ItemColor system. Tint sources are data-driven
     * and referenced by type name in item definition JSONs.
     */
    @SubscribeEvent
    public static void onRegisterItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(
                Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "panel_color"),
                PanelItemTintSource.MAP_CODEC
        );
    }

    /**
     * 26.1: SpecialModelRenderer registration.
     * Replaces the old BEWLR (BlockEntityWithoutLevelRenderer) + IClientItemExtensions system.
     * Each renderer is referenced by type name in item definition JSONs via "minecraft:special".
     */
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onRegisterSpecialRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "panel"),
                (MapCodec<? extends SpecialModelRenderer.Unbaked<?>>) (MapCodec<?>) PanelSpecialRenderer.Unbaked.MAP_CODEC
        );
        event.register(
                Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "tiny_block"),
                (MapCodec<? extends SpecialModelRenderer.Unbaked<?>>) (MapCodec<?>) TinyBlockSpecialRenderer.Unbaked.MAP_CODEC
        );
        event.register(
                Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "panel_cover"),
                (MapCodec<? extends SpecialModelRenderer.Unbaked<?>>) (MapCodec<?>) PanelCoverSpecialRenderer.Unbaked.MAP_CODEC
        );
    }
}
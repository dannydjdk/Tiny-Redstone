package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

@EventBusSubscriber(modid = TinyRedstone.MODID)
public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {
        ModRegistration.registerPanelCells();
    }

    // Registers the data-pack listener that loads tiny_block_overrides JSON.
    @SubscribeEvent
    public static void onAddReloadListeners(final AddServerReloadListenersEvent event) {
        event.addListener(
                Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "tiny_block_overrides"),
                ModRegistration.TINY_BLOCK_OVERRIDES);
    }

}
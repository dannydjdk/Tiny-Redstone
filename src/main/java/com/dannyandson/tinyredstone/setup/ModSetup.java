package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid = TinyRedstone.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {
        Registration.registerPanelCells();
        // Network registration is now event-driven via RegisterPayloadHandlersEvent in ModNetworkHandler
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(Registration.TINY_BLOCK_OVERRIDES);
    }

}

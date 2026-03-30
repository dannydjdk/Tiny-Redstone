package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
// TODO: AddReloadListenerEvent was removed/renamed in 26.1.
// Find the replacement event to re-register TINY_BLOCK_OVERRIDES.

@EventBusSubscriber(modid = TinyRedstone.MODID)
public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {
        ModRegistration.registerPanelCells();
    }

    // TODO: Re-enable reload listener once the correct 26.1 event is identified
    // @SubscribeEvent
    // public static void onAddReloadListeners(??? event) {
    //     event.addListener(Registration.TINY_BLOCK_OVERRIDES);
    // }

}

package com.dannyandson.tinyredstone;

import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCover;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.compat.CompatHandler;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import com.dannyandson.tinyredstone.setup.ModSetup;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TinyRedstone.MODID)
public class TinyRedstone {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "tinyredstone";

    public TinyRedstone(IEventBus modEventBus, ModContainer modContainer) {
        ModRegistration.register(modEventBus);

        modEventBus.addListener(ModSetup::init);
        // 26.1: ClientSetup.init() removed — block/item color registration now via
        // @SubscribeEvent on RegisterColorHandlersEvent.Block in ClientSetup.

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);

        CompatHandler.register();
    }

    public static void registerPanelCell(Class<? extends IPanelCell> iPanelCellClass, Item correspondingItem) {
        PanelBlock.registerPanelCell(iPanelCellClass, correspondingItem);
    }

    public static void registerPanelCover(Class<? extends IPanelCover> iPanelCoverClass, Item correspondingItem) {
        PanelBlock.registerPanelCover(iPanelCoverClass, correspondingItem);
    }
}
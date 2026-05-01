package com.dannyandson.tinyredstone;

import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCover;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.compat.CompatHandler;
import com.dannyandson.tinyredstone.setup.ClientSetup;
import com.dannyandson.tinyredstone.setup.ModSetup;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TinyRedstone.MODID)
public class TinyRedstone {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "tinyredstone";

    public TinyRedstone(IEventBus modEventBus, ModContainer modContainer) {
        Registration.register(modEventBus);

        modEventBus.addListener(ModSetup::init);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(ClientSetup::init);
        }

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        CompatHandler.register();
    }

    public static void registerPanelCell(Class<? extends IPanelCell> iPanelCellClass, Item correspondingItem) {
        PanelBlock.registerPanelCell(iPanelCellClass, correspondingItem);
    }

    public static void registerPanelCover(Class<? extends IPanelCover> iPanelCoverClass, Item correspondingItem) {
        PanelBlock.registerPanelCover(iPanelCoverClass, correspondingItem);
    }
}
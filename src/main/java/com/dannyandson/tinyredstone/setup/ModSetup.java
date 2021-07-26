package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = TinyRedstone.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab("tinyredstone") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.REDSTONE_PANEL_BLOCK.get());
        }
    };

    public static void init(final FMLCommonSetupEvent event) {
        Registration.registerPanelCells();
        ModNetworkHandler.registerMessages();
    }

}

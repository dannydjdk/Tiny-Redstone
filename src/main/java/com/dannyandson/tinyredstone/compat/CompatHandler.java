package com.dannyandson.tinyredstone.compat;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;

public class CompatHandler {
    static final Identifier MEASURING_DEVICE = Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "measuring_device");
    static final Identifier TINY_COMPONENT = Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "tiny_component");

    public static void register()  {
        // TheOneProbe compat excluded from build until 26.1 version available
        // if(ModList.get().isLoaded("theoneprobe")) {
        //     InterModComms.sendTo("theoneprobe", "getTheOneProbe", PanelProvider::new);
        // }
    }

    public static boolean isMeasuringDevice(Item item) {
        for (TagKey<Item> tagKey: item.getDefaultInstance().tags().toList()) {
            if (tagKey.location().compareTo(CompatHandler.MEASURING_DEVICE)==0)
                return true;
        }
        return false;
    }

    public static boolean isTinyComponent(Item item) {
        for (TagKey<Item> tagKey: item.getDefaultInstance().tags().toList()) {
            if (tagKey.location().compareTo(CompatHandler.TINY_COMPONENT)==0)
                return true;
        }
        return false;
    }
}

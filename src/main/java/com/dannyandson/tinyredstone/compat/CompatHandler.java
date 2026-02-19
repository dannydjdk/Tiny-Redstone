package com.dannyandson.tinyredstone.compat;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.compat.theoneprobe.PanelProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;

public class CompatHandler {
    static final ResourceLocation MEASURING_DEVICE = ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID, "measuring_device");
    static final ResourceLocation TINY_COMPONENT = ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID, "tiny_component");

    public static void register()  {
        if(ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", PanelProvider::new);
        }
    }

    public static boolean isMeasuringDevice(Item item) {
        for (TagKey<Item> tagKey: item.getDefaultInstance().getTags().toList()) {
            if (tagKey.location().compareTo(CompatHandler.MEASURING_DEVICE)==0)
                return true;
        }
        return false;
    }

    public static boolean isTinyComponent(Item item) {
        for (TagKey<Item> tagKey: item.getDefaultInstance().getTags().toList()) {
            if (tagKey.location().compareTo(CompatHandler.TINY_COMPONENT)==0)
                return true;
        }
        return false;
    }
}

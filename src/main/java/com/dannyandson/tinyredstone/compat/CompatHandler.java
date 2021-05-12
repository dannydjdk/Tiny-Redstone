package com.dannyandson.tinyredstone.compat;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.compat.theoneprobe.PanelProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

public class CompatHandler {
    private static final ResourceLocation MEASURING_DEVICE = new ResourceLocation(TinyRedstone.MODID, "measuring_device");
    private static final ResourceLocation TINY_COMPONENT = new ResourceLocation(TinyRedstone.MODID, "tiny_component");

    public static void register()  {
        if(ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", PanelProvider::new);
        }
    }

    public static boolean isMeasuringDevice(Item item) {
        ITag<Item> tag = ItemTags.getCollection().get(CompatHandler.MEASURING_DEVICE);
        if(tag == null || !tag.contains(item)) return false;
        return true;
    }

    public static boolean isTinyComponent(Item item) {
        ITagCollection<Item> collection = ItemTags.getCollection();
        ITag<Item> tag = collection.get(CompatHandler.MEASURING_DEVICE);
        if(tag == null || !tag.contains(item)) {
            tag = collection.get(CompatHandler.TINY_COMPONENT);
            if(tag == null || !tag.contains(item)) {
                return false;
            }
        }
        return true;
    }
}

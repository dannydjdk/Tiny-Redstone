package com.dannyandson.tinyredstone.compat;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.compat.theoneprobe.PanelProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

public class CompatHandler {
    static final ResourceLocation MEASURING_DEVICE = new ResourceLocation(TinyRedstone.MODID, "measuring_device");
    static final ResourceLocation TINY_COMPONENT = new ResourceLocation(TinyRedstone.MODID, "tiny_component");

    public static void register()  {
        if(ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", PanelProvider::new);
        }
    }

    public static boolean isMeasuringDevice(Item item) {
        ITag<Item> tag = ItemTags.getAllTags().getTag(CompatHandler.MEASURING_DEVICE);
        if(tag == null || !tag.contains(item)) return false;
        return true;
    }

    public static boolean isTinyComponent(Item item) {
        ITagCollection<Item> collection = ItemTags.getAllTags();
        ITag<Item> tag = collection.getTag(CompatHandler.MEASURING_DEVICE);
        if(tag == null || !tag.contains(item)) {
            tag = collection.getTag(CompatHandler.TINY_COMPONENT);
            if(tag == null || !tag.contains(item)) {
                return false;
            }
        }
        return true;
    }
}

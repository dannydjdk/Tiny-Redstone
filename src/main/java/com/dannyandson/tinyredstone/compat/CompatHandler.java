package com.dannyandson.tinyredstone.compat;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

public class CompatHandler {
    static final ResourceLocation MEASURING_DEVICE = new ResourceLocation(TinyRedstone.MODID, "measuring_device");
    static final ResourceLocation TINY_COMPONENT = new ResourceLocation(TinyRedstone.MODID, "tiny_component");

//    public static void register()  {
//        if(ModList.get().isLoaded("theoneprobe")) {
//            InterModComms.sendTo("theoneprobe", "getTheOneProbe", PanelProvider::new);
//        }
//    }

    public static boolean isMeasuringDevice(Item item) {
        Tag<Item> tag = ItemTags.getAllTags().getTag(CompatHandler.MEASURING_DEVICE);
        return tag != null && tag.contains(item);
    }

    public static boolean isTinyComponent(Item item) {
        TagCollection<Item> collection = ItemTags.getAllTags();
        Tag<Item> tag = collection.getTag(CompatHandler.MEASURING_DEVICE);
        if(tag == null || !tag.contains(item)) {
            tag = collection.getTag(CompatHandler.TINY_COMPONENT);
            if(tag == null || !tag.contains(item)) {
                return false;
            }
        }
        return true;
    }
}

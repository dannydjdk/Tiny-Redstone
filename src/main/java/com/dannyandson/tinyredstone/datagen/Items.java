package com.dannyandson.tinyredstone.datagen;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class Items extends ItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, TinyRedstone.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        singleTexture(ForgeRegistries.ITEMS.getKey(Registration.REDSTONE_PANEL_ITEM.get()).getPath(), new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(TinyRedstone.MODID, "item/redstone_panel"));
    }
}

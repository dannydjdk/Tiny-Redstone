package com.dannyandson.tinyredstone.datagen;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class Items extends ItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, TinyRedstone.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        singleTexture(Registration.REDSTONE_PANEL_ITEM.get().getRegistryName().getPath(), new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(TinyRedstone.MODID, "item/redstone_panel"));
    }
}

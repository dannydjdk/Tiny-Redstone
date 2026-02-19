package com.dannyandson.tinyredstone.datagen;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.core.registries.BuiltInRegistries;

public class Items extends ItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), TinyRedstone.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        singleTexture(BuiltInRegistries.ITEM.getKey(Registration.REDSTONE_PANEL_ITEM.get()).getPath(), ResourceLocation.withDefaultNamespace("item/handheld"),
                "layer0", ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID, "item/redstone_panel"));
    }
}

package com.dannyandson.tinyredstone.datagen;


import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStates extends BlockStateProvider
{
    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, TinyRedstone.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(Registration.REDSTONE_PANEL_BLOCK.get());
    }
}

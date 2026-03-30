package com.dannyandson.tinyredstone.datagen;


import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BlockStates extends BlockStateProvider
{
    public BlockStates(PackOutput gen, ExistingFileHelper exFileHelper) {
        super(gen, TinyRedstone.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(ModRegistration.REDSTONE_PANEL_BLOCK.get());
    }
}

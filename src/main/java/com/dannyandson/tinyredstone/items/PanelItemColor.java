package com.dannyandson.tinyredstone.items;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class PanelItemColor implements IItemColor {
    @Override
    public int getColor(ItemStack p_getColor_1_, int p_getColor_2_)
    {
        if (p_getColor_1_.getTag()!=null && p_getColor_1_.getTag().contains("BlockEntityTag") ) {
            CompoundNBT blockEntityTag = p_getColor_1_.getTag().getCompound("BlockEntityTag");
            if (blockEntityTag.contains("color")) {
                int color = blockEntityTag.getInt("color");
                return color;
            }
        }
        return DyeColor.GRAY.getColorValue();
    }
}

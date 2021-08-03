package com.dannyandson.tinyredstone.items;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class PanelItemColor implements ItemColor {
    @Override
    public int getColor(ItemStack p_getColor_1_, int p_getColor_2_)
    {
        if (p_getColor_1_.getTag()!=null && p_getColor_1_.getTag().contains("BlockEntityTag") ) {
            CompoundTag blockEntityTag = p_getColor_1_.getTag().getCompound("BlockEntityTag");
            if (blockEntityTag.contains("color")) {
                int color = blockEntityTag.getInt("color");
                return color;
            }
        }
        return DyeColor.GRAY.getTextColor();
    }
}

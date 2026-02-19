package com.dannyandson.tinyredstone.items;

import net.minecraft.client.color.item.ItemColor;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class PanelItemColor implements ItemColor {
    @Override
    public int getColor(ItemStack p_getColor_1_, int p_getColor_2_)
    {
        if (ItemStackHelper.getBlockEntityTag(p_getColor_1_) != null) {
            CompoundTag blockEntityTag = ItemStackHelper.getBlockEntityTag(p_getColor_1_);
            if (blockEntityTag.contains("color")) {
                int color = blockEntityTag.getInt("color");
                return color;
            }
        }
        return DyeColor.GRAY.getTextColor();
    }
}

package com.dannyandson.tinyredstone.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TinyColorSelector extends RedstoneWrench {

    @Override
    public  void  appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flags)
    {
        list.add(Component.translatable("message.item.tiny_color_selector"));
    }
}

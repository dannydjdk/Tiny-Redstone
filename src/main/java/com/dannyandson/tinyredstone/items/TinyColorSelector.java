package com.dannyandson.tinyredstone.items;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class TinyColorSelector extends RedstoneWrench {

    @Override
    public  void  appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flags)
    {
        list.add(new TranslatableComponent("message.item.tiny_color_selector"));
    }
}

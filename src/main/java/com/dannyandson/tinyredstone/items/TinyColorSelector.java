package com.dannyandson.tinyredstone.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class TinyColorSelector extends RedstoneWrench {

    public TinyColorSelector(Item.Properties props) {
        super(props);
    }

    @Override
    public  void  appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> textConsumer, TooltipFlag flags)
    {
        textConsumer.accept(Component.translatable("message.item.tiny_color_selector"));
    }
}
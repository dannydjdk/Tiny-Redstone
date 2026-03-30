package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.setup.ModRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class PanelItem extends BlockItem {

    public PanelItem(Item.Properties props)
    {
        super(ModRegistration.REDSTONE_PANEL_BLOCK.get(), props);
    }

    @Override
    public  void  appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> textConsumer, TooltipFlag flags)
    {
        textConsumer.accept(Component.translatable("message.item.redstone_panel"));
    }
}
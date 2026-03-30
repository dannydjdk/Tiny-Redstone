package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.setup.ModRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class PanelItem extends BlockItem {

    public PanelItem(Item.Properties props)
    {
        super(ModRegistration.REDSTONE_PANEL_BLOCK.get(), props);
    }

    // initializeClient removed — client extensions now registered via
    // RegisterClientExtensionsEvent in ClientSetup

    @Override
    public  void  appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flags)
    {
        list.add(Component.translatable("message.item.redstone_panel"));
    }
}

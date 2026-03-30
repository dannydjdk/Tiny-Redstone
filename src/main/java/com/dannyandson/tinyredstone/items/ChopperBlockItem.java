package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.setup.ModRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ChopperBlockItem extends BlockItem {

    public ChopperBlockItem(Item.Properties props) {
        super(ModRegistration.CUTTER_BLOCK.get(), props);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("message." + this.getDescriptionId()).withStyle(ChatFormatting.DARK_GRAY));
    }

}

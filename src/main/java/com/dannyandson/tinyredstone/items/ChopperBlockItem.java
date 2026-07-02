package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.setup.ModRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ChopperBlockItem extends BlockItem {

    public ChopperBlockItem(Item.Properties props) {
        super(ModRegistration.CUTTER_BLOCK.get(), props);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> textConsumer, TooltipFlag flag) {
        textConsumer.accept(Component.translatable("message." + this.getDescriptionId()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
    }

}
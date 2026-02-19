package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.api.AbstractPanelCellItem;
import com.dannyandson.tinyredstone.setup.ModSetup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class PanelCellItem extends AbstractPanelCellItem {
    public PanelCellItem() {
        super(new Item.Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flags) {
        if (Screen.hasShiftDown()) {
            list.add(Component.translatable("message.item.redstone_panel_cell").withStyle(ChatFormatting.GRAY));
            list.add(Component.translatable("message." + this.getDescriptionId()).withStyle(ChatFormatting.RED));
        } else
            list.add(Component.translatable("tinyredstone.tooltip.press_shift").withStyle(ChatFormatting.DARK_GRAY));
    }
}

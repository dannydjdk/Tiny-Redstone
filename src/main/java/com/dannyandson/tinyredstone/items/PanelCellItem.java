package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.api.AbstractPanelCellItem;
import com.dannyandson.tinyredstone.setup.ModSetup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class PanelCellItem extends AbstractPanelCellItem {
    public PanelCellItem() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flags) {
        if (Screen.hasShiftDown()) {
            list.add(new TranslatableComponent("message.item.redstone_panel_cell").withStyle(ChatFormatting.GRAY));
            list.add(new TranslatableComponent("message." + this.getDescriptionId()).withStyle(ChatFormatting.RED));
        } else
            list.add(new TranslatableComponent("tinyredstone.tooltip.press_shift").withStyle(ChatFormatting.DARK_GRAY));
    }
}

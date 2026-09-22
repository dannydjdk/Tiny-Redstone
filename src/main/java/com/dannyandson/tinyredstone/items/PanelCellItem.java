package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.api.AbstractPanelCellItem;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class PanelCellItem extends AbstractPanelCellItem {
    public PanelCellItem(Item.Properties props) {
        super(props);
    }

    private static boolean isShiftKeyDown() {
        return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> textConsumer, TooltipFlag flags) {
        if (isShiftKeyDown()) {
            textConsumer.accept(Component.translatable("message.item.redstone_panel_cell").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
            textConsumer.accept(Component.translatable("message." + this.getDescriptionId()).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        } else
            textConsumer.accept(Component.translatable("tinyredstone.tooltip.press_shift").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
    }
}
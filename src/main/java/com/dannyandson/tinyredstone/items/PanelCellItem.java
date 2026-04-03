package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.api.AbstractPanelCellItem;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class PanelCellItem extends AbstractPanelCellItem {
    public PanelCellItem(Item.Properties props) {
        super(props);
    }

    private static boolean isShiftKeyDown() {
        Window window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> textConsumer, TooltipFlag flags) {
        if (isShiftKeyDown()) {
            textConsumer.accept(Component.translatable("message.item.redstone_panel_cell").withStyle(ChatFormatting.GRAY));
            textConsumer.accept(Component.translatable("message." + this.getDescriptionId()).withStyle(ChatFormatting.RED));
        } else
            textConsumer.accept(Component.translatable("tinyredstone.tooltip.press_shift").withStyle(ChatFormatting.DARK_GRAY));
    }
}
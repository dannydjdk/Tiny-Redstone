package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.api.AbstractPanelCellItem;
import com.dannyandson.tinyredstone.setup.ModSetup;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class PanelCellItem extends AbstractPanelCellItem {
    public PanelCellItem() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
    }

    public PanelCellItem(Item.Properties properties){
        super(properties);
    }

    @Override
    public  void  appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flags) {
        if (Screen.hasShiftDown()) {
            list.add(new TranslationTextComponent("message.item.redstone_panel_cell").withStyle(TextFormatting.GRAY));
            list.add(new TranslationTextComponent("message." + this.getDescriptionId()).withStyle(TextFormatting.RED));
        } else
            list.add(new TranslationTextComponent("tinyredstone.tooltip.press_shift").withStyle(TextFormatting.DARK_GRAY));
    }

}

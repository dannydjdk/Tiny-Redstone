package com.dannyandson.tinyredstone.compat;

import net.minecraft.item.ItemStack;

public interface IToolTipInfo {
    ToolTipInfoMode getMode();
    void setPowerOutput(int power);
    void addText(String text);
    void addText(ItemStack itemStack, String text);
    void addText(String label, String text);
    void addText(ItemStack itemStack, String label, String text);
    void addInfo(String text);
}

package com.dannyandson.tinyredstone.compat;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public interface IToolTipInfo {
    ToolTipInfoMode getMode();
    void setPowerOutput(int power);
    void addText(ITextComponent text);
    default void addText(String text) {
        addText(new StringTextComponent(text));
    }
    void addText(ItemStack itemStack, ITextComponent text);
    default void addText(ItemStack itemStack, String text) {
        addText(itemStack, new StringTextComponent(text));
    }
    void addText(ITextComponent label, ITextComponent text);
    default void addText(String label, String text) {
        addText(new StringTextComponent(label), new StringTextComponent(text));
    }
    void addText(ItemStack itemStack, ITextComponent label, ITextComponent text);
    default void addText(ItemStack itemStack, String label, String text) {
        addText(itemStack, new StringTextComponent(label), new StringTextComponent(text));
    }
}

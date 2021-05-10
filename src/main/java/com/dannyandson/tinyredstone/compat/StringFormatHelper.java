package com.dannyandson.tinyredstone.compat;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class StringFormatHelper {
    public static ITextComponent info(String text) {
        return new StringTextComponent("{=i=}" + text);
    }
}

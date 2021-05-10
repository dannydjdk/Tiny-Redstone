package com.dannyandson.tinyredstone.compat.hwyla;

import com.dannyandson.tinyredstone.compat.IToolTipInfo;
import com.dannyandson.tinyredstone.compat.ToolTipInfoMode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class ToolTipInfo implements IToolTipInfo {
    private List<ITextComponent> tooltip;
    private boolean sneaking;
    protected int power = -1;
    protected ToolTipInfo(List<ITextComponent> tooltip, boolean sneaking) {
        this.tooltip = tooltip;
        this.sneaking = sneaking;
    }
    @Override
    public ToolTipInfoMode getMode() {
        if(sneaking) return ToolTipInfoMode.EXTENDED;
        return ToolTipInfoMode.NORMAL;
    }

    @Override
    public void setPowerOutput(int power) {
        this.power = power;
    }

    @Override
    public void addText(ITextComponent text) {
        this.tooltip.add(text);
    }

    @Override
    public void addText(ItemStack itemStack, ITextComponent text) {
        addText(text);
    }

    @Override
    public void addText(ITextComponent label, ITextComponent text) {
        if (label instanceof IFormattableTextComponent) {
            this.tooltip.add(((IFormattableTextComponent)label).append(new StringTextComponent(": ")).append(text));
        } else {
            this.tooltip.add(label.deepCopy().append(new StringTextComponent(": ")).append(text));
        }
    }

    @Override
    public void addText(ItemStack itemStack, ITextComponent label, ITextComponent text) {
        addText(label, text);
    }
}

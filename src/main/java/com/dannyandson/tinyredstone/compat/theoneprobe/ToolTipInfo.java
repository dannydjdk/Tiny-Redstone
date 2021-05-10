package com.dannyandson.tinyredstone.compat.theoneprobe;

import com.dannyandson.tinyredstone.compat.IToolTipInfo;
import com.dannyandson.tinyredstone.compat.ToolTipInfoMode;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ToolTipInfo implements IToolTipInfo {
    private IProbeInfo probeInfo;
    private ProbeMode probeMode;
    protected int power = -1;
    protected ToolTipInfo(IProbeInfo probeInfo, ProbeMode probeMode) {
        this.probeInfo = probeInfo;
    }

    @Override
    public ToolTipInfoMode getMode() {
        switch (probeMode) {
            case DEBUG: return ToolTipInfoMode.DEBUG;
            case NORMAL: return ToolTipInfoMode.NORMAL;
            case EXTENDED: return ToolTipInfoMode.EXTENDED;
        }
        return ToolTipInfoMode.NORMAL;
    }

    @Override
    public void setPowerOutput(int power) {
        this.power = power;
    }

    @Override
    public void addText(ITextComponent text) {
        this.probeInfo.text(text);
    }

    @Override
    public void addText(ItemStack itemStack, ITextComponent text) {
        this.probeInfo.horizontal().itemLabel(itemStack).text(text);
    }

    @Override
    public void addText(ITextComponent label, ITextComponent text) {
        this.probeInfo.text(CompoundText.createLabelInfo(label + ": ", text));
    }

    @Override
    public void addText(ItemStack itemStack, ITextComponent label, ITextComponent text) {
        this.probeInfo.horizontal().itemLabel(itemStack).text(CompoundText.createLabelInfo(label + ": ", text));
    }
}
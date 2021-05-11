package com.dannyandson.tinyredstone.compat.theoneprobe;

import com.dannyandson.tinyredstone.compat.IToolTipInfo;
import com.dannyandson.tinyredstone.compat.ToolTipInfoMode;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

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
    public void addText(String text) {
        this.probeInfo.text(new StringTextComponent(text));
    }

    @Override
    public void addText(ItemStack itemStack, String text) {
        this.probeInfo.horizontal().itemLabel(itemStack).text(new StringTextComponent(text));
    }

    @Override
    public void addText(String label, String text) {
        this.probeInfo.text(CompoundText.createLabelInfo(label + ": ", text));
    }

    @Override
    public void addText(ItemStack itemStack, String label, String text) {
        this.probeInfo.horizontal().item(itemStack, this.probeInfo.defaultItemStyle().width(14).height(14)).text(CompoundText.createLabelInfo(label + ": ", text));
    }

    @Override
    public void addInfo(String text) {
        this.probeInfo.text(CompoundText.create().style(TextStyleClass.INFO).text(text));
    }
}
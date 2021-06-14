package com.dannyandson.tinyredstone.compat.theoneprobe;

import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.compat.OverlayBlockInfoMode;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class OverlayBlockInfo implements IOverlayBlockInfo {
    private final IProbeInfo probeInfo;
    private final ProbeMode probeMode;
    protected int power = -1;
    protected OverlayBlockInfo(IProbeInfo probeInfo, ProbeMode probeMode) {
        this.probeInfo = probeInfo;
        this.probeMode = probeMode;
    }

    @Override
    public OverlayBlockInfoMode getMode() {
        switch (probeMode) {
            case DEBUG: return OverlayBlockInfoMode.DEBUG;
            case NORMAL: return OverlayBlockInfoMode.NORMAL;
            case EXTENDED: return OverlayBlockInfoMode.EXTENDED;
        }
        return OverlayBlockInfoMode.NORMAL;
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
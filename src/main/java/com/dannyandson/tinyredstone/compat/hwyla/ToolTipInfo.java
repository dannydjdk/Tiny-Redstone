package com.dannyandson.tinyredstone.compat.hwyla;

import com.dannyandson.tinyredstone.compat.IToolTipInfo;
import com.dannyandson.tinyredstone.compat.ToolTipInfoMode;
import mcp.mobius.waila.api.RenderableTextComponent;
import net.minecraft.item.ItemStack;
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
    public void addText(String text) {
        this.tooltip.add(new StringTextComponent(text));
    }

    @Override
    public void addText(ItemStack itemStack, String text) {
        this.tooltip.add(new RenderableTextComponent(
            PanelProvider.getItemStackRenderable(itemStack),
            PanelProvider.getStringRenderable(text)
        ));
    }

    @Override
    public void addText(String label, String text) {
        this.tooltip.add(new RenderableTextComponent(
            PanelProvider.getStringRenderable(label),
            PanelProvider.getStringRenderable(": "),
            PanelProvider.getStringRenderable(text)
        ));
    }

    @Override
    public void addText(ItemStack itemStack, String label, String text) {
        this.tooltip.add(new RenderableTextComponent(
            PanelProvider.getItemStackRenderable(itemStack),
            PanelProvider.getStringRenderable(label),
            PanelProvider.getStringRenderable(": "),
            PanelProvider.getStringRenderable(text)
        ));
    }

    @Override
    public void addInfo(String text) {
        this.tooltip.add(new RenderableTextComponent(
                PanelProvider.getInfoStringRenderable(text)
        ));
    }
}

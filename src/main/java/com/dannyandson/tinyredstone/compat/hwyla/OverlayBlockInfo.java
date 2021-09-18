package com.dannyandson.tinyredstone.compat.hwyla;

import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.compat.OverlayBlockInfoMode;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OverlayBlockInfo implements IOverlayBlockInfo {
    private final ITooltip tooltip;
    private final boolean sneaking;
    protected int power = -1;
    protected OverlayBlockInfo(ITooltip tooltip, boolean sneaking) {
        this.tooltip = tooltip;
        this.sneaking = sneaking;
    }
    @Override
    public OverlayBlockInfoMode getMode() {
        if(sneaking) return OverlayBlockInfoMode.EXTENDED;
        return OverlayBlockInfoMode.NORMAL;
    }

    @Override
    public void setPowerOutput(int power) {
        this.power = power;
    }

    @Override
    public void addText(String text) {
        this.tooltip.add(tooltip.getElementHelper().text(Component.nullToEmpty(text)));
    }

    @Override
    public void addText(ItemStack itemStack, String text) {
        IElementHelper helper = tooltip.getElementHelper();
        List<IElement> elements = new ArrayList<>();
        elements.add(helper.item(itemStack).tag(PanelProvider.RENDER_ITEM_INLINE));
        elements.add(helper.text(Component.nullToEmpty(text)).tag(PanelProvider.RENDER_STRING));
        tooltip.add(elements);
    }

    @Override
    public void addText(String label, String text) {
        tooltip.add(tooltip.getElementHelper().text(Component.nullToEmpty(label + ": " + text)).tag(PanelProvider.RENDER_STRING));
    }

    @Override
    public void addText(ItemStack itemStack, String label, String text) {
        IElementHelper helper = tooltip.getElementHelper();
        List<IElement> elements = new ArrayList<>();
        elements.add(helper.item(itemStack).tag(PanelProvider.RENDER_ITEM_INLINE));
        elements.add(helper.text(Component.nullToEmpty(label + ": " + text)).tag(PanelProvider.RENDER_STRING));
        tooltip.add(elements);

    }

    @Override
    public void addInfo(String text) {
        CompoundTag tag = new CompoundTag();
        tag.putString("string", text);
        tooltip.add(Component.nullToEmpty(text), PanelProvider.RENDER_INFO_STRING);
    }
}

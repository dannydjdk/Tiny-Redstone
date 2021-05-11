package com.dannyandson.tinyredstone.compat.hwyla;

import com.dannyandson.tinyredstone.compat.IToolTipInfo;
import com.dannyandson.tinyredstone.compat.ToolTipInfoMode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import mcp.mobius.waila.api.RenderableTextComponent;
import mcp.mobius.waila.addons.minecraft.PluginMinecraft;
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
        this.tooltip.add(new RenderableTextComponent(
            getItemStackRenderable(itemStack),
            getITextComponentRenderable(text.getString())
        ));
    }

    @Override
    public void addText(ITextComponent label, ITextComponent text) {
        this.tooltip.add(new RenderableTextComponent(
            getITextComponentRenderable(label.getString()),
            getITextComponentRenderable(": "),
            getITextComponentRenderable(text.getString())
        ));
    }

    @Override
    public void addText(ItemStack itemStack, ITextComponent label, ITextComponent text) {
        this.tooltip.add(new RenderableTextComponent(
            getItemStackRenderable(itemStack),
            getITextComponentRenderable(label.getString()),
            getITextComponentRenderable(": "),
            getITextComponentRenderable(text.getString())
        ));
    }

    private static RenderableTextComponent getItemStackRenderable(ItemStack itemStack) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("id", itemStack.getItem().getRegistryName().toString());
        return new RenderableTextComponent(PluginMinecraft.RENDER_ITEM, tag);
    }

    private static RenderableTextComponent getITextComponentRenderable(ITextComponent textComponent) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("string", ITextComponent.Serializer.componentToJson(textComponent));
        return new RenderableTextComponent(PanelProvider.RENDER_STRING, tag);
    }
}

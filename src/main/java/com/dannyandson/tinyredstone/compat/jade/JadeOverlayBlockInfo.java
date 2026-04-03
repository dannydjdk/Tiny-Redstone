package com.dannyandson.tinyredstone.compat.jade;

import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.compat.OverlayBlockInfoMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.ITooltip;

/**
 * Adapts Jade's {@link ITooltip} to the mod's {@link IOverlayBlockInfo}
 * interface so that every existing {@code IPanelCellInfoProvider.addInfo()}
 * implementation works with Jade without any changes.
 */
public class JadeOverlayBlockInfo implements IOverlayBlockInfo {

    private final ITooltip tooltip;
    private int power = -1;

    public JadeOverlayBlockInfo(ITooltip tooltip) {
        this.tooltip = tooltip;
    }

    int getPower() {
        return power;
    }

    @Override
    public OverlayBlockInfoMode getMode() {
        // Jade doesn't expose a "probe mode" the way The One Probe does.
        // Default to NORMAL; extended info is gated by the mod's own
        // DISPLAY_MODE config (handled in JadePanelComponentProvider).
        return OverlayBlockInfoMode.NORMAL;
    }

    @Override
    public void setPowerOutput(int power) {
        this.power = power;
    }

    @Override
    public void addText(String text) {
        tooltip.add(Component.literal(text));
    }

    @Override
    public void addText(ItemStack itemStack, String text) {
        tooltip.add(Component.literal(text));
    }

    @Override
    public void addText(String label, String text) {
        tooltip.add(Component.literal(label + ": " + text));
    }

    @Override
    public void addText(ItemStack itemStack, String label, String text) {
        tooltip.add(Component.literal(label + ": " + text));
    }

    @Override
    public void addInfo(String text) {
        tooltip.add(Component.literal(text));
    }
}
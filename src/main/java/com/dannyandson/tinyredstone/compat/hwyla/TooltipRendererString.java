package com.dannyandson.tinyredstone.compat.hwyla;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.api.impl.config.WailaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;

import java.awt.*;

public class TooltipRendererString implements ITooltipRenderer {
    private final Minecraft client;
    public TooltipRendererString() {
        this.client = Minecraft.getInstance();
    }

    @Override
    public Dimension getSize(CompoundNBT data, ICommonAccessor accessor) {
        return new Dimension(client.font.width(data.getString("string")), client.font.lineHeight + 1);
    }

    @Override
    public void draw(CompoundNBT data, ICommonAccessor accessor, int x, int y) {
        WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
        client.font.draw(new MatrixStack(), data.getString("string"), x, y, color.getFontColor());
    }
}
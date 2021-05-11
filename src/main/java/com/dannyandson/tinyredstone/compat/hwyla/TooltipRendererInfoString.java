package com.dannyandson.tinyredstone.compat.hwyla;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.api.impl.config.WailaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;

import java.awt.*;

public class TooltipRendererInfoString implements ITooltipRenderer {
    private static final float scale = 0.8f;
    private final Minecraft client;
    public TooltipRendererInfoString() {
        this.client = Minecraft.getInstance();
    }

    @Override
    public Dimension getSize(CompoundNBT data, ICommonAccessor accessor) {
        return new Dimension((int) (client.fontRenderer.getStringWidth(data.getString("string")) * scale), (int) ((client.fontRenderer.FONT_HEIGHT + 1) * scale));
    }

    @Override
    public void draw(CompoundNBT data, ICommonAccessor accessor, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.scalef(scale, scale, 1f);
        x /= scale;
        y /= scale;
        WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
        client.fontRenderer.drawStringWithShadow(new MatrixStack(), data.getString("string"), x, y, color.getFontColor());
        GlStateManager.popMatrix();
    }
}
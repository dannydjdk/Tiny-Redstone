package com.dannyandson.tinyredstone.compat.hwyla;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.api.impl.config.WailaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

import java.awt.*;

public class TooltipRendererInfoString implements ITooltipRenderer {
    private static final float scale = 0.8f;
    private final Minecraft client;
    public TooltipRendererInfoString() {
        this.client = Minecraft.getInstance();
    }

    @Override
    public Dimension getSize(CompoundNBT data, ICommonAccessor accessor) {
        return new Dimension((int) (client.font.width(data.getString("string")) * scale), (int) ((client.font.lineHeight + 1) * scale));
    }

    @Override
    public void draw(CompoundNBT data, ICommonAccessor accessor, int x, int y) {
        RenderSystem.pushMatrix();
        RenderSystem.scalef(scale, scale, 1f);
        x /= scale;
        y /= scale;
        WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
        ITextComponent info = new StringTextComponent(data.getString("string")).setStyle(Style.EMPTY.withItalic(true));
        client.font.draw(new MatrixStack(), info, x, y, color.getFontColor());
        client.font.drawShadow(new MatrixStack(), info, x, y, color.getFontColor());
        RenderSystem.popMatrix();
    }
}
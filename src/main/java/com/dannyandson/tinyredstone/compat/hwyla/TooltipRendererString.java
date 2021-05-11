package com.dannyandson.tinyredstone.compat.hwyla;

import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import net.minecraft.util.text.ITextComponent;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.impl.config.WailaConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.nbt.CompoundNBT;
import java.awt.Dimension;

public class TooltipRendererString implements ITooltipRenderer {
    private final Minecraft client;
    public TooltipRendererString() {
        this.client = Minecraft.getInstance();
    }

    @Override
    public Dimension getSize(CompoundNBT data, ICommonAccessor accessor) {
        return new Dimension(client.fontRenderer.getStringWidth(data.getString("string")), client.fontRenderer.FONT_HEIGHT + 1);
    }

    @Override
    public void draw(CompoundNBT data, ICommonAccessor accessor, int x, int y) {
        WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        client.fontRenderer.drawStringWithShadow(new MatrixStack(), data.getString("string"), x, y, color.getFontColor());
        RenderSystem.enableLighting();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
    }
}
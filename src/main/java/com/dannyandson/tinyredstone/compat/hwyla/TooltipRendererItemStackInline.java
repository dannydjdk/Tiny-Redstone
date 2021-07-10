package com.dannyandson.tinyredstone.compat.hwyla;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.overlay.DisplayUtil;
import mcp.mobius.waila.utils.WailaExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;

public class TooltipRendererItemStackInline implements ITooltipRenderer {
    private final Minecraft client;
    public TooltipRendererItemStackInline() {
        this.client = Minecraft.getInstance();
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Dimension getSize(CompoundNBT tag, ICommonAccessor accessor) {
        return new Dimension(client.font.lineHeight, client.font.lineHeight);
    }

    public void draw(CompoundNBT tag, ICommonAccessor accessor, int x, int y) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("id")));
        if (item != Items.AIR) {
            CompoundNBT stackTag = null;

            try {
                stackTag = JsonToNBT.parseTag(tag.getString("nbt"));
            } catch (CommandSyntaxException var9) {
            }

            ItemStack stack = new ItemStack(item, 1);
            stack.setTag(stackTag);

            DisplayUtil.enable3DRender();

            try {
                float scale = client.font.lineHeight/18f;
                RenderSystem.pushMatrix();
                RenderSystem.scalef(scale, scale, 1f);
                x /= scale;
                y /= scale;
                client.getItemRenderer().renderGuiItem(stack, x, y);
                client.getItemRenderer().renderGuiItemDecorations(client.font, stack, x, y, null);
                RenderSystem.popMatrix();
            } catch (Exception var5) {
                String stackStr = stack.toString();
                WailaExceptionHandler.handleErr(var5, "renderStack | " + stackStr, null);
            }

            DisplayUtil.enable2DRender();
        }
    }
}

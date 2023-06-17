package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.panelcells.Repeater;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.RepeaterTickSync;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RepeaterCellGUI extends Screen {

    private static final int WIDTH = 190;
    private static final int HEIGHT = 100;

    private final PanelTile panelTile;
    private final Integer cellIndex;
    private final Repeater repeaterCell;
    private ModWidget tickCount;

    private final ResourceLocation GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    protected RepeaterCellGUI(PanelTile panelTile, Integer cellIndex, Repeater repeaterCell) {
        super(Component.translatable("tinyredstone:repeaterGUI"));
        this.panelTile = panelTile;
        this.cellIndex = cellIndex;
        this.repeaterCell = repeaterCell;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        Integer redstoneTicks = repeaterCell.getTicks()/2;

        Float tSeconds = redstoneTicks.floatValue()/10f;
        this.tickCount = new ModWidget(relX,relY+38,WIDTH,20, Component.nullToEmpty(redstoneTicks.toString() + " ticks (" + tSeconds.toString() + " seconds)"))
            .setTextHAlignment(ModWidget.HAlignment.CENTER).setTextVAlignment(ModWidget.VAlignment.MIDDLE);

        addRenderableWidget(new ModWidget(relX-1, relY-1, WIDTH+2, HEIGHT+2, 0xAA000000));
        addRenderableWidget(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EEEEEE));
        addRenderableWidget(ModWidget.buildButton(relX + 55, relY + 48, 80, 20, Component.translatable("tinyredstone.close"), button -> close()));
        addRenderableWidget(this.tickCount);

        addRenderableWidget(new ModWidget(relX,relY+3,WIDTH-2,20,Component.translatable("tinyredstone.gui.repeater.msg")))
            .setTextHAlignment(ModWidget.HAlignment.CENTER);
        addRenderableWidget(ModWidget.buildButton(relX + 15, relY + 15, 20, 20, Component.nullToEmpty("---"), button -> changeTicks(-200)));
        addRenderableWidget(ModWidget.buildButton(relX + 40, relY + 15, 20, 20, Component.nullToEmpty("--"), button -> changeTicks(-20)));
        addRenderableWidget(ModWidget.buildButton(relX + 65, relY + 15, 20, 20, Component.nullToEmpty("-"), button -> changeTicks(-2)));

        addRenderableWidget(ModWidget.buildButton(relX + 105, relY + 15, 20, 20, Component.nullToEmpty("+"), button -> changeTicks(2)));
        addRenderableWidget(ModWidget.buildButton(relX + 130, relY + 15, 20, 20, Component.nullToEmpty("++"), button -> changeTicks(20)));
        addRenderableWidget(ModWidget.buildButton(relX + 155, relY + 15, 20, 20, Component.nullToEmpty("+++"), button -> changeTicks(200)));

        addRenderableWidget(new ModWidget(relX,relY+73,WIDTH-2,20,Component.translatable("tinyredstone.gui.repeater.msg2"),0xFFFFFFFF))
                .setTextHAlignment(ModWidget.HAlignment.CENTER);
        addRenderableWidget(new ModWidget(relX,relY+88,WIDTH-2,20,Component.translatable("tinyredstone.gui.repeater.msg3"),0xFFFFFFFF))
                .setTextHAlignment(ModWidget.HAlignment.CENTER);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        if (
                mouseX>(this.width - WIDTH) / 2 &&
                        mouseX<(this.width + WIDTH) / 2 &&
                        mouseY>(this.height - HEIGHT) / 2 &&
                        mouseY<(this.height + HEIGHT) / 2
        ) {
            if (scroll != 0) {
                Double dScroll = scroll*2;
                if (hasShiftDown())
                    dScroll *= 10;
                changeTicks(dScroll.intValue());
                return true;
            }
            return false;
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    private void close() {
        minecraft.setScreen(null);
    }

    private void changeTicks(int change)
    {
        repeaterCell.setTicks(repeaterCell.getTicks()+change);

        ModNetworkHandler.sendToServer(new RepeaterTickSync(panelTile.getBlockPos(),cellIndex, repeaterCell.getTicks()));

        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        Integer redstoneTicks = repeaterCell.getTicks()/2;
        this.removeWidget(this.tickCount);
        Float tSeconds = redstoneTicks.floatValue()/10f;
        this.tickCount = new ModWidget(relX,relY+38,WIDTH,20, Component.nullToEmpty(redstoneTicks.toString() + " ticks (" + tSeconds.toString() + " seconds)"))
                .setTextHAlignment(ModWidget.HAlignment.CENTER).setTextVAlignment(ModWidget.VAlignment.MIDDLE);
        addRenderableWidget(this.tickCount);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, GUI);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindForSetup(GUI);
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        guiGraphics.blit(GUI, relX, relY, 0, 0, WIDTH, HEIGHT);

        super.render(guiGraphics,mouseX, mouseY, partialTicks);
    }


    public static void open(PanelTile panelTile, Integer cellIndex, Repeater repeaterCell) {
        Minecraft.getInstance().setScreen(new RepeaterCellGUI(panelTile, cellIndex, repeaterCell));
    }

}

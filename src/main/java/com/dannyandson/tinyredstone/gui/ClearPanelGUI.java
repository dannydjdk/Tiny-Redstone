package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.network.ClearPanelSync;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ClearPanelGUI extends Screen {

    private static final int WIDTH = 220;
    private static final int HEIGHT = 60;

    private final Identifier GUI = Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "textures/gui/transparent.png");

    private final PanelTile panelTile;

    protected ClearPanelGUI(PanelTile panelTile) {
        super(Component.translatable("tinyredstone.gui.clearpanel.msg"));
        this.panelTile=panelTile;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        addRenderableOnly(new ModWidget(relX-1, relY-1, WIDTH+2, HEIGHT+2, 0xAA000000));
        addRenderableOnly(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EEEEEE));

        addRenderableOnly(new ModWidget(relX,relY+10,WIDTH,20,Component.translatable("tinyredstone.gui.clearpanel.msg")))
                .setTextHAlignment(ModWidget.HAlignment.CENTER);
        addRenderableWidget(Button.builder(Component.translatable("tinyredstone.yes"),button -> removeCells())
                .pos(relX + 20, relY + 30)
                .size(80, 20)
                .build()
        );

        addRenderableWidget(Button.builder(Component.translatable("tinyredstone.cancel"),button -> close())
                .pos(relX + 120, relY + 30)
                .size(80, 20)
                .build()
        );

    }

    private void removeCells(){
        ModNetworkHandler.sendToServer(new ClearPanelSync(panelTile.getBlockPos()));
        close();
    }

    private void close() {
        minecraft.setScreen(null);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, WIDTH, HEIGHT, 256, 256);

        super.extractRenderState(guiGraphics,mouseX, mouseY, partialTicks);
    }


    public static void open(PanelTile panelTile) {
        Minecraft.getInstance().setScreen(new ClearPanelGUI(panelTile));
    }
}
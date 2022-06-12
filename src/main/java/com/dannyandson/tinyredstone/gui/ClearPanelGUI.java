package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.network.ClearPanelSync;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ClearPanelGUI extends Screen {

    private static final int WIDTH = 220;
    private static final int HEIGHT = 60;

    private final ResourceLocation GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    private final PanelTile panelTile;

    protected ClearPanelGUI(PanelTile panelTile) {
        super(Component.translatable("tinyredstone.gui.clearpanel.msg"));
        this.panelTile=panelTile;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        addRenderableWidget(new ModWidget(relX-1, relY-1, WIDTH+2, HEIGHT+2, 0xAA000000));
        addRenderableWidget(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EEEEEE));

        addRenderableWidget(new ModWidget(relX,relY+10,WIDTH,20,Component.translatable("tinyredstone.gui.clearpanel.msg")))
            .setTextHAlignment(ModWidget.HAlignment.CENTER);
        addRenderableWidget(new Button(relX + 20, relY + 30, 80, 20, Component.translatable("tinyredstone.yes"), button -> removeCells()));
        addRenderableWidget(new Button(relX + 120, relY + 30, 80, 20, Component.translatable("tinyredstone.cancel"), button -> close()));

    }

    private void removeCells(){
        ModNetworkHandler.sendToServer(new ClearPanelSync(panelTile.getBlockPos()));
        close();
    }

    private void close() {
        minecraft.setScreen(null);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, GUI);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindForSetup(GUI);
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, WIDTH, HEIGHT);

        super.render(matrixStack,mouseX, mouseY, partialTicks);
    }


    public static void open(PanelTile panelTile) {
        Minecraft.getInstance().setScreen(new ClearPanelGUI(panelTile));
    }
}

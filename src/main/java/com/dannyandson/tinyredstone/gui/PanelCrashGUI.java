package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.network.CrashFlagResetSync;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public class PanelCrashGUI  extends Screen {

    private final ResourceLocation GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    private static final int WIDTH = 250;
    private static final int HEIGHT = 90;

    private final PanelTile panelTile;

    protected PanelCrashGUI(PanelTile panelTile) {
        super(Component.translatable("tinyredstone:crashGUI"));
        this.panelTile = panelTile;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        addRenderableWidget(new ModWidget(relX-1, relY-1, WIDTH+2, HEIGHT+2, 0xAA000000));
        addRenderableWidget(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EE0000));

        Component crashTextComponent = Component.translatable((panelTile.isCrashed())?"tinyredstone.gui.crash.msg":"tinyredstone.gui.overflow.msg");
        int lineY = relY+2;
        for (String line : crashTextComponent.getString().split("\n",5))
        {
            addRenderableWidget(new ModWidget(relX+2,lineY,WIDTH-2,50, Component.nullToEmpty(line)));
            lineY+=10;
        }


        addRenderableWidget(new Button(relX + 60, relY + 68, 60, 20, Component.translatable("tinyredstone.enable"), button -> enable()));
        addRenderableWidget(new Button(relX + 140, relY + 68, 60, 20, Component.translatable("tinyredstone.close"), button -> close()));


    }

    private void close() {
        minecraft.setScreen(null);
    }

    private void enable() {
        panelTile.resetCrashFlag();
        panelTile.resetOverflownFlag();
        ModNetworkHandler.sendToServer(new CrashFlagResetSync(panelTile.getBlockPos()));
        this.close();
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
        Minecraft.getInstance().setScreen(new PanelCrashGUI(panelTile));
    }

}

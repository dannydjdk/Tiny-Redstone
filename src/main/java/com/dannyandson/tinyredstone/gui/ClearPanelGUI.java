package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.network.ClearPanelSync;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class ClearPanelGUI extends Screen {

    private static final int WIDTH = 220;
    private static final int HEIGHT = 60;

    private final ResourceLocation GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    private final PanelTile panelTile;

    protected ClearPanelGUI(PanelTile panelTile) {
        super(new TranslationTextComponent("tinyredstone.gui.clearpanel.msg"));
        this.panelTile=panelTile;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        addButton(new ModWidget(relX-1, relY-1, WIDTH+2, HEIGHT+2, 0xAA000000));
        addButton(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EEEEEE));

        addButton(new ModWidget(relX,relY+10,WIDTH,20,new TranslationTextComponent("tinyredstone.gui.clearpanel.msg")))
            .setTextHAlignment(ModWidget.HAlignment.CENTER);
        addButton(new Button(relX + 20, relY + 30, 80, 20, new TranslationTextComponent("tinyredstone.yes"), button -> removeCells()));
        addButton(new Button(relX + 120, relY + 30, 80, 20, new TranslationTextComponent("tinyredstone.cancel"), button -> close()));

    }

    private void removeCells(){
        ModNetworkHandler.sendToServer(new ClearPanelSync(panelTile.getPos()));
        close();
    }

    private void close() {
        minecraft.displayGuiScreen(null);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, WIDTH, HEIGHT);

        super.render(matrixStack,mouseX, mouseY, partialTicks);
    }


    public static void open(PanelTile panelTile) {
        Minecraft.getInstance().displayGuiScreen(new ClearPanelGUI(panelTile));
    }
}

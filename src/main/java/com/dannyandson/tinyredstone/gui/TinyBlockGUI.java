package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IColorablePanelCell;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.TinyBlockColorSync;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class TinyBlockGUI extends Screen {

    private static final int WIDTH = 170;
    private static final int HEIGHT = 90;

    private final PanelTile panelTile;
    private final Integer cellIndex;
    private final IColorablePanelCell iColorablePanelCell;

    private final ResourceLocation GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    protected TinyBlockGUI(PanelTile panelTile, Integer cellIndex, IColorablePanelCell iColorablePanelCell) {
        super(Component.translatable("tinyredstone:tinyBlockGUI"));
        this.panelTile = panelTile;
        this.cellIndex = cellIndex;
        this.iColorablePanelCell = iColorablePanelCell;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;



        addRenderableWidget(new ModWidget(relX-1, relY-1, WIDTH+2, HEIGHT+2, 0xAA000000));
        addRenderableWidget(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EEEEEE));
        addRenderableWidget(ModWidget.buildButton(relX + 45, relY + 68, 80, 20, Component.translatable("tinyredstone.close"), button -> close()));

        addRenderableWidget(new ModWidget(relX + 5, relY+ 20,20,20, DyeColor.WHITE.getTextColor()+0xFF000000-1, button->setColor(DyeColor.WHITE.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 25, relY+ 20,20,20, DyeColor.BLACK.getTextColor()+0xFF000000, button->setColor(DyeColor.BLACK.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 45, relY+ 20,20,20, DyeColor.RED.getTextColor()+0xFF000000, button->setColor(DyeColor.RED.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 65, relY+ 20,20,20, DyeColor.GREEN.getTextColor()+0xFF000000, button->setColor(DyeColor.GREEN.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 85, relY+ 20,20,20, DyeColor.BROWN.getTextColor()+0xFF000000, button->setColor(DyeColor.BROWN.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 105, relY+ 20,20,20, DyeColor.BLUE.getTextColor()+0xFF000000, button->setColor(DyeColor.BLUE.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 125, relY+ 20,20,20, DyeColor.PURPLE.getTextColor()+0xFF000000, button->setColor(DyeColor.PURPLE.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 145, relY+ 20,20,20, DyeColor.CYAN.getTextColor()+0xFF000000, button->setColor(DyeColor.CYAN.getMapColor().col)));

        addRenderableWidget(new ModWidget(relX + 5, relY+ 40,20,20, DyeColor.LIGHT_GRAY.getTextColor()+0xFF000000, button->setColor(DyeColor.LIGHT_GRAY.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 25, relY+ 40,20,20, DyeColor.GRAY.getTextColor()+0xFF000000, button->setColor(DyeColor.GRAY.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 45, relY+ 40,20,20, DyeColor.PINK.getTextColor()+0xFF000000, button->setColor(DyeColor.PINK.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 65, relY+ 40,20,20, DyeColor.LIME.getTextColor()+0xFF000000, button->setColor(DyeColor.LIME.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 85, relY+ 40,20,20, DyeColor.YELLOW.getTextColor()+0xFF000000, button->setColor(DyeColor.YELLOW.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 105, relY+ 40,20,20, DyeColor.LIGHT_BLUE.getTextColor()+0xFF000000, button->setColor(DyeColor.LIGHT_BLUE.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 125, relY+ 40,20,20, DyeColor.MAGENTA.getTextColor()+0xFF000000, button->setColor(DyeColor.MAGENTA.getMapColor().col)));
        addRenderableWidget(new ModWidget(relX + 145, relY+ 40,20,20, DyeColor.ORANGE.getTextColor()+0xFF000000, button->setColor(DyeColor.ORANGE.getMapColor().col)));


        addRenderableWidget(new ModWidget(relX,relY+3,WIDTH-2,20,Component.translatable("tinyredstone.gui.tinyblock.msg")))
                .setTextHAlignment(ModWidget.HAlignment.CENTER);



    }

    private void close() {
        minecraft.setScreen(null);
    }

    private void setColor(int color)
    {
        this.iColorablePanelCell.setColor(color);
        ModNetworkHandler.sendToServer(new TinyBlockColorSync(panelTile.getBlockPos(),cellIndex, color));
        this.close();
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


    public static void open(PanelTile panelTile, Integer cellIndex, IColorablePanelCell iColorablePanelCell) {
        Minecraft.getInstance().setScreen(new TinyBlockGUI(panelTile, cellIndex, iColorablePanelCell));
    }
}

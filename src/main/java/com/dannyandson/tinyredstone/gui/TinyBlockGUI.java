package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.TinyBlock;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.TinyBlockColorSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TinyBlockGUI extends Screen {

    private static final int WIDTH = 170;
    private static final int HEIGHT = 90;

    private final PanelTile panelTile;
    private final Integer cellIndex;
    private final TinyBlock tinyBlockCell;

    private final ResourceLocation GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    protected TinyBlockGUI(PanelTile panelTile, Integer cellIndex, TinyBlock tinyBlockCell) {
        super(new TranslationTextComponent("tinyredstone:tinyBlockGUI"));
        this.panelTile = panelTile;
        this.cellIndex = cellIndex;
        this.tinyBlockCell = tinyBlockCell;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;



        addButton(new ModWidget(relX-1, relY-1, WIDTH+2, HEIGHT+2, 0xAA000000));
        addButton(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EEEEEE));
        addButton(new Button(relX + 45, relY + 68, 80, 20, new TranslationTextComponent("tinyredstone.close"), button -> close()));

        addButton(new ModWidget(relX + 5, relY+ 20,20,20, DyeColor.WHITE.getTextColor()+0xFF000000-1, button->setColor(DyeColor.WHITE.getColorValue())));
        addButton(new ModWidget(relX + 25, relY+ 20,20,20, DyeColor.BLACK.getTextColor()+0xFF000000, button->setColor(DyeColor.BLACK.getColorValue())));
        addButton(new ModWidget(relX + 45, relY+ 20,20,20, DyeColor.RED.getTextColor()+0xFF000000, button->setColor(DyeColor.RED.getColorValue())));
        addButton(new ModWidget(relX + 65, relY+ 20,20,20, DyeColor.GREEN.getTextColor()+0xFF000000, button->setColor(DyeColor.GREEN.getColorValue())));
        addButton(new ModWidget(relX + 85, relY+ 20,20,20, DyeColor.BROWN.getTextColor()+0xFF000000, button->setColor(DyeColor.BROWN.getColorValue())));
        addButton(new ModWidget(relX + 105, relY+ 20,20,20, DyeColor.BLUE.getTextColor()+0xFF000000, button->setColor(DyeColor.BLUE.getColorValue())));
        addButton(new ModWidget(relX + 125, relY+ 20,20,20, DyeColor.PURPLE.getTextColor()+0xFF000000, button->setColor(DyeColor.PURPLE.getColorValue())));
        addButton(new ModWidget(relX + 145, relY+ 20,20,20, DyeColor.CYAN.getTextColor()+0xFF000000, button->setColor(DyeColor.CYAN.getColorValue())));

        addButton(new ModWidget(relX + 5, relY+ 40,20,20, DyeColor.LIGHT_GRAY.getTextColor()+0xFF000000, button->setColor(DyeColor.LIGHT_GRAY.getColorValue())));
        addButton(new ModWidget(relX + 25, relY+ 40,20,20, DyeColor.GRAY.getTextColor()+0xFF000000, button->setColor(DyeColor.GRAY.getColorValue())));
        addButton(new ModWidget(relX + 45, relY+ 40,20,20, DyeColor.PINK.getTextColor()+0xFF000000, button->setColor(DyeColor.PINK.getColorValue())));
        addButton(new ModWidget(relX + 65, relY+ 40,20,20, DyeColor.LIME.getTextColor()+0xFF000000, button->setColor(DyeColor.LIME.getColorValue())));
        addButton(new ModWidget(relX + 85, relY+ 40,20,20, DyeColor.YELLOW.getTextColor()+0xFF000000, button->setColor(DyeColor.YELLOW.getColorValue())));
        addButton(new ModWidget(relX + 105, relY+ 40,20,20, DyeColor.LIGHT_BLUE.getTextColor()+0xFF000000, button->setColor(DyeColor.LIGHT_BLUE.getColorValue())));
        addButton(new ModWidget(relX + 125, relY+ 40,20,20, DyeColor.MAGENTA.getTextColor()+0xFF000000, button->setColor(DyeColor.MAGENTA.getColorValue())));
        addButton(new ModWidget(relX + 145, relY+ 40,20,20, DyeColor.ORANGE.getTextColor()+0xFF000000, button->setColor(DyeColor.ORANGE.getColorValue())));


        addButton(new ModWidget(relX,relY+3,WIDTH-2,20,new TranslationTextComponent("tinyredstone.gui.tinyblock.msg")))
                .setTextHAlignment(ModWidget.HAlignment.CENTER);



    }

    private void close() {
        minecraft.displayGuiScreen(null);
    }

    private void setColor(int color)
    {
        this.tinyBlockCell.setColor(color);
        ModNetworkHandler.sendToServer(new TinyBlockColorSync(panelTile.getPos(),cellIndex, color));
        this.close();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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


    public static void open(PanelTile panelTile, Integer cellIndex, TinyBlock tinyBlockCell) {
        Minecraft.getInstance().displayGuiScreen(new TinyBlockGUI(panelTile, cellIndex, tinyBlockCell));
    }
}

package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.panelcells.Repeater;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.RepeaterTickSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class RepeaterCellGUI extends Screen {

    private static final int WIDTH = 190;
    private static final int HEIGHT = 100;

    private final PanelTile panelTile;
    private final Integer cellIndex;
    private final Repeater repeaterCell;
    private ModWidget tickCount;

    private final ResourceLocation GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    protected RepeaterCellGUI(PanelTile panelTile, Integer cellIndex, Repeater repeaterCell) {
        super(new TranslationTextComponent("tinyredstone:repeaterGUI"));
        this.panelTile = panelTile;
        this.cellIndex = cellIndex;
        this.repeaterCell = repeaterCell;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        Integer redstoneTicks = repeaterCell.getTicks() / 2;


        Float tSeconds = redstoneTicks.floatValue() / 10f;
        this.tickCount = new ModWidget(relX,relY+38,WIDTH,20, ITextComponent.nullToEmpty(redstoneTicks.toString() + " ticks (" + tSeconds.toString() + " seconds)"))
                .setTextHAlignment(ModWidget.HAlignment.CENTER).setTextVAlignment(ModWidget.VAlignment.MIDDLE);

        addButton(new ModWidget(relX - 1, relY - 1, WIDTH + 2, HEIGHT + 2, 0xAA000000));
        addButton(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EEEEEE));
        addButton(new Button(relX + 55, relY + 48, 80, 20, new TranslationTextComponent("tinyredstone.close"), button -> close()));
        addButton(this.tickCount);

        addButton(new ModWidget(relX, relY + 3, WIDTH - 2, 20, new TranslationTextComponent("tinyredstone.gui.repeater.msg")))
                .setTextHAlignment(ModWidget.HAlignment.CENTER);
        addButton(new Button(relX + 15, relY + 15, 20, 20, ITextComponent.nullToEmpty("---"), button -> changeTicks(-200)));
        addButton(new Button(relX + 40, relY + 15, 20, 20, ITextComponent.nullToEmpty("--"), button -> changeTicks(-20)));
        addButton(new Button(relX + 65, relY + 15, 20, 20, ITextComponent.nullToEmpty("-"), button -> changeTicks(-2)));

        addButton(new Button(relX + 105, relY + 15, 20, 20, ITextComponent.nullToEmpty("+"), button -> changeTicks(2)));
        addButton(new Button(relX + 130, relY + 15, 20, 20, ITextComponent.nullToEmpty("++"), button -> changeTicks(20)));
        addButton(new Button(relX + 155, relY + 15, 20, 20, ITextComponent.nullToEmpty("+++"), button -> changeTicks(200)));

        addButton(new ModWidget(relX, relY + 73, WIDTH - 2, 20, new TranslationTextComponent("tinyredstone.gui.repeater.msg2"), 0xFF000000))
                .setTextHAlignment(ModWidget.HAlignment.CENTER);
        addButton(new ModWidget(relX, relY + 88, WIDTH - 2, 20, new TranslationTextComponent("tinyredstone.gui.repeater.msg3"), 0xFF000000))
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
        this.buttons.remove(this.tickCount);
        Float tSeconds = redstoneTicks.floatValue()/10f;
        this.tickCount = new ModWidget(relX,relY+38,WIDTH,20, ITextComponent.nullToEmpty(redstoneTicks.toString() + " ticks (" + tSeconds.toString() + " seconds)"))
                .setTextHAlignment(ModWidget.HAlignment.CENTER).setTextVAlignment(ModWidget.VAlignment.MIDDLE);
        addButton(this.tickCount);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(GUI);
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, WIDTH, HEIGHT);

        super.render(matrixStack,mouseX, mouseY, partialTicks);
    }


    public static void open(PanelTile panelTile, Integer cellIndex, Repeater repeaterCell) {
        Minecraft.getInstance().setScreen(new RepeaterCellGUI(panelTile, cellIndex, repeaterCell));
    }

}

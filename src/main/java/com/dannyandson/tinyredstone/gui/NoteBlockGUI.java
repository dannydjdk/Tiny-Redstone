package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.NoteBlock;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.NoteBlockInstrumentSync;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class NoteBlockGUI extends Screen {

    private static final int WIDTH = 250;
    private static final int HEIGHT = 130;

    private final PanelTile panelTile;
    private final Integer cellIndex;
    private final NoteBlock tinyNoteBlock;

    private final ResourceLocation GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    protected NoteBlockGUI(PanelTile panelTile, Integer cellIndex, NoteBlock tinyNoteBlock) {
        super(Component.translatable("tinyredstone:tinyNoteBlockGUI"));
        this.panelTile = panelTile;
        this.cellIndex = cellIndex;
        this.tinyNoteBlock = tinyNoteBlock;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;



        addRenderableWidget(new ModWidget(relX-1, relY-1, WIDTH+2, HEIGHT+2, 0xAA000000));
        addRenderableWidget(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EEEEEE));
        addRenderableWidget(ModWidget.buildButton(relX + 85, relY + 105, 80, 20, Component.translatable("tinyredstone.close"), button -> close()));

        addInstrumentButton("bass",relX + 5, relY+ 20);
        addInstrumentButton("snare",relX + 65, relY+ 20);
        addInstrumentButton("hat",relX + 125, relY+ 20);
        addInstrumentButton("basedrum",relX + 185, relY+ 20);

        addInstrumentButton("bell",relX + 5, relY+ 40);
        addInstrumentButton("flute",relX + 65, relY+ 40);
        addInstrumentButton("chime",relX + 125, relY+ 40);
        addInstrumentButton("guitar",relX + 185, relY+ 40);

        addInstrumentButton("xylophone",relX + 5, relY+ 60);
        addInstrumentButton("iron_xylophone",relX + 65, relY+ 60);
        addInstrumentButton("cow_bell",relX + 125, relY+ 60);
        addInstrumentButton("didgeridoo",relX + 185, relY+ 60);

        addInstrumentButton("bit",relX + 5, relY+ 80);
        addInstrumentButton("banjo",relX + 65, relY+ 80);
        addInstrumentButton("pling",relX + 125, relY+ 80);
        addInstrumentButton("harp",relX + 185, relY+ 80);

    }

    private void addInstrumentButton(String instrument, Integer xPos, Integer yPos) {
        addRenderableWidget(Button.builder(Component.translatable("tinyredstone.noteblock." + instrument), button -> setInstrument(instrument))
                .pos(xPos, yPos)
                .size(60, 20)
                .build()
        );
    }

    private void close() {
        minecraft.setScreen(null);
    }

    private void setInstrument(String instrument)
    {
        this.tinyNoteBlock.setInstrument(instrument);
        ModNetworkHandler.sendToServer(new NoteBlockInstrumentSync(panelTile.getBlockPos(),cellIndex, instrument));
        this.close();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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


    public static void open(PanelTile panelTile, Integer cellIndex, NoteBlock tinyNoteBlock) {
        Minecraft.getInstance().setScreen(new NoteBlockGUI(panelTile, cellIndex, tinyNoteBlock));
    }
}

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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class NoteBlockGUI extends Screen {

    private static final int WIDTH = 250;
    private static final int HEIGHT = 130;

    private final PanelTile panelTile;
    private final Integer cellIndex;
    private final NoteBlock tinyNoteBlock;

    private final ResourceLocation GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    protected NoteBlockGUI(PanelTile panelTile, Integer cellIndex, NoteBlock tinyNoteBlock) {
        super(new TranslatableComponent("tinyredstone:tinyNoteBlockGUI"));
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
        addRenderableWidget(new Button(relX + 85, relY + 105, 80, 20, new TranslatableComponent("tinyredstone.close"), button -> close()));

        addRenderableWidget(new Button(relX + 5, relY+ 20,60,20,  new TranslatableComponent("tinyredstone.noteblock.bass"), button-> setInstrument("bass")));
        addRenderableWidget(new Button(relX + 65, relY+ 20,60,20, new TranslatableComponent("tinyredstone.noteblock.snare"), button->setInstrument("snare")));
        addRenderableWidget(new Button(relX + 125, relY+ 20,60,20, new TranslatableComponent("tinyredstone.noteblock.hat"), button->setInstrument("hat")));
        addRenderableWidget(new Button(relX + 185, relY+ 20,60,20, new TranslatableComponent("tinyredstone.noteblock.basedrum"), button->setInstrument("basedrum")));

        addRenderableWidget(new Button(relX + 5,  relY+ 40,60,20, new TranslatableComponent("tinyredstone.noteblock.bell"), button->setInstrument("bell")));
        addRenderableWidget(new Button(relX + 65, relY+ 40,60,20,new TranslatableComponent("tinyredstone.noteblock.flute"), button->setInstrument("flute")));
        addRenderableWidget(new Button(relX + 125, relY+ 40,60,20,new TranslatableComponent("tinyredstone.noteblock.chime"), button->setInstrument("chime")));
        addRenderableWidget(new Button(relX + 185, relY+ 40,60,20,new TranslatableComponent("tinyredstone.noteblock.guitar"), button->setInstrument("guitar")));

        addRenderableWidget(new Button(relX + 5,  relY+ 60,60,20,  new TranslatableComponent("tinyredstone.noteblock.xylophone"),button->setInstrument("xylophone")));
        addRenderableWidget(new Button(relX + 65,  relY+ 60,60,20, new TranslatableComponent("tinyredstone.noteblock.iron_xylophone"),button->setInstrument("iron_xylophone")));
        addRenderableWidget(new Button(relX + 125, relY+ 60,60,20, new TranslatableComponent("tinyredstone.noteblock.cow_bell"),button->setInstrument("cow_bell")));
        addRenderableWidget(new Button(relX + 185, relY+ 60,60,20, new TranslatableComponent("tinyredstone.noteblock.didgeridoo"),button->setInstrument("didgeridoo")));

        addRenderableWidget(new Button(relX + 5,  relY+ 80,60,20, new TranslatableComponent("tinyredstone.noteblock.bit"),button->setInstrument("bit")));
        addRenderableWidget(new Button(relX + 65,  relY+ 80,60,20,new TranslatableComponent("tinyredstone.noteblock.banjo"),button->setInstrument("banjo")));
        addRenderableWidget(new Button(relX + 125, relY+ 80,60,20,new TranslatableComponent("tinyredstone.noteblock.pling"),button->setInstrument("pling")));
        addRenderableWidget(new Button(relX + 185, relY+ 80,60,20,new TranslatableComponent("tinyredstone.noteblock.harp"),button->setInstrument("harp")));

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

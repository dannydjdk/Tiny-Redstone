package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.NoteBlock;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.NoteBlockInstrumentSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class NoteBlockGUI extends Screen {

    private static final int WIDTH = 250;
    private static final int HEIGHT = 130;

    private final PanelTile panelTile;
    private final Integer cellIndex;
    private final NoteBlock tinyNoteBlock;

    private final ResourceLocation GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    protected NoteBlockGUI(PanelTile panelTile, Integer cellIndex, NoteBlock tinyNoteBlock) {
        super(new TranslationTextComponent("tinyredstone:tinyNoteBlockGUI"));
        this.panelTile = panelTile;
        this.cellIndex = cellIndex;
        this.tinyNoteBlock = tinyNoteBlock;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;



        addButton(new ModWidget(relX-1, relY-1, WIDTH+2, HEIGHT+2, 0xAA000000));
        addButton(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EEEEEE));
        addButton(new Button(relX + 85, relY + 105, 80, 20, new TranslationTextComponent("tinyredstone.close"), button -> close()));

        addButton(new Button(relX + 5, relY+ 20,60,20,  new TranslationTextComponent("tinyredstone.noteblock.bass"), button-> setInstrument("bass")));
        addButton(new Button(relX + 65, relY+ 20,60,20, new TranslationTextComponent("tinyredstone.noteblock.snare"), button->setInstrument("snare")));
        addButton(new Button(relX + 125, relY+ 20,60,20, new TranslationTextComponent("tinyredstone.noteblock.hat"), button->setInstrument("hat")));
        addButton(new Button(relX + 185, relY+ 20,60,20, new TranslationTextComponent("tinyredstone.noteblock.basedrum"), button->setInstrument("basedrum")));

        addButton(new Button(relX + 5,  relY+ 40,60,20, new TranslationTextComponent("tinyredstone.noteblock.bell"), button->setInstrument("bell")));
        addButton(new Button(relX + 65, relY+ 40,60,20,new TranslationTextComponent("tinyredstone.noteblock.flute"), button->setInstrument("flute")));
        addButton(new Button(relX + 125, relY+ 40,60,20,new TranslationTextComponent("tinyredstone.noteblock.chime"), button->setInstrument("chime")));
        addButton(new Button(relX + 185, relY+ 40,60,20,new TranslationTextComponent("tinyredstone.noteblock.guitar"), button->setInstrument("guitar")));

        addButton(new Button(relX + 5,  relY+ 60,60,20,  new TranslationTextComponent("tinyredstone.noteblock.xylophone"),button->setInstrument("xylophone")));
        addButton(new Button(relX + 65,  relY+ 60,60,20, new TranslationTextComponent("tinyredstone.noteblock.iron_xylophone"),button->setInstrument("iron_xylophone")));
        addButton(new Button(relX + 125, relY+ 60,60,20, new TranslationTextComponent("tinyredstone.noteblock.cow_bell"),button->setInstrument("cow_bell")));
        addButton(new Button(relX + 185, relY+ 60,60,20, new TranslationTextComponent("tinyredstone.noteblock.didgeridoo"),button->setInstrument("didgeridoo")));

        addButton(new Button(relX + 5,  relY+ 80,60,20, new TranslationTextComponent("tinyredstone.noteblock.bit"),button->setInstrument("bit")));
        addButton(new Button(relX + 65,  relY+ 80,60,20,new TranslationTextComponent("tinyredstone.noteblock.banjo"),button->setInstrument("banjo")));
        addButton(new Button(relX + 125, relY+ 80,60,20,new TranslationTextComponent("tinyredstone.noteblock.pling"),button->setInstrument("pling")));
        addButton(new Button(relX + 185, relY+ 80,60,20,new TranslationTextComponent("tinyredstone.noteblock.harp"),button->setInstrument("harp")));

    }

    private void close() {
        minecraft.displayGuiScreen(null);
    }

    private void setInstrument(String instrument)
    {
        this.tinyNoteBlock.setInstrument(instrument);
        ModNetworkHandler.sendToServer(new NoteBlockInstrumentSync(panelTile.getPos(),cellIndex, instrument));
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


    public static void open(PanelTile panelTile, Integer cellIndex, NoteBlock tinyNoteBlock) {
        Minecraft.getInstance().displayGuiScreen(new NoteBlockGUI(panelTile, cellIndex, tinyNoteBlock));
    }
}

package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.items.Blueprint;
import com.dannyandson.tinyredstone.network.BlueprintSync;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class BlueprintGUI  extends Screen {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 90;

    private final Identifier GUI = Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "textures/gui/transparent.png");

    private final ItemStack blueprint;
    private boolean dialogOpen = false;
    private Button button;

    protected BlueprintGUI(ItemStack blueprint) {
        super(Component.translatable("tinyredstone.gui.blueprint.msg"));
        this.blueprint=blueprint;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        addRenderableOnly(new ModWidget(relX-1, relY-1, WIDTH+2, HEIGHT+2, 0xAA000000));
        addRenderableOnly(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EEEEEE));
        addRenderableWidget(ModWidget.buildButton(relX + 20, relY + 50, 80, 20, Component.translatable("tinyredstone.close"), button -> close()));

        if (ItemStackHelper.getCustomTag(this.blueprint) != null)
            button=ModWidget.buildButton(relX + 20, relY + 20, 80, 20, Component.translatable("tinyredstone.export"), button -> exportToFile());
        else
            button=ModWidget.buildButton(relX + 20, relY + 20, 80, 20, Component.translatable("tinyredstone.import"), button -> importFromFile());

        addRenderableWidget(button);



    }

    private void close() {
        minecraft.gui.setScreen(null);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, WIDTH, HEIGHT, 256, 256);

        super.extractRenderState(guiGraphics,mouseX, mouseY, partialTicks);
    }


    public static void open(ItemStack blueprint) {
        Minecraft.getInstance().gui.setScreen(new BlueprintGUI(blueprint));
    }

    public void exportToFile()
    {
        if (!dialogOpen) {
            this.dialogOpen=true;
            this.renderables.remove(button);
            this.renderables.add(new ModWidget((this.width - WIDTH) / 2 + 20, (this.height - HEIGHT) / 2 + 20, 80, 20, 0xFF444444));

            new Thread(() -> {

                MemoryStack stack = MemoryStack.stackPush();
                PointerBuffer filters = null;

                String path = TinyFileDialogs.tinyfd_saveFileDialog(
                        Component.translatable("tinyredstone.save_file").getString(),
                        "blueprint.json", filters, null
                );
                this.dialogOpen=false;
                stack.pop();

                if (path != null) {
                    try {
                        File file = new File(path);
                        if (file.createNewFile()) {
                            FileWriter writer = new FileWriter(path);
                            writer.write(ItemStackHelper.getCustomTag(blueprint).toString());
                            writer.close();
                        }
                    } catch (IOException e) {
                        TinyRedstone.LOGGER.error("IOException attempting to save blueprint json to file: " + e.getLocalizedMessage());
                    }
                }

            }).start();
        }
    }

    public void importFromFile() {
        if (!dialogOpen) {
            this.dialogOpen = true;

            this.renderables.remove(button);
            this.renderables.add(new ModWidget((this.width - WIDTH) / 2 + 20, (this.height - HEIGHT) / 2 + 20, 80, 20, 0xFF444444));

            new Thread(() -> {

                MemoryStack stack = MemoryStack.stackPush();
                PointerBuffer filters = null;

                String path = TinyFileDialogs.tinyfd_openFileDialog(
                        Component.translatable("tinyredstone.choose_file").getString(),
                        null, filters, "JSON File (*.json)", false
                );
                this.dialogOpen=false;

                stack.pop();

                if (path != null) {
                    StringBuilder data = new StringBuilder();
                    try {
                        File file = new File(path);
                        Scanner scanner = new Scanner(file);
                        while (scanner.hasNextLine()) {
                            data.append(scanner.nextLine());
                        }
                        scanner.close();
                    } catch (FileNotFoundException e) {
                        TinyRedstone.LOGGER.error("FileNotFoundException attempting to load blueprint json to file: " + e.getLocalizedMessage());
                    }

                    try {
                        //will throw CommandSyntaxException, abort and log error if file is not valid NBT json
                        CompoundTag nbt = TagParser.parseCompoundFully(data.toString());
                        CompoundTag cleanNBT = Blueprint.cleanUpBlueprintNBT(nbt);
                        if (cleanNBT!=null) {
                            ItemStackHelper.setCustomTag(this.blueprint, cleanNBT);
                            ModNetworkHandler.sendToServer(new BlueprintSync(cleanNBT));
                        }
                    } catch (CommandSyntaxException e) {
                        TinyRedstone.LOGGER.error("Exception reading JSON from user file: ",e);
                        try {
                            ItemStackHelper.setCustomTag(this.blueprint, TagParser.parseCompoundFully(""));
                            ModNetworkHandler.sendToServer(new BlueprintSync(TagParser.parseCompoundFully("")));
                        } catch(CommandSyntaxException e2){
                            TinyRedstone.LOGGER.error("Exception rolling back faulty JSON: ",e);

                        }
                    }
                }

            }).start();
        }

    }

}
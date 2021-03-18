package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.network.BlueprintSync;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
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

    private final ResourceLocation GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/transparent.png");

    private final ItemStack blueprint;
    private boolean dialogOpen = false;
    private Widget button;

    protected BlueprintGUI(ItemStack blueprint) {
        super(new TranslationTextComponent("tinyredstone.gui.blueprint.msg"));
        this.blueprint=blueprint;
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        addButton(new ModWidget(relX-1, relY-1, WIDTH+2, HEIGHT+2, 0xAA000000));
        addButton(new ModWidget(relX, relY, WIDTH, HEIGHT, 0x88EEEEEE));
        addButton(new Button(relX + 20, relY + 50, 80, 20, new TranslationTextComponent("tinyredstone.cancel"), button -> close()));

        if (this.blueprint.hasTag())
            button=new Button(relX + 20, relY + 20, 80, 20, new TranslationTextComponent("tinyredstone.export"), button -> exportToFile());
        else
            button=new Button(relX + 20, relY + 20, 80, 20, new TranslationTextComponent("tinyredstone.import"), button -> importFromFile());

        addButton(button);



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


    public static void open(ItemStack blueprint) {
        Minecraft.getInstance().displayGuiScreen(new BlueprintGUI(blueprint));
    }

    public void exportToFile()
    {
        if (!dialogOpen) {
            this.dialogOpen=true;
            this.buttons.remove(button);
            this.buttons.add(new ModWidget((this.width - WIDTH) / 2 + 20, (this.height - HEIGHT) / 2 + 20, 80, 20, 0xFF444444));

            MemoryStack stack = MemoryStack.stackPush();
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.json"));

            new Thread(() -> {
                String path = TinyFileDialogs.tinyfd_saveFileDialog(
                        new TranslationTextComponent("tinyredstone.save_file").getString(),
                        null, filters, null
                );
                close();
                this.dialogOpen=false;

                if (path != null) {
                    try {
                        File file = new File(path);
                        if (file.createNewFile()) {
                            FileWriter writer = new FileWriter(path);
                            writer.write(blueprint.getTag().toString());
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

            this.buttons.remove(button);
            this.buttons.add(new ModWidget((this.width - WIDTH) / 2 + 20, (this.height - HEIGHT) / 2 + 20, 80, 20, 0xFF444444));

            MemoryStack stack = MemoryStack.stackPush();
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.json"));

            new Thread(() -> {
                String path = TinyFileDialogs.tinyfd_openFileDialog(
                        new TranslationTextComponent("tinyredstone.choose_file").getString(),
                        null, filters, null, false
                );
                close();
                this.dialogOpen=false;

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
                        //TODO validate that NBT received from JSON file is a valid blueprint
                        CompoundNBT nbt = JsonToNBT.getTagFromJson(data.toString());
                        this.blueprint.setTag(nbt);
                        ModNetworkHandler.sendToServer(new BlueprintSync(nbt));
                    } catch (CommandSyntaxException e) {
                        TinyRedstone.LOGGER.error("CommandSyntaxException reading JSON from user file: " + e.getLocalizedMessage());
                    }
                }

            }).start();
        }

    }

}

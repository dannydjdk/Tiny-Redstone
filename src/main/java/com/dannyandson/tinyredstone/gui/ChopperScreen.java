package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.BlockHitResult;

public class ChopperScreen extends AbstractContainerScreen<ChopperMenu> implements MenuAccess<ChopperMenu> {

    public static final Identifier CUTTER_GUI = Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "textures/gui/block_chopper.png");
    private ChopperMenu chopperMenu;
    private Button itemTypeButton = null;

    public ChopperScreen(ChopperMenu chopperMenu, Inventory playerInventory, Component title) {
        // 26.1: imageWidth and imageHeight are now final, set via constructor params
        super(chopperMenu, playerInventory, title, 184, 184);
        this.chopperMenu = chopperMenu;
    }

    @Override
    protected void init() {
        super.init();
        itemTypeButton = ModWidget.buildButton(leftPos+(imageWidth/2)-35, topPos+18, 70, 20, Component.nullToEmpty(chopperMenu.getItemType()), button -> toggleItemType());
        addRenderableWidget(itemTypeButton);
    }

    private void toggleItemType(){
        if (this.minecraft.hitResult instanceof BlockHitResult) {
            chopperMenu.toggleItemType(new BlockPos(((BlockHitResult) this.minecraft.hitResult).getBlockPos()));
            removeWidget(itemTypeButton);
            itemTypeButton = ModWidget.buildButton(leftPos + (imageWidth / 2) - 35, topPos + 18, 70, 20, Component.nullToEmpty(chopperMenu.getItemType()), button -> toggleItemType());
            addRenderableWidget(itemTypeButton);
        }
    }

    // 26.1: AbstractContainerScreen#render now calls renderTooltip automatically.
    // No need to override extractRenderState.


    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // 26.1: blit uses RenderPipelines.GUI_TEXTURED instead of RenderType::guiTextured
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CUTTER_GUI, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}

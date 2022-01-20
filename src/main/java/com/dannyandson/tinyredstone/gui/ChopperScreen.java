package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.BlockHitResult;

public class ChopperScreen extends AbstractContainerScreen<ChopperMenu> implements MenuAccess<ChopperMenu> {

    public static final ResourceLocation CUTTER_GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/block_chopper.png");
    private ChopperMenu chopperMenu;
    private Button itemTypeButton = null;

    public ChopperScreen(ChopperMenu chopperMenu, Inventory playerInventory, Component title) {
        super(chopperMenu, playerInventory, title);

        this.chopperMenu = chopperMenu;
        this.imageWidth = 184;
        this.imageHeight = 184;
    }

    @Override
    protected void init() {
        super.init();
        itemTypeButton=new Button(leftPos+(imageWidth/2)-35,topPos+18,70,20,Component.nullToEmpty(chopperMenu.getItemType()),button->toggleItemType());
        addRenderableWidget(itemTypeButton);
    }

    private void toggleItemType(){
        if (this.minecraft.hitResult instanceof BlockHitResult) {

            chopperMenu.toggleItemType(new BlockPos(((BlockHitResult) this.minecraft.hitResult).getBlockPos()));

            removeWidget(itemTypeButton);
            itemTypeButton = new Button(leftPos + (imageWidth / 2) - 35, topPos + 18, 70, 20, Component.nullToEmpty(chopperMenu.getItemType()), button -> toggleItemType());
            addRenderableWidget(itemTypeButton);
        }
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CUTTER_GUI);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

    }

}

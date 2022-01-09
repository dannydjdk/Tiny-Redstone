package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;

public class ChopperScreen extends ContainerScreen<ChopperMenu> {

    public static final ResourceLocation CUTTER_GUI = new ResourceLocation(TinyRedstone.MODID, "textures/gui/block_chopper.png");
    private ChopperMenu chopperMenu;
    private Button itemTypeButton = null;

    public ChopperScreen(ChopperMenu chopperMenu, PlayerInventory playerInventory, ITextComponent title) {
        super(chopperMenu, playerInventory, title);

        this.chopperMenu = chopperMenu;
        this.imageWidth = 184;
        this.imageHeight = 184;
    }

    @Override
    protected void init() {
        super.init();
        itemTypeButton=new Button(leftPos+(imageWidth/2)-35,topPos+18,70,20,ITextComponent.nullToEmpty(chopperMenu.getItemType()),button->toggleItemType());
        addButton(itemTypeButton);
    }

    private void toggleItemType(){
        if (this.minecraft.hitResult instanceof BlockRayTraceResult) {

            chopperMenu.toggleItemType(new BlockPos(((BlockRayTraceResult) this.minecraft.hitResult).getBlockPos()));

            buttons.remove(itemTypeButton);
            itemTypeButton = new Button(leftPos + (imageWidth / 2) - 35, topPos + 18, 70, 20, ITextComponent.nullToEmpty(chopperMenu.getItemType()), button -> toggleItemType());
            addButton(itemTypeButton);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(CUTTER_GUI);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

}

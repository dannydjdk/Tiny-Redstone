package com.dannyandson.tinyredstone.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModWidget extends AbstractWidget {

    public enum HAlignment {
        LEFT, CENTER, RIGHT
    }
    public enum VAlignment {
        TOP, MIDDLE, BOTTOM
    }

    private HAlignment halignment = HAlignment.LEFT;
    private VAlignment valignment = VAlignment.TOP;
    private float scale = 1.0f;
    private int color;
    private int bgcolor=-1;
    private int textWidth;
    private int textHeight;
    private Component toolTipTextComponent;
    private ModWidget.IPressable pressedAction=null;

    public ModWidget(int x, int y, int width, int height, Component title, int textColor, int bgColor)
    {
        super(x, y, width, height, title);
        this.color=textColor;
        this.bgcolor=bgColor;
        if (title.getString().length()>0) {
            this.textWidth = Minecraft.getInstance().font.width(getMessage());
            this.textHeight = Minecraft.getInstance().font.lineHeight;
        }
    }

    public ModWidget(int x, int y, int width, int height, Component title, int textColor)
    {
        this(x,y,width,height,title,textColor,-1);

    }
    public ModWidget(int x, int y, int width, int height, Component title)
    {
        this(x,y,width,height,title,0xFFFFFFFF,-1);

    }
    public ModWidget(int x, int y, int width, int height, int bgColor)
    {
        this(x,y,width,height,Component.nullToEmpty(""),0xFFFFFFFF,bgColor);

    }
    public ModWidget(int x, int y, int width, int height, int bgColor, ModWidget.IPressable pressedAction)
    {
        this(x,y,width,height,Component.nullToEmpty(""),0xFFFFFFFF,bgColor);
        this.pressedAction=pressedAction;
    }


    public ModWidget setTextHAlignment(HAlignment alignment) {
        this.halignment = alignment;
        return this;
    }
    public ModWidget setTextVAlignment(VAlignment alignment) {
        this.valignment = alignment;
        return this;
    }

    public ModWidget setToolTip(Component textComponent)
    {
        this.toolTipTextComponent = textComponent;
        return this;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        if (pressedAction==null ||
                mouseX<this.getX() || mouseX>this.getX()+this.width ||
                mouseY<this.getY() || mouseY>this.getY()+this.height
        )
            return false;

        pressedAction.onPress(this);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            int drawX,drawY;
            Font fr = Minecraft.getInstance().font;


            switch (halignment) {
                case LEFT:
                default:
                    drawX = getX();
                    break;
                case CENTER:
                    drawX = getX() + (int)((width-textWidth) / 2 * scale);
                    break;
                case RIGHT:
                    drawX = getX() + (int)((width-textWidth) * scale);
                    break;
            }
            switch (valignment) {
                case TOP:
                default:
                    drawY = getY();
                    break;
                case MIDDLE:
                    drawY = getY() + (int)((height-textHeight) / 2 * scale);
                    break;
                case BOTTOM:
                    drawY = getY() + (int)((height-textHeight) * scale);
                    break;
            }


            PoseStack matrixStack = guiGraphics.pose();
            if (scale != 1.0f) {
                matrixStack.pushPose();
                matrixStack.scale(scale, scale, scale);
                matrixStack.translate(drawX, getY(), 0);
                guiGraphics.drawString(fr, getMessage().getVisualOrderText(), drawX, getY(), this.color);
                matrixStack.popPose();
            } else {
                guiGraphics.drawString(fr, getMessage().getVisualOrderText(), drawX, getY(), this.color);
            }

            if (bgcolor!=-1)
            {
                guiGraphics.fill(getX(),getY(),getX()+width,getY()+height,bgcolor);
            }

            if (this.toolTipTextComponent!=null && mouseX>=getX() && mouseX<=getX()+width && mouseY>=getY() && mouseY<=getY()+height)
                this.renderHoverToolTip(guiGraphics,mouseX,mouseY);
        }
    }


    public void renderHoverToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.toolTipTextComponent != null) {
            Font fr = Minecraft.getInstance().font;
            int width = fr.width(this.toolTipTextComponent);
            int height = fr.lineHeight;

            guiGraphics.fill( mouseX, mouseY+10, mouseX + width + 4, mouseY +10 + height + 4, 0xCC000000);
            guiGraphics.fill( mouseX + 1, mouseY + 11, mouseX + width + 3, mouseY + 10 + height + 3, 0x66EEEEEE);
            guiGraphics.drawString(fr, this.toolTipTextComponent.getVisualOrderText(), mouseX + 3, mouseY + 13, 0xFFFEFEFE);
        }
    }

    public static Button buildButton(Integer xPos, Integer yPos, Integer width, Integer height, Component component, Button.OnPress onPress){
        return Button.builder(component,onPress)
                .pos(xPos, yPos)
                .size(width, height)
                .build();
    }

    @OnlyIn(Dist.CLIENT)
    public interface IPressable {
        void onPress(ModWidget modWidget);
    }

}

package com.dannyandson.tinyredstone.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModWidget extends Widget {


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
    private ITextComponent toolTipTextComponent;
    private ModWidget.IPressable pressedAction=null;

    public ModWidget(int x, int y, int width, int height, ITextComponent title, int textColor, int bgColor)
    {
        super(x, y, width, height, title);
        this.color=textColor;
        this.bgcolor=bgColor;
        if (title.getString().length()>0) {
            this.textWidth = Minecraft.getInstance().font.width(getMessage());
            this.textHeight = Minecraft.getInstance().font.lineHeight;
        }
    }

    public ModWidget(int x, int y, int width, int height, ITextComponent title, int textColor)
    {
        this(x,y,width,height,title,textColor,-1);

    }
    public ModWidget(int x, int y, int width, int height, ITextComponent title)
    {
        this(x,y,width,height,title,0xFFFFFFFF,-1);

    }
    public ModWidget(int x, int y, int width, int height, int bgColor)
    {
        this(x,y,width,height,ITextComponent.nullToEmpty(""),0xFFFFFFFF,bgColor);

    }
    public ModWidget(int x, int y, int width, int height, int bgColor, ModWidget.IPressable pressedAction)
    {
        this(x,y,width,height,ITextComponent.nullToEmpty(""),0xFFFFFFFF,bgColor);
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

    public ModWidget setToolTip(ITextComponent textComponent)
    {
        this.toolTipTextComponent = textComponent;
        return this;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        if (pressedAction==null ||
                mouseX<this.x || mouseX>this.x+this.width ||
                mouseY<this.y || mouseY>this.y+this.height
        )
            return false;

        pressedAction.onPress(this);
        return true;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            int drawX,drawY;
            FontRenderer fr = Minecraft.getInstance().font;


            switch (halignment) {
                case LEFT:
                default:
                    drawX = x;
                    break;
                case CENTER:
                    drawX = x + (int)((width-textWidth) / 2 * scale);
                    break;
                case RIGHT:
                    drawX = x + (int)((width-textWidth) * scale);
                    break;
            }
            switch (valignment) {
                case TOP:
                default:
                    drawY = y;
                    break;
                case MIDDLE:
                    drawY = y + (int)((height-textHeight) / 2 * scale);
                    break;
                case BOTTOM:
                    drawY = y + (int)((height-textHeight) * scale);
                    break;
            }


            if (scale != 1.0f) {
                matrixStack.pushPose();
                matrixStack.scale(scale, scale, scale);
                matrixStack.translate(drawX, y, 0);
                fr.draw(matrixStack, getMessage().getVisualOrderText(), drawX, y, this.color);
                matrixStack.popPose();
            } else {
                fr.draw(matrixStack, getMessage().getVisualOrderText(), drawX, y, this.color);
            }

            if (bgcolor!=-1)
            {
                fill(matrixStack,x,y,x+width,y+height,bgcolor);
            }

            if (this.toolTipTextComponent!=null && mouseX>=x && mouseX<=x+width && mouseY>=y && mouseY<=y+height)
                this.renderHoverToolTip(matrixStack,mouseX,mouseY);
        }
    }


    public void renderHoverToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (this.toolTipTextComponent != null) {
            FontRenderer fr = Minecraft.getInstance().font;
            int width = fr.width(this.toolTipTextComponent);
            int height = fr.lineHeight;

            fill(matrixStack, mouseX, mouseY+10, mouseX + width + 4, mouseY +10 + height + 4, 0xCC000000);
            fill(matrixStack, mouseX + 1, mouseY + 11, mouseX + width + 3, mouseY + 10 + height + 3, 0x66EEEEEE);
            fr.draw(matrixStack, this.toolTipTextComponent.getVisualOrderText(), (float) (mouseX + 3.0), (float) (mouseY + 13.0), 0xFFFEFEFE);
        }
    }


    @OnlyIn(Dist.CLIENT)
    public interface IPressable {
        void onPress(ModWidget modWidget);
    }

}

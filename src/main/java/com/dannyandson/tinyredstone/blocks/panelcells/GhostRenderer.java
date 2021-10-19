package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelCellSegment;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class GhostRenderer implements IPanelCell {

    PanelCellSegment segmentHovering;
    //pre-calculated variables for segment points
    private static final float s6 = 0.375f;
    private static final float s10 = 0.625f;
    private static final int color = ColorHelper.PackedColor.color(255,80,0,0);
    public static ResourceLocation TEXTURE_REDSTONE_DUST_SEGMENT_GHOST = new ResourceLocation(TinyRedstone.MODID,"block/panel_redstone_segment_ghost");
    private static TextureAtlasSprite sprite;

    public GhostRenderer(PanelCellSegment segmentHovering){
        this.segmentHovering=segmentHovering;
    }

    @Override
    public void render(MatrixStack poseStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {
        if (sprite==null){
            sprite=RenderHelper.getSprite(TEXTURE_REDSTONE_DUST_SEGMENT_GHOST);
        }
        alpha = .75f;
        //color = RenderHelper.getColor(255,80,0,0);

        IVertexBuilder builder = buffer.getBuffer(RenderType.translucent());

        poseStack.translate(0,0,0.015);

        if (segmentHovering==PanelCellSegment.RIGHT || segmentHovering==PanelCellSegment.CENTER) {
            RenderHelper.drawRectangle(builder,poseStack,s10,1.01f,0,1,sprite,combinedLight,color,alpha);
        }
        if (segmentHovering==PanelCellSegment.LEFT || segmentHovering==PanelCellSegment.CENTER) {
            RenderHelper.drawRectangle(builder,poseStack,-.01f,s6,0,1,sprite,combinedLight,color,alpha);
        }
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(90));
        poseStack.translate(0,-1,0);
        if (segmentHovering==PanelCellSegment.FRONT || segmentHovering==PanelCellSegment.CENTER) {
            RenderHelper.drawRectangle(builder,poseStack,s10,1.01f,0,1,sprite,combinedLight,color,alpha);
        }
        if (segmentHovering==PanelCellSegment.BACK || segmentHovering==PanelCellSegment.CENTER) {
            RenderHelper.drawRectangle(builder,poseStack,-.01f,s6,0,1,sprite,combinedLight,color,alpha);
        }
    }

    @Override
    public boolean neighborChanged(PanelCellPos cellPos) {
        return false;
    }

    @Override
    public int getWeakRsOutput(Side outputDirection) {
        return 0;
    }

    @Override
    public int getStrongRsOutput(Side outputDirection) {
        return 0;
    }

    @Override
    public CompoundNBT writeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
    }
}

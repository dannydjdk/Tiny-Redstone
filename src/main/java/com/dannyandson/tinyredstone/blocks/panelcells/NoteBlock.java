package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelCellSegment;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3f;

public class NoteBlock extends TinyBlock{

    public static ResourceLocation TEXTURE_TINY_NOTE_BLOCK = new ResourceLocation("minecraft","block/note_block");

    private int pitch = 0;

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {
        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)? RenderType.getSolid():RenderType.getTranslucent());
        TextureAtlasSprite sprite = RenderHelper.getSprite(TEXTURE_TINY_NOTE_BLOCK);



        matrixStack.translate(0,0,1.0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

    }


    @Override
    public boolean neighborChanged(PanelCellPos cellPos){
        int strength = this.weakSignalStrength;
        boolean r = super.neighborChanged(cellPos);
        if (strength==0 && this.weakSignalStrength>0) {
            playNote(cellPos.getPanelTile());
        }
        return r;
    }

    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked){
        pitch++;
        if (pitch>24)pitch=0;
        playNote(cellPos.getPanelTile());
        return false;
    }

    @Override
    public boolean hasActivation(){return true;}

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = super.writeNBT();
        nbt.putInt("pitch",this.pitch);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        super.readNBT(compoundNBT);
        this.pitch= compoundNBT.getInt("pitch");
    }

    private void playNote(PanelTile panelTile)
    {
        panelTile.getWorld().playSound(
                panelTile.getPos().getX(), panelTile.getPos().getY(), panelTile.getPos().getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_HARP,
                SoundCategory.BLOCKS, 0.25f, (pitch==0)?0.5f:(float)Math.pow(2f,((pitch-12f)/12f)), false
        );
    }


}

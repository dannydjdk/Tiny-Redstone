package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.compat.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.gui.NoteBlockGUI;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.PlaySound;
import com.dannyandson.tinyredstone.setup.Registration;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TranslationTextComponent;

public class NoteBlock extends TinyBlock implements IPanelCellInfoProvider {

    public static ResourceLocation TEXTURE_TINY_NOTE_BLOCK = new ResourceLocation("minecraft","block/note_block");
    private static final String[] noteNames = {"F#","G","G#","A","A#","B","C","C#","D","D#","E","F"};

    private boolean powered = false;
    private int pitch = 0;
    private String instrument = "harp";

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

        PanelCellNeighbor rightNeighbor = cellPos.getNeighbor(Side.RIGHT),
                leftNeighbor = cellPos.getNeighbor(Side.LEFT),
                backNeighbor = cellPos.getNeighbor(Side.BACK),
                frontNeighbor = cellPos.getNeighbor(Side.FRONT),
                topNeighbor = cellPos.getNeighbor(Side.TOP),
                bottomNeighbor = cellPos.getNeighbor(Side.BOTTOM);

        boolean r = super.neighborChanged(cellPos);

        if (
                (weakSignalStrength + strongSignalStrength > 0) ||
                        ((
                                ((frontNeighbor != null) ? frontNeighbor.getWeakRsOutput() : 0) +
                                        ((rightNeighbor != null) ? rightNeighbor.getWeakRsOutput() : 0) +
                                        ((backNeighbor != null) ? backNeighbor.getWeakRsOutput() : 0) +
                                        ((leftNeighbor != null) ? leftNeighbor.getWeakRsOutput() : 0)+
                                        ((topNeighbor != null) ? topNeighbor.getWeakRsOutput() : 0)+
                                        ((bottomNeighbor != null) ? bottomNeighbor.getWeakRsOutput() : 0)) > 0)
        ) {
            if (!this.powered) {
                this.powered = true;
                playNote(cellPos.getPanelTile());
                return true;
            }
        } else if (this.powered) {
            this.powered = false;
            return true;
        }

        return r;
    }

    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, PlayerEntity player){
        if (player.getHeldItemMainhand().getItem() == Registration.REDSTONE_WRENCH.get()) {
            if (cellPos.getPanelTile().getWorld().isRemote)
                NoteBlockGUI.open(cellPos.getPanelTile(), cellPos.getIndex(), this);
        }else {
            pitch++;
            if (pitch > 24) pitch = 0;
            playNote(cellPos.getPanelTile());
        }
        return false;
    }

    @Override
    public boolean hasActivation(){return true;}

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = super.writeNBT();
        nbt.putInt("pitch",this.pitch);
        nbt.putString("instrument",this.instrument);
        nbt.putBoolean("powered",this.powered);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        super.readNBT(compoundNBT);
        this.pitch= compoundNBT.getInt("pitch");
        this.instrument= compoundNBT.getString("instrument");
        this.powered =compoundNBT.getBoolean("powered");
    }

    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos) {
        overlayBlockInfo.addText("Instrument", new TranslationTextComponent("tinyredstone.noteblock." + this.instrument).getString() );
        overlayBlockInfo.addText("Note", this.pitch + " (" + noteNames[this.pitch%12] + ")");
    }

    private void playNote(PanelTile panelTile)
    {
        if (!panelTile.getWorld().isRemote)
        {
            BlockPos pos = panelTile.getPos();
            for(PlayerEntity player:panelTile.getWorld().getPlayers()){
                if(panelTile.getWorld().isPlayerWithin(pos.getX(),pos.getY(),pos.getZ(),48))
                    ModNetworkHandler.sendToClient(
                            new PlaySound(pos,"minecraft", "block.note_block." + instrument, 0.5f, (pitch==0)?0.5f:(float)Math.pow(2f,((pitch-12f)/12f))),
                            (ServerPlayerEntity) player);
            }
        }
    }

    public void setInstrument(String instrument)
    {
        this.instrument=instrument;
    }
}

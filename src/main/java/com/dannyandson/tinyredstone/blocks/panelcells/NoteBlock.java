package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.gui.NoteBlockGUI;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.PlaySound;
import com.dannyandson.tinyredstone.setup.Registration;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class NoteBlock extends TinyBlock implements IPanelCellInfoProvider {

    public static ResourceLocation TEXTURE_TINY_NOTE_BLOCK = new ResourceLocation("minecraft","block/note_block");
    private static final String[] noteNames = {"F#","G","G#","A","A#","B","C","C#","D","D#","E","F"};

    private boolean powered = false;
    private int pitch = 0;
    private String instrument = "harp";

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, float alpha) {
        VertexConsumer builder = buffer.getBuffer((alpha==1.0)? RenderType.solid():RenderType.translucent());
        TextureAtlasSprite sprite = RenderHelper.getSprite(TEXTURE_TINY_NOTE_BLOCK);



        matrixStack.translate(0,0,1.0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,alpha);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
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
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, Player player){
        if (player.getMainHandItem().getItem() == Registration.REDSTONE_WRENCH.get()) {
            if (cellPos.getPanelTile().getLevel().isClientSide)
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

    /**
     * Can this cell be pushed by a piston?
     *
     * @return true if a piston can push this block
     */
    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag nbt = super.writeNBT();
        nbt.putInt("pitch",this.pitch);
        nbt.putString("instrument",this.instrument);
        nbt.putBoolean("powered",this.powered);
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag compoundNBT) {
        super.readNBT(compoundNBT);
        this.pitch= compoundNBT.getInt("pitch");
        this.instrument= compoundNBT.getString("instrument");
        this.powered =compoundNBT.getBoolean("powered");
    }

    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos) {
        overlayBlockInfo.addText("Instrument", new TranslatableComponent("tinyredstone.noteblock." + this.instrument).getString() );
        overlayBlockInfo.addText("Note", this.pitch + " (" + noteNames[this.pitch%12] + ")");
    }

    private void playNote(PanelTile panelTile)
    {
        if (!panelTile.getLevel().isClientSide)
        {
            BlockPos pos = panelTile.getBlockPos();
            for(Player player:panelTile.getLevel().players()){
                if(panelTile.getLevel().hasNearbyAlivePlayer(pos.getX(),pos.getY(),pos.getZ(),48))
                    ModNetworkHandler.sendToClient(
                            new PlaySound(pos,"minecraft", "block.note_block." + instrument, 0.5f, (pitch==0)?0.5f:(float)Math.pow(2f,((pitch-12f)/12f))),
                            (ServerPlayer) player);
            }
        }
    }

    public void setInstrument(String instrument)
    {
        this.instrument=instrument;
    }
}

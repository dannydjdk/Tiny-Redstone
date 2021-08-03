package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.api.IObservingPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;

public class Observer implements IPanelCell, IObservingPanelCell {

    public static ResourceLocation TEXTURE_OBSERVER_TOP      = new ResourceLocation("minecraft","block/observer_top");
    public static ResourceLocation TEXTURE_OBSERVER_BACK_ON  = new ResourceLocation("minecraft","block/observer_back_on");
    public static ResourceLocation TEXTURE_OBSERVER_BACK     = new ResourceLocation("minecraft","block/observer_back");
    public static ResourceLocation TEXTURE_OBSERVER_FRONT    = new ResourceLocation("minecraft","block/observer_front");
    public static ResourceLocation TEXTURE_OBSERVER_SIDE     = new ResourceLocation("minecraft","block/observer_side");


    boolean output = false;
    private LinkedList<Boolean> queue = new LinkedList<>();
    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     * @param buffer
     */
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, float alpha) {
        VertexConsumer builder = buffer.getBuffer((alpha==1.0)?RenderType.solid():RenderType.translucent());

        TextureAtlasSprite sprite_top = RenderHelper.getSprite(TEXTURE_OBSERVER_TOP);
        TextureAtlasSprite sprite_back;
        if (output)
            sprite_back = RenderHelper.getSprite(TEXTURE_OBSERVER_BACK_ON);
        else
            sprite_back = RenderHelper.getSprite((TEXTURE_OBSERVER_BACK));
        TextureAtlasSprite sprite_front = RenderHelper.getSprite((TEXTURE_OBSERVER_FRONT));
        TextureAtlasSprite sprite_side = RenderHelper.getSprite((TEXTURE_OBSERVER_SIDE));

        matrixStack.translate(0,0,1);
        addRectangle(builder,matrixStack,sprite_top,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90));
        matrixStack.translate(0,0,1);
        addRectangle(builder,matrixStack,sprite_front,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        addRectangle(builder,matrixStack,sprite_side,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        addRectangle(builder,matrixStack,sprite_back,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        addRectangle(builder,matrixStack,sprite_side,combinedLight,alpha);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90));
        matrixStack.translate(-1,0,1);
        addRectangle(builder,matrixStack,sprite_top,combinedLight,alpha);

    }

    private void addRectangle(VertexConsumer builder, PoseStack matrixStack, TextureAtlasSprite sprite,int combinedLight, float alpha)
    {
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,alpha);
    }

    /**
     * Called when neighboring redstone signal output changes.
     * This can be called multiple times in a tick.
     * Passes PanelCellPos object for this cell which can be used to query PanelTile for PanelCellNeighbor objects - objects wrapping another IPanelCell or a BlockState
     * @param cellPos PanelCellPos object for this cell. Can be used to query paneltile about neighbors
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean neighborChanged(PanelCellPos cellPos) {
        return false;
    }

    /**
     * Gets redstone output of the given side of the cell
     *
     * @param outputDirection (1=Front,2=Right,3=Back,4=Left)
     * @return integer 0-15 indicating the strength of redstone signal
     */
    @Override
    public int getWeakRsOutput(Side outputDirection) {
        return (outputDirection== Side.BACK && output)?15:0;
    }

    @Override
    public int getStrongRsOutput(Side outputDirection) {
        return getWeakRsOutput(outputDirection);
    }

    /**
     * Is this a component that does not change state based on neighbors (such as a redstone block, or potentiometer)?
     *
     * @return true if this cell's state is unaffected by neighbors
     */
    @Override
    public boolean isIndependentState() {
        return true;
    }

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
    public boolean canPlaceVertical(){return true;}

    /**
     * Called each each tick.
     *
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick(PanelCellPos cellPos)
    {
        if (queue.size()>0)
        {
            return setOutput(queue.remove());
        }
        if (output)
        {
            output=false;
            return true;
        }

        return false;
    }

    private boolean setOutput(boolean output)
    {
        if (this.output!=output)
        {
            this.output=output;
            return true;
        }
        return false;
    }


    @Override
    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("output",output);

        String queueString = "";
        int i=0;
        for (Object b : queue.toArray())
        {
            queueString += ((Boolean)b)?"1":"0";
        }
        nbt.putString("queue",queueString);

        return nbt;
    }

    @Override
    public void readNBT(CompoundTag compoundNBT) {
        this.output = compoundNBT.getBoolean("output");
        String queueString = compoundNBT.getString("queue");
        for (Byte b : queueString.getBytes())
        {
            queue.add(b==49);
        }
    }

    @Override
    public boolean frontNeighborUpdated() {
        if(queue.isEmpty()) {
            queue.add(false);
            queue.add(true);
            queue.add(true);
        }

        return false;
    }
}

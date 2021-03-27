package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.Config;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellNeighbor;
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
import net.minecraft.util.math.vector.Vector3f;

import java.util.LinkedList;

public class Torch implements IPanelCell
{
    private boolean output = true;
    private int changePending = 0;
    private final LinkedList<Boolean> changeHx = new LinkedList<>();
    private boolean burnout = false;

    public static ResourceLocation TEXTURE_TORCH_ON = new ResourceLocation(TinyRedstone.MODID,"block/redstone_torch");
    public static ResourceLocation TEXTURE_TORCH_OFF = new ResourceLocation(TinyRedstone.MODID,"block/redstone_torch_off");
    public static ResourceLocation TEXTURE_TORCH_TOP_ON = new ResourceLocation(TinyRedstone.MODID,"block/redstone_torch_top");
    public static ResourceLocation TEXTURE_TORCH_TOP_OFF = new ResourceLocation(TinyRedstone.MODID,"block/redstone_torch_top_off");


    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     */
    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {

        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)?RenderType.getSolid():RenderType.getTranslucent());
        TextureAtlasSprite sprite_torch;
        TextureAtlasSprite sprite_torch_top;

        if (this.getWeakRsOutput(PanelCellSide.FRONT)>0) {
            sprite_torch = RenderHelper.getSprite(TEXTURE_TORCH_ON);
            sprite_torch_top = RenderHelper.getSprite(TEXTURE_TORCH_TOP_ON);
        }
        else
        {
            sprite_torch = RenderHelper.getSprite(TEXTURE_TORCH_OFF);
            sprite_torch_top = RenderHelper.getSprite(TEXTURE_TORCH_TOP_OFF);
        }

        float x1 = 0.375f;
        float x2 = 0.625f;
        float y1 = 0;
        float y2 = 1f;

        matrixStack.rotate(Vector3f.XP.rotationDegrees(60));
        matrixStack.translate(0,0.03125f,0);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_torch,(output)?15728880:combinedLight,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_torch,(output)?15728880:combinedLight,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_torch,(output)?15728880:combinedLight,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_torch,(output)?15728880:combinedLight,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(-90));
        matrixStack.translate(0,-x1,y2);
        RenderHelper.drawRectangle(builder,matrixStack,x1,x2,x1,x2,sprite_torch_top,(output)?15728880:combinedLight,alpha);


    }

    /**
     * Called when neighboring redstone signal output changes.
     * This can be called multiple times in a tick.
     * Passes PanelCellNeighbor objects - an object wrapping another IPanelCell or a BlockState
     * @param frontNeighbor object to access info about front neighbor
     * @param rightNeighbor object to access info about right neighbor
     * @param backNeighbor object to access info about back neighbor
     * @param leftNeighbor object to access info about left neighbor
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean neighborChanged(PanelCellNeighbor frontNeighbor, PanelCellNeighbor rightNeighbor, PanelCellNeighbor backNeighbor, PanelCellNeighbor leftNeighbor)
    {
        boolean output = (backNeighbor==null || backNeighbor.getWeakRsOutput() ==0);

        if (burnout && output)
        {
            int changes=0;
            for (Boolean b : changeHx)
            {
                if (b)changes++;
            }
            if (changes<=16) burnout=false;
            this.changePending=2;
        }

        if (!burnout && output!=this.output)
        {
            this.output=output;
            this.changePending=2;
        }
        return false;
    }

    /**
     * Gets redstone output of the given side of the cell
     *
     * @param outputDirection direction from which the output is being read
     * @return integer 0-15 indicating the strength of redstone signal
     */
    @Override
    public int getWeakRsOutput(PanelCellSide outputDirection)
    {
        return getStrongRsOutput(outputDirection);
    }
    @Override
    public int getStrongRsOutput(PanelCellSide outputDirection) {
        if (outputDirection!=PanelCellSide.BACK && !burnout && ((output&&changePending==0)||(!output&&changePending>0)))
            return 15;
        else
            return 0;
    }

    /**
     * Does the power level drop when transmitting between these cells (such as with redstone dust)?
     *
     * @return true if power level should drop, false if not
     */
    @Override
    public boolean powerDrops() {
        return false;
    }

    /**
     * Is this a component that does not change state based on neighbors (such as a redstone block, or potentiometer)?
     *
     * @return true if this cell's state is unaffected by neighbors
     */
    @Override
    public boolean isIndependentState() {
        return false;
    }

    /**
     * Can this cell be pushed by a piston?
     *
     * @return true if a piston can push this block
     */
    @Override
    public boolean isPushable() {
        return false;
    }

    /**
     * If this cell outputs light, return the level here. Otherwise, return 0.
     *
     * @return Light level to output 0-15
     */
    @Override
    public int lightOutput() {
        return (Config.TORCH_LIGHT.get() && this.getWeakRsOutput(PanelCellSide.FRONT)>0)?1:0;
    }

    /**
     * Called at the beginning of each tick if isTicking() returned true on last call.
     *
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick() {
        changeHx.add(changePending==2);
        if (changeHx.size()>60) changeHx.pop();
        int changes = 0;
        for (Boolean b : changeHx)
        {
            if (b)changes++;
        }
        if (changes>16) burnout=true;

        if (changePending == 0)
            return false;
        if (changePending > 1) {
            changePending--;
            return false;
        }
        changePending--;
        return true;
    }

    /**
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param panelTile the activated PanelTile tile entity that contains this cell
     * @param cellIndex The index of the clicked IPanelCell within the panel
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelTile panelTile, Integer cellIndex, PanelCellSegment segmentClicked) {
        return false;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("output",output);
        nbt.putInt("changePending",changePending);
        nbt.putBoolean("burnout",burnout);

        StringBuilder changeHxString = new StringBuilder();
        for (Object b : changeHx.toArray())
        {
            changeHxString.append(((Boolean) b) ? "1" : "0");
        }
        nbt.putString("changeHx", changeHxString.toString());

        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.output = compoundNBT.getBoolean("output");
        this.changePending = compoundNBT.getInt("changePending");
        this.burnout = compoundNBT.getBoolean("burnout");

        String changeHxString = compoundNBT.getString("changeHx");
        for (Byte b : changeHxString.getBytes())
        {
            changeHx.add(b==49);
            if (changeHx.size()>60)changeHx.pop();
        }

    }
}

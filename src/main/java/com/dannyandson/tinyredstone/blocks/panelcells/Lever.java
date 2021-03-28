package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;

public class Lever implements IPanelCell, IPanelCellProbeInfoProvider {

    public static ResourceLocation TEXTURE_LEVER = new ResourceLocation(TinyRedstone.MODID,"block/lever");
    public static ResourceLocation TEXTURE_LEVER_TOP = new ResourceLocation(TinyRedstone.MODID,"block/lever_top");
    public static ResourceLocation TEXTURE_COBBLESTONE = new ResourceLocation("minecraft","block/cobblestone");
    private boolean active = false;
    /**
     * Drawing the cell on the panel
     *
     * @param matrixStack     positioned for this cell
     *                        scaled to 1/8 block size such that length and width of cell are 1.0
     *                        starting point is (0,0,0)
     */
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha) {
        TextureAtlasSprite sprite_cobble = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_COBBLESTONE);
        TextureAtlasSprite sprite_lever = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_LEVER);
        TextureAtlasSprite sprite_lever_top = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_LEVER_TOP);
        IVertexBuilder builder = buffer.getBuffer((alpha==1.0)? RenderType.getSolid():RenderType.getTranslucent());

        matrixStack.push();
        float x1 = 0.3125f, x2 = .6875f, y1 = 0.25f, y2 = 0.75f;
        float w = .375f, d = 0.5f,h=0.1875f;
        //matrixStack.scale(.375f,.375f,1);

        matrixStack.translate(0,0,h);
        drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_cobble,combinedLight,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-h,-y1);
        drawRectangle(builder,matrixStack,x1,x2,0,h,sprite_cobble,combinedLight,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1f-x1);
        drawRectangle(builder,matrixStack,0,d,0,h,sprite_cobble,combinedLight,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,d);
        drawRectangle(builder,matrixStack,0,w,0,h,sprite_cobble,combinedLight,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,w);
        drawRectangle(builder,matrixStack,0,d,0,h,sprite_cobble,combinedLight,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(60));
        matrixStack.translate(0,0.03125f,0);

        matrixStack.pop();

        x1 = 0.4375f;
        x2 = 0.5625f;
        y1 = 0;
        y2 = .625f;

        matrixStack.translate(0,0.40625,h/2f);
        matrixStack.rotate(Vector3f.XP.rotationDegrees((active)?45:135));

        drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_lever,combinedLight,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_lever,combinedLight,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_lever,combinedLight,alpha);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(-x1,0,x2);
        drawRectangle(builder,matrixStack,x1,x2,y1,y2,sprite_lever,combinedLight,alpha);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(-90));
        matrixStack.translate(0,-x1,y2);
        drawRectangle(builder,matrixStack,x1,x2,x1,x2,sprite_lever_top,combinedLight,alpha);


    }

    private void drawRectangle(IVertexBuilder builder, MatrixStack matrixStack, float x1, float x2, float y1, float y2,TextureAtlasSprite sprite, int combinedLight ,float alpha)
    {
        add(builder, matrixStack, x1,y1,0, sprite.getMinU(), sprite.getMinV(), combinedLight,alpha);
        add(builder, matrixStack, x2,y1,0, sprite.getMaxU(), sprite.getMinV(), combinedLight,alpha);
        add(builder, matrixStack, x2,y2,0, sprite.getMaxU(), sprite.getMaxV(), combinedLight,alpha);
        add(builder, matrixStack, x1,y2,0, sprite.getMinU(), sprite.getMaxV(), combinedLight,alpha);
    }

    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v, int combinedLightIn, float alpha) {
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, alpha)
                .tex(u, v)
                .lightmap(combinedLightIn)
                .normal(1, 0, 0)
                .endVertex();
    }
    /**
     * Called when neighboring redstone signal output changes.
     * This can be called multiple times in a tick.
     * Passes PanelCellNeighbor objects - an object wrapping another IPanelCell or a BlockState
     * WARNING! Check for null values!
     *
     * @param frontNeighbor object to access info about front neighbor or NULL if no neighbor exists
     * @param rightNeighbor object to access info about right neighbor or NULL if no neighbor exists
     * @param backNeighbor  object to access info about back neighbor or NULL if no neighbor exists
     * @param leftNeighbor  object to access info about left neighbor or NULL if no neighbor exists
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean neighborChanged(@Nullable PanelCellNeighbor frontNeighbor, @Nullable PanelCellNeighbor rightNeighbor, @Nullable PanelCellNeighbor backNeighbor, @Nullable PanelCellNeighbor leftNeighbor) {
        return false;
    }

    /**
     * Gets redstone output of the given side of the cell
     *
     * @param outputDirection (1=Front,2=Right,3=Back,4=Left)
     * @return integer 0-15 indicating the strength of redstone signal
     */
    @Override
    public int getWeakRsOutput(PanelCellSide outputDirection) {
        return (active)?15:0;
    }

    @Override
    public int getStrongRsOutput(PanelCellSide outputDirection) {
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
        return true;
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
        return 0;
    }

    /**
     * Called each each tick.
     *
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick() {
        return false;
    }

    /**
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param panelTile      the activated PanelTile tile entity that contains this cell
     * @param cellIndex      The index of the clicked IPanelCell within the panel (this IPanelCell)
     * @param segmentClicked Which of nine segment within the cell were clicked. 0 through 8 where 0 is front-right and 8 is back-left;
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelTile panelTile, Integer cellIndex, PanelCellSegment segmentClicked) {
        panelTile.getWorld().playSound(
                panelTile.getPos().getX(), panelTile.getPos().getY(), panelTile.getPos().getZ(),
                SoundEvents.BLOCK_LEVER_CLICK,
                SoundCategory.BLOCKS, 0.25f, 2f, false
        );
        this.active=!this.active;
        return true;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("active",this.active);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        this.active=compoundNBT.getBoolean("active");
    }

    @Override
    public boolean addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PanelTile panelTile, PosInPanelCell pos) {
        probeInfo.horizontal().item(new ItemStack(Items.REDSTONE), probeInfo.defaultItemStyle().width(14).height(14))
                .text(CompoundText.createLabelInfo("State: ", this.active ? "On" : "Off"));
        return true;
    }
}

package com.dannyandson.tinyredstone.api;

import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public interface IPanelCover {

    default void onPlace(PanelTile panelTile, PlayerEntity player){}

    /**
     * Drawing the cover on the panel
     */
    void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, int color);

    /**
     * Does this cover allows light output?
     * @return true if cells can output light, false if not.
     */
    boolean allowsLightOutput();

    /**
     * Get the shape of the cover for defining the hit box
     * @return VoxelShape object defining the shape
     */
    default VoxelShape getShape() {return VoxelShapes.block();}

    default CompoundNBT writeNBT(){return null;}

    default void readNBT(CompoundNBT compoundNBT){}

    /**
     * Gets Compound tag with any NBT data the itemStack for this cover should contain
     * This is used when the item is removed and the itemStack provided to player, also
     * for pick block and info overlays like The One Probe
     * @return CompoundTag with item NBT data
     */
    default CompoundNBT getItemTag(){return null;}

}

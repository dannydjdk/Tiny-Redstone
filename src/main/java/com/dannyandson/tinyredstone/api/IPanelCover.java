package com.dannyandson.tinyredstone.api;

import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface IPanelCover {

    default void onPlace(PanelTile panelTile, Player player){}

    /**
     * Drawing the cover on the panel
     */
    void render(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, int color);

    /**
     * Does this cover allows light output?
     * @return true if cells can output light, false if not.
     */
    boolean allowsLightOutput();

    /**
     * Get the shape of the cover for defining the hit box
     * @return VoxelShape object defining the shape
     */
    default VoxelShape getShape() {return Shapes.block();}

    default CompoundTag writeNBT(){return null;}

    default void readNBT(CompoundTag compoundNBT){}

    /**
     * Gets Compound tag with any NBT data the itemStack for this cover should contain
     * This is used when the item is removed and the itemStack provided to player, also
     * for pick block and info overlays like The One Probe
     * @return CompoundTag with item NBT data
     */
    default CompoundTag getItemTag(){return null;}

}

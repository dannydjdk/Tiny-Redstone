package com.dannyandson.tinyredstone.blocks;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class PanelTileColor  implements BlockColor {

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
        if (world != null) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof PanelTile) {
                PanelTile panelTile = (PanelTile) te;
                return panelTile.getColor();
            }
        }
        return -1;
    }
}

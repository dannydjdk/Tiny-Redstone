package com.dannyandson.tinyredstone.blocks;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 26.1: Block tint source for the panel block.
 * BlockTintSource is at net.minecraft.client.color.block.BlockTintSource.
 * Registered via RegisterColorHandlersEvent.BlockTintSources.
 */
public class PanelTileColor implements BlockTintSource {

    @Override
    public int color(BlockState state) {
        // Fallback color when no world access
        return -1;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof PanelTile panelTile) {
            return panelTile.getColor();
        }
        return -1;
    }
}
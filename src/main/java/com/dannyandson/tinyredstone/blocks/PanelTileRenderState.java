package com.dannyandson.tinyredstone.blocks;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Render state for PanelTile, used with the 1.21.9+ render state system.
 * The extractRenderState() method copies all data needed for rendering
 * from the block entity, and submit() uses this snapshot to render.
 */
public class PanelTileRenderState extends BlockEntityRenderState {
    // Pre-built cached vertex data from CachedPanelRenderer
    public final List<CachedPanelRenderer.CachedVertex> solidVertices = new ArrayList<>();
    public final List<CachedPanelRenderer.CachedVertex> translucentVertices = new ArrayList<>();
    
    // Panel facing direction
    public Direction facing = Direction.DOWN;
    
    // Whether the cache contains camouflage geometry (no rotation needed)
    public boolean isCamouflageCache = false;
    
    // Ghost preview data
    public boolean hasCover = false;
    public PanelCellGhostPos ghostPos = null;
    public boolean hasBase = false;
    
    // Cache rebuild tracking
    public boolean cacheRebuilt = false;
}

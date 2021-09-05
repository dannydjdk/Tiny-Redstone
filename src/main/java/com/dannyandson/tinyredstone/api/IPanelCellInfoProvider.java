package com.dannyandson.tinyredstone.api;

import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.PosInPanelCell;

/**
 * Interface for IPanelCell classes that need to provide custom information to overlay mods
 * such as The One Probe and HWYLA.
 */
public interface IPanelCellInfoProvider {
    void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos);
}

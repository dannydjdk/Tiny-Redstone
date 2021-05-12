package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.compat.IOverlayBlockInfo;

public interface IPanelCellInfoProvider {
    void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell pos);
}

package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.compat.IOverlayBlockInfo;

public interface IPanelCellInfoProvider {
    void addInfo(IOverlayBlockInfo tooltipInfo, PanelTile panelTile, PosInPanelCell pos);
}

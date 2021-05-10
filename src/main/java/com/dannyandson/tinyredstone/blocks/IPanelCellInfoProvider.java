package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.compat.IToolTipInfo;

public interface IPanelCellInfoProvider {
    void addInfo(IToolTipInfo tooltipInfo, PanelTile panelTile, PosInPanelCell pos);
}

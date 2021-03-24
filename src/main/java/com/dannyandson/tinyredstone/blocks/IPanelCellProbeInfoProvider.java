package com.dannyandson.tinyredstone.blocks;

import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

public interface IPanelCellProbeInfoProvider {
    void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PanelTile panelTile, Integer cellIndex, PanelCellSegment segment);
}

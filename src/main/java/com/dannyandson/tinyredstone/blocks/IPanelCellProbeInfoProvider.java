package com.dannyandson.tinyredstone.blocks;

import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

public interface IPanelCellProbeInfoProvider {
    boolean addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PanelTile panelTile, PosInPanelCell pos, PanelCellSegment segment);
}

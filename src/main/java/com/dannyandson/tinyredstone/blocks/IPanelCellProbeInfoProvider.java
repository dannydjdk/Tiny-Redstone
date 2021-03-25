package com.dannyandson.tinyredstone.blocks;

import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.util.Direction;

public interface IPanelCellProbeInfoProvider {
    boolean addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PanelTile panelTile, PanelCellPos pos, Direction sideHit, PanelCellSegment segment);
}

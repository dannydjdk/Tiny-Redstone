package com.dannyandson.tinyredstone.api;

import com.dannyandson.tinyredstone.blocks.PanelCellPos;

public interface IObservingPanelCell {
    boolean frontNeighborUpdated(PanelCellPos cellPos);
}

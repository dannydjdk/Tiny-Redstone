package com.dannyandson.tinyredstone.api;

import com.dannyandson.tinyredstone.blocks.PanelCellPos;

/**
 * Interface for Tiny Observer-like components to receive notice when observing cell changes.
 */
public interface IObservingPanelCell {
    boolean frontNeighborUpdated(PanelCellPos cellPos);
}

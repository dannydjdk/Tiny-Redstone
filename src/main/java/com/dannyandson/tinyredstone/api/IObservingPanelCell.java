package com.dannyandson.tinyredstone.api;

/**
 * Interface for Tiny Observer-like components to receive notice when observing cell changes.
 */
public interface IObservingPanelCell {
    boolean frontNeighborUpdated();
}

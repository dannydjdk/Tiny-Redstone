package com.dannyandson.tinyredstone.blocks;

public enum PanelCellSegment {
    FRONT_RIGHT,
    FRONT,
    FRONT_LEFT,
    RIGHT,
    CENTER,
    LEFT,
    BACK_RIGHT,
    BACK,
    BACK_LEFT;

    /**
     * Converts an integer.
     * @param i 0 through 8 where 0 is FRONT_RIGHT and 8 is BACK_LEFT;
     * @return PanelCellSegment
     */

    public static PanelCellSegment fromInteger(int i) {
        switch (i) {
            case 0: return FRONT_RIGHT;
            case 1: return FRONT;
            case 2: return FRONT_LEFT;
            case 3: return RIGHT;
            case 4: return CENTER;
            case 5: return LEFT;
            case 6: return BACK_RIGHT;
            case 7: return BACK;
            case 8: return BACK_LEFT;
            default: return null;
        }
    }
}

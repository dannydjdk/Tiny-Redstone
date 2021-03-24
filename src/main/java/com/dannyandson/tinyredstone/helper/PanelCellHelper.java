package com.dannyandson.tinyredstone.helper;

import com.dannyandson.tinyredstone.blocks.PanelCellSegment;
import net.minecraft.util.Direction;

import static net.minecraft.util.Direction.*;

public class PanelCellHelper {
    public static PanelCellSegment getSegment(Direction cellDirection, double x, double z, int row, int cell) {
        double segmentX = (x - (row/8d))*8d;
        double segmentZ = (z - (cell/8d))*8d;
        int segmentRow = Math.round((float)(segmentX*3f)-0.5f);
        int segmentColumn = Math.round((float)(segmentZ*3f)-0.5f);

        int segmentRow1;
        int segmentColumn1;
        if (cellDirection == NORTH) {
            segmentRow1 = segmentColumn;
            segmentColumn1 = ((segmentRow - 1) * -1) + 1;
        } else if (cellDirection == EAST) {
            segmentRow1 = ((segmentRow - 1) * -1) + 1;
            segmentColumn1 = ((segmentColumn - 1) * -1) + 1;
        } else if (cellDirection == SOUTH){
            segmentRow1 = ((segmentColumn - 1) * -1) + 1;
            segmentColumn1 = segmentRow;
        } else {
            segmentRow1 = segmentRow;
            segmentColumn1 = segmentColumn;
        }
        return PanelCellSegment.fromInteger((segmentRow1*3)+segmentColumn1);
    }
}

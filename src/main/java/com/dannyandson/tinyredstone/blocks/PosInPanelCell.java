package com.dannyandson.tinyredstone.blocks;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import static net.minecraft.util.Direction.*;

public class PosInPanelCell extends PanelCellPos {
    private double x;
    private double z;

    protected PosInPanelCell(int row, int cell, double x, double z) {
        super(row, cell);
        this.x = x;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return z;
    }

    public static PosInPanelCell fromCoordinates(PanelCellPos panelCellPos, double x, double z) {
        if(x >= 0.0 && x <= 1.0 && z >= 0.0 && z <= 1.0) {
            return new PosInPanelCell(panelCellPos.getRow(), panelCellPos.getCell(), x, z);
        }
        return null;
    }

    public static PosInPanelCell fromHitVec(BlockPos pos, Vector3d hitVec) {
        double x = hitVec.x - pos.getX();
        double z = hitVec.z - pos.getZ();

        PanelCellPos panelCellPos = PanelCellPos.fromCoordinates(x, z);
        double segmentX = (x - (panelCellPos.getRow()/8d))*8d;
        double segmentZ = (z - (panelCellPos.getCell()/8d))*8d;

        return fromCoordinates(panelCellPos, x, z);
    }

    public PanelCellSegment getSegment(Direction cellDirection) {
        int segmentRow = Math.round((float)(this.x*3f)-0.5f);
        int segmentColumn = Math.round((float)(this.z*3f)-0.5f);

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

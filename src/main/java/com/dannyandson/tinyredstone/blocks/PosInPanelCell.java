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

    public double getZ() {
        return z;
    }

    public PanelCellSegment getSegment() {
        int segmentRow = Math.round((float)(this.x*3f)-0.5f);
        int segmentColumn = Math.round((float)(this.z*3f)-0.5f);

        if(segmentRow == 0) {
            switch (segmentColumn) {
                case 0: return PanelCellSegment.FRONT_RIGHT;
                case 1: return PanelCellSegment.FRONT;
                case 2: return PanelCellSegment.FRONT_LEFT;
            }
        } else if(segmentRow == 1) {
            switch (segmentColumn) {
                case 0: return PanelCellSegment.RIGHT;
                case 1: return PanelCellSegment.CENTER;
                case 2: return PanelCellSegment.LEFT;
            }
        } else if(segmentRow == 2) {
            switch (segmentColumn) {
                case 0: return PanelCellSegment.BACK_RIGHT;
                case 1: return PanelCellSegment.BACK;
                case 2: return PanelCellSegment.BACK_LEFT;
            }
        }
        return null;
    }

    public static PosInPanelCell fromCoordinates(PanelTile panelTile, PanelCellPos panelCellPos, double x, double z) {
        if(x >= 0.0 && x <= 1.0 && z >= 0.0 && z <= 1.0) {
            double rotatedX;
            double rotatedZ;

            Direction direction = panelTile.cellDirections.get(panelCellPos.getIndex());

            if (direction == NORTH) {
                rotatedX = z;
                rotatedZ = 1.0 - x;
            } else if (direction == EAST) {
                rotatedX = 1.0 - x;
                rotatedZ = 1.0 - z;
            } else if (direction == SOUTH){
                rotatedX = 1.0 - z;
                rotatedZ = x;
            } else {
                rotatedX = x;
                rotatedZ = z;
            }

            return new PosInPanelCell(panelCellPos.getRow(), panelCellPos.getCell(), rotatedX, rotatedZ);
        }
        return null;
    }

    public static PosInPanelCell fromHitVec(PanelTile panelTile, BlockPos pos, Vector3d hitVec) {
        double x = hitVec.x - pos.getX();
        double z = hitVec.z - pos.getZ();

        PanelCellPos panelCellPos = PanelCellPos.fromCoordinates(x, z);
        if(panelCellPos == null) return null;

        x = (x - (panelCellPos.getRow()/8d))*8d;
        z = (z - (panelCellPos.getCell()/8d))*8d;

        return fromCoordinates(panelTile, panelCellPos, x, z);
    }
}

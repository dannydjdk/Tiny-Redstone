package com.dannyandson.tinyredstone.blocks;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class PanelCellPos {
    private int row;
    private int cell;

    protected PanelCellPos(int row, int cell) {
        this.row = row;
        this.cell = cell;
    }


    public static PanelCellPos fromRowCell(int row, int cell) {
        if (row >= 0 && row < 8 && cell >= 0 && cell < 8) {
            return new PanelCellPos(row, cell);
        }
        return null;
    }

    public static PanelCellPos fromCoordinates(double x, double z) {
        return fromRowCell(Math.round((float) (x * 8f) - 0.5f), Math.round((float) (z * 8f) - 0.5f));
    }

    public static PanelCellPos fromHitVec(BlockPos pos, Vector3d hitVec) {
        return fromCoordinates(hitVec.x - pos.getX(), hitVec.z - pos.getZ());
    }

    public int getRow() {
        return row;
    }

    public int getCell() {
        return cell;
    }

    public int getIndex() {
        return (row * 8) + cell;
    }
}

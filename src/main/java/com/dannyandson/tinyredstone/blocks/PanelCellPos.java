package com.dannyandson.tinyredstone.blocks;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class PanelCellPos {
    private int row;
    private int column;

    protected PanelCellPos(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getIndex() {
        return (row * 8) + column;
    }

    public static PanelCellPos fromCoordinates(double x, double z) {
        int row = Math.round((float) (x * 8f) - 0.5f);
        int cell = Math.round((float) (z * 8f) - 0.5f);
        if (row >= 0 && row < 8 && cell >= 0 && cell < 8) {
            return new PanelCellPos(row, cell);
        }
        return null;
    }

    public static PanelCellPos fromHitVec(BlockPos pos, Direction panelFacing, Vector3d hitVec) {
        double relX = hitVec.x - pos.getX();
        double relZ = hitVec.z - pos.getZ();

        if (panelFacing==Direction.NORTH)
            relZ = 1-(hitVec.y-pos.getY());
        else if (panelFacing==Direction.EAST)
            relX = hitVec.y-pos.getY();
        else if (panelFacing==Direction.SOUTH)
            relZ = hitVec.y-pos.getY();
        else if (panelFacing==Direction.WEST)
            relX = 1-(hitVec.y-pos.getY());
        else if (panelFacing==Direction.UP)
            relZ = 1-relZ;


        return fromCoordinates(relX, relZ);
    }

    public static PanelCellPos fromIndex(Integer cellIndex)
    {
        int row = Math.round((cellIndex.floatValue() / 8f) - 0.5f);
        int cell = cellIndex % 8;
        return new PanelCellPos(row,cell);
    }

    public static PanelCellPos fromRowColumn(int row, int column)
    {
        return new PanelCellPos(row,column);
    }

    /**
     * Gets the PanelCellPos adjacent to this one in the specified direction.
     * Returns null if the index is off this tile.
     *
     * @param side The direction of the neighbor from the cell.
     * @return PanelCellPos representing the neighbor position, or null if we're at the edge of the panel
     */
    @Nullable
    public PanelCellPos offset(Side side) {

        if (side==Side.FRONT) {
            if (column <= 0) {
                return null;
            } else {
                return new PanelCellPos(row, column -1);
            }
        }
        if (side==Side.BACK) {
            if (column >= 7) {
                return null;
            } else {
                return new PanelCellPos(row,column+1);
            }
        }
        if (side==Side.LEFT) {
            if (row <= 0) {
                return null;
            } else {
                return new PanelCellPos(row-1,column);
            }
        }
        if (side==Side.RIGHT) {
            if (row >= 7) {
                return null;
            } else {
                return new PanelCellPos(row+1,column);
            }
        }

        return null;

    }

}

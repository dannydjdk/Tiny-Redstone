package com.dannyandson.tinyredstone.blocks;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.CheckForNull;

public class PanelCellPos {
    private final int row;
    private final int column;
    private final PanelTile panelTile;

    protected PanelCellPos(PanelTile panelTile, int row, int column) {
        this.row = row;
        this.column = column;
        this.panelTile = panelTile;
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

    public PanelTile getPanelTile(){
        return this.panelTile;
    }

    public static PanelCellPos fromCoordinates(PanelTile panelTile,double x, double z) {
        int row = Math.round((float) (x * 8f) - 0.5f);
        int cell = Math.round((float) (z * 8f) - 0.5f);
        if (row >= 0 && row < 8 && cell >= 0 && cell < 8) {
            return new PanelCellPos(panelTile,row, cell);
        }
        return null;
    }

    public static PanelCellPos fromHitVec(PanelTile panelTile, Direction panelFacing, Vector3d hitVec) {

        BlockPos pos = panelTile.getPos();

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

        return fromCoordinates(panelTile, relX, relZ);
    }

    public static PanelCellPos fromIndex(PanelTile panelTile, Integer cellIndex)
    {
        int row = Math.round((cellIndex.floatValue() / 8f) - 0.5f);
        int cell = cellIndex % 8;
        return new PanelCellPos(panelTile, row,cell);
    }

    public static PanelCellPos fromRowColumn(PanelTile panelTile, int row, int column)
    {
        return new PanelCellPos(panelTile, row,column);
    }

    /**
     * Gets the PanelCellPos adjacent to this one in the specified direction.
     * It will also return cells on adjacent tiles.
     * Returns null if the index is not on a tile.
     *
     * @param side The direction of the neighbor from the cell.
     * @return PanelCellPos representing the neighbor position, or null if we're at the edge of the panel
     */
    @CheckForNull
    public PanelCellPos offset(Side side) {
        PanelCellPos cellPos = null;

        if (side==Side.FRONT) {
            if (column > 0) {
                cellPos = new PanelCellPos(panelTile, row, column -1);
            }
        }
        else if (side==Side.BACK) {
            if (column < 7) {
                cellPos =  new PanelCellPos(panelTile, row,column+1);
            }
        }
        else if (side==Side.LEFT) {
            if (row > 0) {
                cellPos =  new PanelCellPos(panelTile,row-1,column);
            }
        }
        else if (side==Side.RIGHT) {
            if (row < 7) {
                cellPos =  new PanelCellPos(panelTile,row+1,column);
            }
        }

        if (cellPos==null)
        {
            Direction direction = panelTile.getDirectionFromSide(side);
            TileEntity te = panelTile.getWorld().getTileEntity(panelTile.getPos().offset(direction));

            if (te instanceof PanelTile)
            {
                PanelTile panelTile2 = (PanelTile) te;
                if (panelTile2.getBlockState().get(BlockStateProperties.FACING)==panelTile.getBlockState().get(BlockStateProperties.FACING))
                {
                    int neighborRow, neighborColumn;
                    if (side==Side.FRONT || side==Side.BACK) {
                        neighborRow = row;
                        neighborColumn = (column - 7) * -1;
                    } else {
                        neighborRow = (row - 7) * -1;
                        neighborColumn = column;
                    }
                    cellPos = new PanelCellPos(panelTile2,neighborRow,neighborColumn);

                }
            }
        }

        return cellPos;
    }

    /**
     * Gets the IPanelCell at this position or null if position is empty
     * @return IPanelCell or null
     */
    @CheckForNull
    public IPanelCell getIPanelCell()
    {
        return this.panelTile.getIPanelCell(this);
    }

    /**
     * Gets the Side the cell at this position is facing within the panel tile
     * or null if position is empty
     * @return Side or null
     */
    @CheckForNull
    public Side getCellFacing()
    {
        return this.panelTile.getCellFacing(this);
    }

}

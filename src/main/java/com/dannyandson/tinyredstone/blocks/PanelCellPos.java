package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.api.IPanelCell;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.CheckForNull;

public class PanelCellPos {
    private final int row;
    private final int column;
    private final int level;
    private Integer index=null;
    private final PanelTile panelTile;

    protected PanelCellPos(PanelTile panelTile, int row, int column, int level) {
        this.row = row;
        this.column = column;
        this.level = level;
        this.panelTile = panelTile;
    }

    /**
     * Returns the row number of this position (x value within the tile grid)
     * @return integer 0-7 corresponding to the row number
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column number of this position (y value within the tile grid)
     * @return integer 0-7 corresponding to the column number
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the level number of this position (y value within the tile grid)
     * @return integer 0-6 corresponding to the level number
     */
    public int getLevel(){
        return level;
    }

    public int getIndex() {
        if (this.index==null)
            this.index=(level*64) + (row * 8) + column;
        return this.index;
    }

    public PanelTile getPanelTile(){
        return this.panelTile;
    }

    public static PanelCellPos fromCoordinates(PanelTile panelTile,double x,double y, double z) {
        int row = Math.round((float) (x * 8f) - 0.5f);
        int cell = Math.round((float) (z * 8f) - 0.5f);
        int level = Math.round((float)(y * 8f) - 0.5f)-1;
        if (row >= 0 && row < 8 && cell >= 0 && cell < 8 && level>=0 && level<7) {
            return new PanelCellPos(panelTile,row, cell, level);
        }
        return null;
    }

    public static PanelCellPos fromHitVec(PanelTile panelTile, Direction panelFacing, BlockRayTraceResult result) {

        BlockPos pos = panelTile.getPos();
        Direction rayTraceDirection = result.getFace().getOpposite();
        Vector3d hitVec = result.getHitVec().add((double)rayTraceDirection.getXOffset()*.001d,(double)rayTraceDirection.getYOffset()*.001d,(double)rayTraceDirection.getZOffset()*.001d);

        double relX,relY,relZ;

        if (panelFacing==Direction.NORTH) {
            relX = hitVec.x - pos.getX();
            relY = hitVec.z-pos.getZ();
            relZ = 1 - (hitVec.y - pos.getY());
        }
        else if (panelFacing==Direction.EAST) {
            relX = hitVec.y - pos.getY();
            relY = 1-(hitVec.x - pos.getX());
            relZ = hitVec.z - pos.getZ();
        }
        else if (panelFacing==Direction.SOUTH) {
            relX = hitVec.x - pos.getX();
            relY = 1-(hitVec.z-pos.getZ());
            relZ = hitVec.y - pos.getY();
        }
        else if (panelFacing==Direction.WEST) {
            relX =1-(hitVec.y - pos.getY());
            relY = hitVec.x-pos.getX();
            relZ = hitVec.z - pos.getZ();
        }
        else if (panelFacing==Direction.UP) {
            relX = hitVec.x - pos.getX();
            relY = 1 - (hitVec.y - pos.getY());
            relZ = 1 - (hitVec.z - pos.getZ());
        }
        else{
            relX = hitVec.x - pos.getX();
            relZ = hitVec.z - pos.getZ();
            relY = hitVec.y - pos.getY();
        }

        if (relY<.125 && relY>.0625)
            relY+=.002f;

        return fromCoordinates(panelTile, relX,relY, relZ);
    }

    public static PanelCellPos fromIndex(PanelTile panelTile, Integer cellIndex)
    {
        int level = Math.round((cellIndex.floatValue()/64f) - 0.5f);
        int row = Math.round(((cellIndex.floatValue()% 64) / 8f) - 0.5f);
        int cell = cellIndex % 8;
        return new PanelCellPos(panelTile, row,cell,level);
    }

    public static PanelCellPos fromRowColumn(PanelTile panelTile, int row, int column, int level)
    {
        return new PanelCellPos(panelTile, row,column,level);
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
                cellPos = new PanelCellPos(panelTile, row, column -1,level);
            }
        }
        else if (side==Side.BACK) {
            if (column < 7) {
                cellPos =  new PanelCellPos(panelTile, row,column+1,level);
            }
        }
        else if (side==Side.LEFT) {
            if (row > 0) {
                cellPos =  new PanelCellPos(panelTile,row-1,column,level);
            }
        }
        else if (side==Side.RIGHT) {
            if (row < 7) {
                cellPos =  new PanelCellPos(panelTile,row+1,column,level);
            }
        }
        else if (side==Side.TOP) {
            if (level < 6) {
                cellPos =  new PanelCellPos(panelTile,row,column,level+1);
            }
        }
        else if (side==Side.BOTTOM) {
            if (level > 0) {
                cellPos =  new PanelCellPos(panelTile,row,column,level-1);
            }
        }

        //TODO consider support for linking panels facing in different directions
        if (cellPos==null && side!=Side.TOP && side!=Side.BOTTOM)
        {
            Direction direction = panelTile.getDirectionFromSide(side);
            TileEntity te = panelTile.getLevel().getBlockEntity(panelTile.getPos().offset(direction));

            if (te instanceof PanelTile)
            {
                PanelTile panelTile2 = (PanelTile) te;
                if (panelTile2.getBlockState().get(BlockStateProperties.FACING)==panelTile.getBlockState().get(BlockStateProperties.FACING))
                {
                    int neighborRow, neighborColumn, neighborLevel;
                    if (side==Side.FRONT || side==Side.BACK) {
                        neighborRow = row;
                        neighborColumn = (column - 7) * -1;
                        neighborLevel = level;
                    }/* else if (side==Side.TOP||side==Side.BOTTOM) {
                        neighborRow = row;
                        neighborColumn = column;
                        neighborLevel = (level-6)*-1;
                    }*/
                    else{
                        neighborRow = (row - 7) * -1;
                        neighborColumn = column;
                        neighborLevel = level;
                    }
                    cellPos = new PanelCellPos(panelTile2,neighborRow,neighborColumn,neighborLevel);

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

    /**
     * Gets a PanelCellNeighbor object providing data about the neighboring cell or block.
     *
     * @param side The direction of the neighbor relative to this cell's facing direction.
     * @return PanelCellNeighbor object, null for an empty cell.
     */
    @CheckForNull
    public PanelCellNeighbor getNeighbor(Side side) {
        Side cellFacing = this.getCellFacing();
        return getNeighbor(side,cellFacing);
    }

    public PanelCellNeighbor getNeighbor(Side side, Side cellFacing)
    {
        Side towardPanelSide;
        if (cellFacing==Side.FRONT)
            towardPanelSide=side;
        else if ((side==Side.TOP || side==Side.BOTTOM) && cellFacing!=Side.TOP && cellFacing!=Side.BOTTOM)
            towardPanelSide=side;
        else if (cellFacing==Side.BACK)
            towardPanelSide=side.getOpposite();
        else if (cellFacing==Side.RIGHT)
            towardPanelSide=side.rotateYCW();
        else if (cellFacing==Side.LEFT)
            towardPanelSide=side.rotateYCCW();
        else if (cellFacing==Side.TOP)
            towardPanelSide=side.rotateBack();
        else// if (cellFacing==Side.BOTTOM)
            towardPanelSide=side.rotateForward();

        PanelCellPos neighborPos = this.offset(towardPanelSide);

        if (neighborPos != null) {
            IPanelCell neighborCell = neighborPos.getIPanelCell();

            if (neighborCell != null) {
                Side neighborSide = neighborPos.getPanelTile().getPanelCellSide(neighborPos, towardPanelSide.getOpposite());
                return new PanelCellNeighbor(neighborPos, neighborCell, neighborSide, towardPanelSide);
            } else if (neighborPos.getPanelTile().checkCellForPistonExtension(neighborPos)) {
                return new PanelCellNeighbor(neighborPos, null, null, side);
            }

        } else if (towardPanelSide!=Side.BOTTOM){
            BlockPos blockPos = this.panelTile.getPos().offset(this.panelTile.getDirectionFromSide(towardPanelSide));
            return new PanelCellNeighbor(this.panelTile, blockPos, towardPanelSide);
        }


        return null;
    }

}

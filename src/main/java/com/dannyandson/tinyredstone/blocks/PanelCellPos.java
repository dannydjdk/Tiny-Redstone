package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.api.IPanelCell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
        int level = Math.round((float)(y * 8f) - 0.5f)-((panelTile.hasBase())?1:0);
        if (row >= 0 && row < 8 && cell >= 0 && cell < 8 && level>=0 && level<(panelTile.hasBase()?7:8)) {
            return new PanelCellPos(panelTile,row, cell, level);
        }
        return null;
    }

    public static PanelCellPos fromHitVec(PanelTile panelTile, Direction panelFacing, BlockHitResult result) {

        BlockPos pos = panelTile.getBlockPos();
        Direction rayTraceDirection = result.getDirection().getOpposite();
        Vec3 hitVec = result.getLocation().add((double)rayTraceDirection.getStepX()*.001d,(double)rayTraceDirection.getStepY()*.001d,(double)rayTraceDirection.getStepZ()*.001d);

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

        if (panelTile.hasBase() && relY<.125 && relY>.0625)
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
            if (level < (panelTile.hasBase()?6:7)) {
                cellPos =  new PanelCellPos(panelTile,row,column,level+1);
            }
        }
        else if (side==Side.BOTTOM) {
            if (level > 0) {
                cellPos =  new PanelCellPos(panelTile,row,column,level-1);
            }
        }

        boolean panel1HasBase = panelTile.hasBase();
        //TODO consider support for linking panels facing in different directions
        if (cellPos==null && !(panel1HasBase && side==Side.BOTTOM))
        {
            Direction direction = panelTile.getDirectionFromSide(side);
            BlockEntity te = panelTile.getLevel().getBlockEntity(panelTile.getBlockPos().relative(direction));

            if (te instanceof PanelTile panelTile2)
            {
                boolean panel2HasBase = panelTile2.hasBase();
                if (panelTile2.getBlockState().getValue(BlockStateProperties.FACING)==panelTile.getBlockState().getValue(BlockStateProperties.FACING))
                {
                    int neighborRow, neighborColumn, neighborLevel;
                    int levelOffset = (panel1HasBase==panel2HasBase)?0:(panel1HasBase)?1:-1;
                    if (side==Side.FRONT || side==Side.BACK) {
                        neighborRow = row;
                        neighborColumn = (column - 7) * -1;
                        neighborLevel = level+levelOffset;
                    } else if (side==Side.TOP||side==Side.BOTTOM) {
                        if ((panel2HasBase && side == Side.TOP) )
                            return null;
                        neighborRow = row;
                        neighborColumn = column;
                        neighborLevel = (side == Side.TOP)?0:(level - 7) * -1 + levelOffset;
                    }
                    else{
                        neighborRow = (row - 7) * -1;
                        neighborColumn = column;
                        neighborLevel = level+levelOffset;
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

    @CheckForNull
    public Side getBaseDirection(){
        IPanelCell thisCell = getIPanelCell();
        if (thisCell==null || thisCell.getBaseSide()==null)return null;
        return (thisCell.getBaseSide()==Side.FRONT)?this.getCellFacing():thisCell.getBaseSide();
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

        } else if (!panelTile.hasBase() || towardPanelSide!=Side.BOTTOM){
            BlockPos blockPos = this.panelTile.getBlockPos().relative(this.panelTile.getDirectionFromSide(towardPanelSide));
            return new PanelCellNeighbor(this.panelTile, blockPos, towardPanelSide);
        }


        return null;
    }

}

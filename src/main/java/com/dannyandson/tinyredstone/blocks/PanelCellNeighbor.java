package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.blocks.panelcells.TinyBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class PanelCellNeighbor {
    private IPanelCell iPanelCell=null;
    private BlockPos blockPos=null;
    private final Side neighborDirection;
    private Side neighborsSide =null;
    private final PanelTile panelTile;
    private PanelCellPos cellPos;

    /**
     * Construct a PanelCellNeighbor object for querying an IPanelCell
     * @param cellPos Cell position of the neighbor
     * @param panelCell IPanelCell of the neighbor
     * @param neighborsSide Side of the neighbor facing the querying cell.
     * @param neighborDirection Direction of the neighbor from the querying cell relative to the panel facing
     */
    PanelCellNeighbor(PanelCellPos cellPos, IPanelCell panelCell, Side neighborsSide, Side neighborDirection)
    {
        this.iPanelCell=panelCell;
        this.panelTile= cellPos.getPanelTile();
        this.neighborsSide =neighborsSide;
        this.cellPos=cellPos;
        this.neighborDirection =neighborDirection;
    }

    /**
     * Construct a PanelCellNeighbor object for querying a block in the world
     * @param panelTile PanelTile of the querying cell
     * @param blockPos Position of the neighbor block
     * @param neighborDirection Direction of the neighbor block relative to the panel facing
     */
    PanelCellNeighbor(PanelTile panelTile,BlockPos blockPos,Side neighborDirection)
    {
        this.blockPos=blockPos;
        this.panelTile=panelTile;
        this.neighborDirection = neighborDirection;
    }

    /**
     * Gets redstone output of the facing side of the cell
     *
     * @return integer 0-15 indicating the strength of redstone signal
     */
    public int getWeakRsOutput() {
        if (iPanelCell!=null){
            return iPanelCell.getWeakRsOutput(neighborsSide);
        }
        else if (blockPos!=null)
        {
            return panelTile.getWorld().getRedstonePower(blockPos,panelTile.getDirectionFromSide(neighborDirection));
        }
        return 0;
    }

    public int getStrongRsOutput() {
        if (iPanelCell!=null){
            return iPanelCell.getStrongRsOutput(neighborsSide);
        }
        else if (blockPos!=null)
        {
            return panelTile.getWorld().getStrongPower(blockPos,panelTile.getDirectionFromSide(neighborDirection));
        }
        return 0;
    }

    public boolean hasComparatorOverride()
    {
        if (blockPos!=null)
            return panelTile.comparatorOverrides.containsKey(neighborDirection);
        return false;
    }

    public int getComparatorOverride()
    {
        if (hasComparatorOverride())
            return panelTile.comparatorOverrides.get(neighborDirection);
        return 0;
    }

    /**
     * Does the power level drop when transmitting between cells/blocks (such as with redstone dust)?
     *
     * @return true if power level should drop, false if not
     */
    public boolean powerDrops() {
        if (iPanelCell!=null)
            return iPanelCell.powerDrops();
        if(blockPos!=null)
            return panelTile.getWorld().getBlockState(blockPos).getBlock() == Blocks.REDSTONE_WIRE;
        return false;
    }

    public boolean isPushable()
    {
        return panelTile.canExtendTo(this.cellPos,this.neighborDirection,0);
    }

    /**
     * Is this neighbor on a Redstone Panel
     * @return True if this neighbor is on a panel, false if it is a block in the world (you are an edge cell)
     */
    public boolean isOnPanel()
    {
        return this.cellPos!=null;
    }

    public boolean canConnectRedstone()
    {
        BlockState blockState = getNeighborBlockState();
        if (blockState!=null)
            return blockState.canConnectRedstone(panelTile.getWorld(),this.blockPos,panelTile.getDirectionFromSide(neighborDirection));
        if (iPanelCell!=null && !(iPanelCell instanceof TinyBlock) && !(iPanelCell.powerDrops()))
            return true;
        return false;
    }

    /**
     * Gets the neighboring cell if neighbor is an iPanelCell
     * @return Either an IPanelCell instance or null if no neighbor is not an IPanelCell
     */
    public IPanelCell getNeighborIPanelCell() {
        return iPanelCell;
    }
    /**
     * Gets the neighboring BlockState if the neighbor is a block
     * @return Either a BlockState or null if neighbor is not a block
     */
    public BlockState getNeighborBlockState()
    {
        if (blockPos!=null)
        {
            return panelTile.getWorld().getBlockState(blockPos);
        }
        return null;
    }

}

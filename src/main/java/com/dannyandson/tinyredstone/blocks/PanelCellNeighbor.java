package com.dannyandson.tinyredstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class PanelCellNeighbor {
    private IPanelCell iPanelCell=null;
    private BlockPos blockPos=null;
    private Direction facingDirection=null;
    private IPanelCell.PanelCellSide panelCellSide=null;
    private final PanelTile panelTile;
    private Integer index=null;

    PanelCellNeighbor(PanelTile panelTile,IPanelCell panelCell, IPanelCell.PanelCellSide panelCellSide, Direction facing, Integer index)
    {
        this.iPanelCell=panelCell;
        this.panelTile=panelTile;
        this.panelCellSide=panelCellSide;
        this.index=index;
        this.facingDirection=facing;
    }
    PanelCellNeighbor(PanelTile panelTile,BlockPos blockPos,Direction facingDirection)
    {
        this.blockPos=blockPos;
        this.panelTile=panelTile;
        this.facingDirection= facingDirection;
    }

    /**
     * Gets redstone output of the facing side of the cell
     *
     * @return integer 0-15 indicating the strength of redstone signal
     */
    public int getWeakRsOutput() {
        if (iPanelCell!=null){
            return iPanelCell.getWeakRsOutput(panelCellSide);
        }
        else if (blockPos!=null)
        {
            return panelTile.weakPowerFromNeighbors.get(facingDirection);
            //return panelTile.getWorld().getRedstonePower(blockPos,facingDirection);
        }
        return 0;
    }

    public int getStrongRsOutput() {
        if (iPanelCell!=null){
            return iPanelCell.getStrongRsOutput(panelCellSide);
        }
        else if (blockPos!=null)
        {
            return panelTile.strongPowerFromNeighbors.get(facingDirection);
            //return panelTile.getWorld().getStrongPower(blockPos,facingDirection);
        }
        return 0;
    }

    public boolean hasComparatorOverride()
    {
        if (blockPos!=null)
            return panelTile.comparatorOverrides.containsKey(facingDirection);
            //return panelTile.getWorld().getBlockState(blockPos).hasComparatorInputOverride();
        return false;
    }

    public int getComparatorOverride()
    {
        if (blockPos!=null && panelTile.comparatorOverrides.containsKey(facingDirection))
            return panelTile.comparatorOverrides.get(facingDirection);
            //2return panelTile.getWorld().getBlockState(blockPos).getComparatorInputOverride(panelTile.getWorld(),blockPos);
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
        return panelTile.getWorld().getBlockState(blockPos).getBlock() == Blocks.REDSTONE_WIRE;
    }

    public boolean isPushable()
    {
        return panelTile.canExtendTo(this.index,this.facingDirection,0);
    }

    /**
     * Gets the neighboring cell if neighbor is an iPanelCell
     * @return Either an IPanelCell instance or null if no neighbor is not an IPanelCell
     */
    public IPanelCell getNeighborIPanelCell()
    {
        if (iPanelCell!=null)
        {
            return iPanelCell;
        }
        return null;
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

package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.blocks.panelcells.Button;
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
        }
        return 0;
    }

    public boolean hasComparatorOverride()
    {
        if (blockPos!=null)
            return panelTile.comparatorOverrides.containsKey(facingDirection);
        return false;
    }

    public int getComparatorOverride()
    {
        if (blockPos!=null && panelTile.comparatorOverrides.containsKey(facingDirection))
            return panelTile.comparatorOverrides.get(facingDirection);
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
        return panelTile.canExtendTo(this.index,this.facingDirection,0);
    }

    /**
     * Is this neighbor on a Redstone Panel
     * @return True if this neighbor is on a panel, false if it is a block in the world (you are an edge cell)
     */
    public boolean isOnPanel()
    {
        return this.index!=null;
    }

    public boolean canConnectRedstone()
    {
        BlockState blockState = getNeighborBlockState();
        if (blockState!=null)
            return blockState.canConnectRedstone(panelTile.getWorld(),this.blockPos,facingDirection);
        if (iPanelCell!=null && iPanelCell instanceof Button)
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

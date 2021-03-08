package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PanelTile extends TileEntity implements ITickableTileEntity {

    //TODO
    // cell types - button, lever, lamp?, pistons (useful for resetting clocks and other more complicated circuitry)
    // add-on - gates, clock
    // One probe support
    // troubleshoot issue with powered neighbor blocks not updating their neighbors

    public Map<Integer, IPanelCell> cells = new HashMap<>();
    public Map<Integer, Direction> cellDirections = new HashMap<>();

    public Map<Direction, Integer> weakPowerFromNeighbors = new HashMap<>();
    public Map<Direction, Integer> strongPowerFromNeighbors = new HashMap<>();
    public Map<Direction, Integer> strongPowerToNeighbors = new HashMap<>();
    public Map<Direction, Integer> weakPowerToNeighbors = new HashMap<>();
    public Integer Color = DyeColor.GRAY.getColorValue();

    public Map<Direction, Integer> comparatorOverrides = new HashMap<>();

    public PanelTile() {
        super(Registration.REDSTONE_PANEL_TILE.get());

    }

    /* When the world loads from disk, the server needs to send the TileEntity information to the client
    //  it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this:
    //  getUpdatePacket() and onDataPacket() are used for one-at-a-time TileEntity updates
    //  getUpdateTag() and handleUpdateTag() are used by vanilla to collate together into a single chunk update packet
    // In this case, we need it for the gem colour.  There's no need to save the gem angular position because
    //  the player will never notice the difference and the client<-->server synchronisation lag will make it
    //  inaccurate anyway
    */

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        int tileEntityType = -1;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new SUpdateTileEntityPacket(this.pos, tileEntityType, nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        if (this.world.isRemote) {
            this.cells.clear();
            this.cellDirections.clear();
        }
        BlockState blockState = world.getBlockState(pos);
        read(blockState, pkt.getNbtCompound());   // read from the nbt in the packet
    }

    /* Creates a tag containing the TileEntity information, used by vanilla to transmit from server to client*/

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        return nbtTagCompound;
    }

    /* Populates this TileEntity with information from the tag, used by vanilla to transmit from server to client*/

    @Override
    public void handleUpdateTag(BlockState blockState, CompoundNBT tag) {
        if (this.world.isRemote) {
            this.cells.clear();
            this.cellDirections.clear();
        }
        this.read(blockState, tag);
    }

    public CompoundNBT saveToNbt(CompoundNBT compoundNBT) {

        CompoundNBT cellsNBT = new CompoundNBT();

        for (Integer key : this.cells.keySet()) {
            IPanelCell cell = this.cells.get(key);
            CompoundNBT cellNBT = new CompoundNBT();

            cellNBT.putString("class", cell.getClass().getCanonicalName());
            cellNBT.putInt("direction", this.cellDirections.get(key).getIndex());
            cellNBT.put("data", cell.writeNBT());

            cellsNBT.put(key.toString(), cellNBT);
        }

        compoundNBT.put("cells", cellsNBT);
        compoundNBT.putInt("color", this.Color);

        return compoundNBT;
    }

    /* This is where you save any data that you don't want to lose when the tile entity unloads
    // In this case, we only need to store the gem colour.  For examples with other types of data, see MBE20*/
    @Override
    public CompoundNBT write(CompoundNBT parentNBTTagCompound) {
        try {
            CompoundNBT strongPowerFromNeighbors = new CompoundNBT();
            strongPowerFromNeighbors.putInt(Direction.NORTH.getIndex() + "", this.strongPowerFromNeighbors.get(Direction.NORTH));
            strongPowerFromNeighbors.putInt(Direction.SOUTH.getIndex() + "", this.strongPowerFromNeighbors.get(Direction.SOUTH));
            strongPowerFromNeighbors.putInt(Direction.EAST.getIndex() + "", this.strongPowerFromNeighbors.get(Direction.EAST));
            strongPowerFromNeighbors.putInt(Direction.WEST.getIndex() + "", this.strongPowerFromNeighbors.get(Direction.WEST));
            parentNBTTagCompound.put("strong_power_incoming", strongPowerFromNeighbors);

            CompoundNBT weakPowerFromNeighbors = new CompoundNBT();
            weakPowerFromNeighbors.putInt(Direction.NORTH.getIndex() + "", this.weakPowerFromNeighbors.get(Direction.NORTH));
            weakPowerFromNeighbors.putInt(Direction.SOUTH.getIndex() + "", this.weakPowerFromNeighbors.get(Direction.SOUTH));
            weakPowerFromNeighbors.putInt(Direction.EAST.getIndex() + "", this.weakPowerFromNeighbors.get(Direction.EAST));
            weakPowerFromNeighbors.putInt(Direction.WEST.getIndex() + "", this.weakPowerFromNeighbors.get(Direction.WEST));
            parentNBTTagCompound.put("weak_power_incoming", weakPowerFromNeighbors);

            CompoundNBT strongPowerToNeighbors = new CompoundNBT();
            strongPowerToNeighbors.putInt(Direction.NORTH.getIndex() + "", this.strongPowerToNeighbors.get(Direction.NORTH));
            strongPowerToNeighbors.putInt(Direction.SOUTH.getIndex() + "", this.strongPowerToNeighbors.get(Direction.SOUTH));
            strongPowerToNeighbors.putInt(Direction.EAST.getIndex() + "", this.strongPowerToNeighbors.get(Direction.EAST));
            strongPowerToNeighbors.putInt(Direction.WEST.getIndex() + "", this.strongPowerToNeighbors.get(Direction.WEST));
            parentNBTTagCompound.put("strong_power_outgoing", strongPowerToNeighbors);

            CompoundNBT weakPowerToNeighbors = new CompoundNBT();
            weakPowerToNeighbors.putInt(Direction.NORTH.getIndex() + "", this.weakPowerToNeighbors.get(Direction.NORTH));
            weakPowerToNeighbors.putInt(Direction.SOUTH.getIndex() + "", this.weakPowerToNeighbors.get(Direction.SOUTH));
            weakPowerToNeighbors.putInt(Direction.EAST.getIndex() + "", this.weakPowerToNeighbors.get(Direction.EAST));
            weakPowerToNeighbors.putInt(Direction.WEST.getIndex() + "", this.weakPowerToNeighbors.get(Direction.WEST));
            parentNBTTagCompound.put("weak_power_outgoing", weakPowerToNeighbors);

            CompoundNBT comparatorOverrideNBT = new CompoundNBT();
            for(Direction cDirection : comparatorOverrides.keySet())
            {
                comparatorOverrideNBT.putInt(cDirection.getIndex()+"",comparatorOverrides.get(cDirection));
            }
            parentNBTTagCompound.put("comparator_overrides",comparatorOverrideNBT);

        } catch (NullPointerException exception) {
            TinyRedstone.LOGGER.error("Exception thrown when attempting to save power inputs and outputs: " + exception.getMessage());
        }

        return super.write(this.saveToNbt(parentNBTTagCompound));
    }

    // This is where you load the data that you saved in writeToNBT
    @Override
    public void read(BlockState blockState, CompoundNBT parentNBTTagCompound) {
        super.read(blockState, parentNBTTagCompound);

        // important rule: never trust the data you read from NBT, make sure it can't cause a crash

        CompoundNBT cellsNBT = parentNBTTagCompound.getCompound("cells");

        for (Integer i = 0; i < 64; i++) {
            CompoundNBT cellNBT = cellsNBT.getCompound(i.toString());
            if (cellNBT.contains("data")) {
                String className = cellNBT.getString("class");
                try {
                    IPanelCell cell = (IPanelCell) Class.forName(className).getConstructor().newInstance();
                    cell.readNBT(cellNBT.getCompound("data"));
                    this.cells.put(i, cell);
                    this.cellDirections.put(i, Direction.byIndex(cellNBT.getInt("direction")));
                } catch (Exception exception) {
                    TinyRedstone.LOGGER.error("Exception attempting to construct IPanelCell class " + className +
                            ": " + exception.getMessage());
                }
            }
        }

        CompoundNBT strongPowerFromNeighbors = parentNBTTagCompound.getCompound("strong_power_incoming");
        if (!strongPowerFromNeighbors.isEmpty()) {
            this.strongPowerFromNeighbors.put(Direction.NORTH, strongPowerFromNeighbors.getInt(Direction.NORTH.getIndex() + ""));
            this.strongPowerFromNeighbors.put(Direction.SOUTH, strongPowerFromNeighbors.getInt(Direction.SOUTH.getIndex() + ""));
            this.strongPowerFromNeighbors.put(Direction.EAST, strongPowerFromNeighbors.getInt(Direction.EAST.getIndex() + ""));
            this.strongPowerFromNeighbors.put(Direction.WEST, strongPowerFromNeighbors.getInt(Direction.WEST.getIndex() + ""));
        }
        CompoundNBT weakPowerFromNeighbors = parentNBTTagCompound.getCompound("weak_power_incoming");
        if (!weakPowerFromNeighbors.isEmpty()) {
            this.weakPowerFromNeighbors.put(Direction.NORTH, weakPowerFromNeighbors.getInt(Direction.NORTH.getIndex() + ""));
            this.weakPowerFromNeighbors.put(Direction.SOUTH, weakPowerFromNeighbors.getInt(Direction.SOUTH.getIndex() + ""));
            this.weakPowerFromNeighbors.put(Direction.EAST, weakPowerFromNeighbors.getInt(Direction.EAST.getIndex() + ""));
            this.weakPowerFromNeighbors.put(Direction.WEST, weakPowerFromNeighbors.getInt(Direction.WEST.getIndex() + ""));
        }

        CompoundNBT strongPowerToNeighbors = parentNBTTagCompound.getCompound("strong_power_outgoing");
        if (!strongPowerToNeighbors.isEmpty()) {
            this.strongPowerToNeighbors.put(Direction.NORTH, strongPowerToNeighbors.getInt(Direction.NORTH.getIndex() + ""));
            this.strongPowerToNeighbors.put(Direction.SOUTH, strongPowerToNeighbors.getInt(Direction.SOUTH.getIndex() + ""));
            this.strongPowerToNeighbors.put(Direction.EAST, strongPowerToNeighbors.getInt(Direction.EAST.getIndex() + ""));
            this.strongPowerToNeighbors.put(Direction.WEST, strongPowerToNeighbors.getInt(Direction.WEST.getIndex() + ""));
        }
        CompoundNBT weakPowerToNeighbors = parentNBTTagCompound.getCompound("weak_power_outgoing");
        if (!weakPowerToNeighbors.isEmpty()) {
            this.weakPowerToNeighbors.put(Direction.NORTH, weakPowerToNeighbors.getInt(Direction.NORTH.getIndex() + ""));
            this.weakPowerToNeighbors.put(Direction.SOUTH, weakPowerToNeighbors.getInt(Direction.SOUTH.getIndex() + ""));
            this.weakPowerToNeighbors.put(Direction.EAST, weakPowerToNeighbors.getInt(Direction.EAST.getIndex() + ""));
            this.weakPowerToNeighbors.put(Direction.WEST, weakPowerToNeighbors.getInt(Direction.WEST.getIndex() + ""));
        }

        CompoundNBT comparatorOverridesNBT = parentNBTTagCompound.getCompound("comparator_overrides");
        if (!comparatorOverridesNBT.isEmpty())
        {
            if (comparatorOverridesNBT.contains(Direction.NORTH.getIndex()+""))
                this.comparatorOverrides.put(Direction.NORTH,comparatorOverridesNBT.getInt(Direction.NORTH.getIndex()+""));
            if (comparatorOverridesNBT.contains(Direction.SOUTH.getIndex()+""))
                this.comparatorOverrides.put(Direction.SOUTH,comparatorOverridesNBT.getInt(Direction.SOUTH.getIndex()+""));
            if (comparatorOverridesNBT.contains(Direction.EAST.getIndex()+""))
                this.comparatorOverrides.put(Direction.EAST,comparatorOverridesNBT.getInt(Direction.EAST.getIndex()+""));
            if (comparatorOverridesNBT.contains(Direction.WEST.getIndex()+""))
                this.comparatorOverrides.put(Direction.WEST,comparatorOverridesNBT.getInt(Direction.WEST.getIndex()+""));
        }

        this.Color = parentNBTTagCompound.getInt("color");

    }

    /**
     * Don't render the object if the player is too far away
     *
     * @return the maximum distance squared at which the TER should render
     */
    @Override
    public double getMaxRenderDistanceSquared() {
        final int MAXIMUM_DISTANCE_IN_BLOCKS = 16;
        return MAXIMUM_DISTANCE_IN_BLOCKS * MAXIMUM_DISTANCE_IN_BLOCKS;
    }

    @Override
    public void tick() {
        boolean dirty = false;
        //if we have a neighbor with a comparator override (outputs through comparator), check for change
        if (!this.world.isRemote)
        for (Direction direction :  comparatorOverrides.keySet())
        {
            BlockPos neighborPos = pos.offset(direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            if (neighborState.hasComparatorInputOverride()) {
                int comparatorInputOverride = neighborState.getComparatorInputOverride(world, pos.offset(direction));
                if (comparatorInputOverride != comparatorOverrides.get(direction))
                {
                    this.comparatorOverrides.put(direction,comparatorInputOverride);
                    updateSide(direction);
                    dirty=true;
                }
            }
            else {
                comparatorOverrides.remove(direction);
                if(updateSide(direction))
                {
                    dirty=true;
                }
            }

        }
        //call the tick() method in all our cells
        for (Integer index : this.cells.keySet()) {
            IPanelCell panelCell = this.cells.get(index);
            boolean update = panelCell.tick();
            if (update) {
                updateNeighborCells(index);
                dirty = true;
            }

        }

        if (dirty) {
            markDirty();
            if (updateOutputs())
                world.notifyNeighborsOfStateChange(pos, this.getBlockState().getBlock());
            sync();
        }

    }

    @Override
    public void rotate(Rotation rotationIn) {


        Map<Integer, IPanelCell> cells = new HashMap<>();
        Map<Integer, Direction> cellDirections = new HashMap<>();

        for (Integer i = 0; i < 64; i++) {
            if (this.cells.containsKey(i)) {
                int row1 = Math.round((i.floatValue() / 8f) - 0.5f);
                int cell1 = i % 8;
                int row2, cell2;

                if (rotationIn == Rotation.COUNTERCLOCKWISE_90) {
                    row2 = cell1;
                    cell2 = ((row1 - 4) * -1) + 3;
                } else if (rotationIn == Rotation.CLOCKWISE_180) {
                    row2 = ((row1 - 4) * -1) + 3;
                    cell2 = ((cell1 - 4) * -1) + 3;
                } else {
                    //default rotation 90°
                    row2 = ((cell1 - 4) * -1) + 3;
                    cell2 = row1;
                }

                cells.put(cell2 + row2 * 8, this.cells.get(i));
                cellDirections.put(cell2 + row2 * 8, rotationIn.rotate(this.cellDirections.get(i)));

            }
        }

        this.cells = cells;
        this.cellDirections = cellDirections;

        updateSide(Direction.NORTH);
        updateSide(Direction.EAST);
        updateSide(Direction.SOUTH);
        updateSide(Direction.WEST);

        if (!world.isRemote)
            markDirty();

        if (updateOutputs())
            world.notifyNeighborsOfStateChange(pos, this.getBlockState().getBlock());

        sync();

    }

    public boolean updateSide(Direction facing) {
        //row 0 is west
        //cell 0 is north
        boolean updated = false;
        List<Integer> cellIndices = new ArrayList<>();
        if (facing == Direction.WEST) {
            for (int i = 0; i < 8; i++) {
                if (cells.containsKey(i)) {
                    cellIndices.add(i);
                }
            }
        } else if (facing == Direction.NORTH) {
            for (int i = 0; i < 64; i += 8) {
                if (cells.containsKey(i)) {
                    cellIndices.add(i);
                }
            }
        } else if (facing == Direction.EAST) {
            for (int i = 56; i < 64; i++) {
                if (cells.containsKey(i)) {
                    cellIndices.add(i);
                }
            }
        } else if (facing == Direction.SOUTH) {
            for (int i = 7; i < 64; i += 8) {
                if (cells.containsKey(i)) {
                    cellIndices.add(i);
                }
            }
        }


        return updateCells(cellIndices, 0);
    }

    protected boolean updateCell(Integer cellIndex) {
        List<Integer> indices = new ArrayList<>();
        indices.add(cellIndex);
        return updateCells(indices, 1);
    }

    public boolean updateNeighborCells(Integer cellIndex) {
        return updateNeighborCells(cellIndex, 1);
    }

    private boolean updateNeighborCells(Integer cellIndex, Integer iteration) {
        List<Integer> indices = new ArrayList<>();
        boolean updateOutputs = false;

        Integer adjacentIndex = getAdjacentIndex(cellIndex, Direction.NORTH);
        if (adjacentIndex == null) {
            updateOutputs = true;
            updateNeighborTileCell(Direction.NORTH, cellIndex);
        } else
            indices.add(adjacentIndex);

        adjacentIndex = getAdjacentIndex(cellIndex, Direction.EAST);
        if (adjacentIndex == null) {
            updateOutputs = true;
            updateNeighborTileCell(Direction.EAST, cellIndex);
        } else
            indices.add(adjacentIndex);

        adjacentIndex = getAdjacentIndex(cellIndex, Direction.SOUTH);
        if (adjacentIndex == null) {
            updateOutputs = true;
            updateNeighborTileCell(Direction.SOUTH, cellIndex);
        } else
            indices.add(adjacentIndex);

        adjacentIndex = getAdjacentIndex(cellIndex, Direction.WEST);
        if (adjacentIndex == null) {
            updateOutputs = true;
            updateNeighborTileCell(Direction.WEST, cellIndex);
        } else
            indices.add(adjacentIndex);

        if (updateOutputs) {
            if (updateOutputs())
                world.notifyNeighborsOfStateChange(pos, this.getBlockState().getBlock());
        }

        return updateCells(indices, iteration) || updateOutputs;
    }

    /**
     * Update a cell with a potential input change
     *
     * @param indices
     * @return true if this update caused the panel output to change
     */
    public boolean updateCells(List<Integer> indices, int iteration) {
        //row 0 is west
        //column 0 is north

        boolean change = false;
        if (iteration > 63) {
            TinyRedstone.LOGGER.warn("Redstone panel iterated too many times.");
            return false;
        }

        for (Integer index : indices) {
            IPanelCell thisCell = cells.get(index);

            if (thisCell != null && !thisCell.isIndependentState()) {

                Direction frontDirection = getPanelSideDirection(index, IPanelCell.PanelCellSide.FRONT);
                Direction backDirection = getPanelSideDirection(index, IPanelCell.PanelCellSide.BACK);
                Direction rightDirection = getPanelSideDirection(index, IPanelCell.PanelCellSide.RIGHT);
                Direction leftDirection = getPanelSideDirection(index, IPanelCell.PanelCellSide.LEFT);

                PanelCellNeighbor front = getNeighbor(frontDirection, index);
                PanelCellNeighbor back = getNeighbor(backDirection, index);
                PanelCellNeighbor right = getNeighbor(rightDirection, index);
                PanelCellNeighbor left = getNeighbor(leftDirection, index);

                if (thisCell.neighborChanged(front,right,back,left)) {
                    updateNeighborCells(index, iteration + 1);
                    change=true;
                }
            }

        }
        if (change)
            this.markDirty();
        return change;
    }

    /**
     * Gets the weak and strong redstone output of the neighboring cell or block.
     * If the neighbor block is also a PanelTile, it queries for the adjacent cell.
     * If it is any other block, it uses the cached value queried from the facing side of the block
     *
     * @param facing         Direction of the adjacent cell or block relative to this cell.
     * @param localCellIndex Index of the panel cell on this panel.
     * @return an Integer array of size 2. First element is weak power. Second is strong power.
     */
    private PanelCellNeighbor getNeighbor(Direction facing, Integer localCellIndex) {
        //row 0 is west
        //column 0 is north

        int row = Math.round((localCellIndex.floatValue() / 8f) - 0.5f);
        int cell = localCellIndex % 8;

        IPanelCell localCell = this.cells.get(localCellIndex);
        Integer neighborIndex = getAdjacentIndex(localCellIndex, facing);

        if (this.cells.containsKey(neighborIndex)) {

            IPanelCell neighborCell = this.cells.get(neighborIndex);
            IPanelCell.PanelCellSide panelCellSide = getPanelCellSide(neighborIndex, facing.getOpposite());
            PanelCellNeighbor panelCellNeighbor = new PanelCellNeighbor(this,neighborCell,panelCellSide);
            return panelCellNeighbor;

        } else if (cellPanelEdge(localCellIndex,facing)) {
            TileEntity te = world.getTileEntity(pos.offset(facing));
            if (te instanceof PanelTile) {
                PanelTile neighborTile = ((PanelTile) te);
                int neighborRow, neighborCell;
                if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                    neighborRow = row;
                    neighborCell = (cell - 7) * -1;
                } else { //EAST & WEST
                    neighborRow = (row - 7) * -1;
                    neighborCell = cell;
                }
                int neighborCellIndex = (neighborRow * 8) + neighborCell;
                IPanelCell neighborPanelCell = neighborTile.cells.get(neighborCellIndex);
                IPanelCell.PanelCellSide neighborFacing = neighborTile.getPanelCellSide(neighborCellIndex, facing.getOpposite());
                if (neighborFacing != null) {
                    return new PanelCellNeighbor(this,neighborPanelCell,neighborFacing);
                }
                return null;
            } else {
                BlockPos blockPos = this.pos.offset(facing);
                return new PanelCellNeighbor(this,blockPos,facing);
            }
        }

        return null;
    }

    private void updateNeighborTileCell(Direction facing, Integer localCellIndex) {
        //row 0 is west
        //column 0 is north

        int row = Math.round((localCellIndex.floatValue() / 8f) - 0.5f);
        int cell = localCellIndex % 8;

        TileEntity te = world.getTileEntity(pos.offset(facing));
        if (te instanceof PanelTile) {
            PanelTile neighborTile = ((PanelTile) te);
            int neighborRow, neighborCell;
            if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                neighborRow = row;
                neighborCell = (cell - 7) * -1;
            } else {
                neighborRow = (row - 7) * -1;
                neighborCell = cell;
            }
            int neighborCellIndex = (neighborRow * 8) + neighborCell;
            neighborTile.updateCell(neighborCellIndex);
        }
    }

    private IPanelCell.PanelCellSide getPanelCellSide(int cellIndex, Direction facing) {
        Direction cellDirection = cellDirections.get(cellIndex);
        if (cellDirection == null) return null;

        if (cellDirection == facing)
            return IPanelCell.PanelCellSide.FRONT;
        if (cellDirection == facing.getOpposite())
            return IPanelCell.PanelCellSide.BACK;
        if (cellDirection.rotateY() == facing)
            return IPanelCell.PanelCellSide.RIGHT;
        if (cellDirection.rotateYCCW() == facing)
            return IPanelCell.PanelCellSide.LEFT;

        return null;
    }

    private Direction getPanelSideDirection(int cellIndex, IPanelCell.PanelCellSide side) {
        Direction cellDirection = cellDirections.get(cellIndex);
        if (cellDirection == null) return null;

        if (side == IPanelCell.PanelCellSide.FRONT)
            return cellDirection;
        if (side == IPanelCell.PanelCellSide.BACK)
            return cellDirection.getOpposite();
        if (side == IPanelCell.PanelCellSide.RIGHT)
            return cellDirection.rotateY();
        if (side == IPanelCell.PanelCellSide.LEFT)
            return cellDirection.rotateYCCW();

        return null;
    }

    private IPanelCell getAdjacentCell(Integer cellIndex, Direction direction) {
        Integer index = getAdjacentIndex(cellIndex, direction);
        if (index == null) {
            return null;
        } else {
            return cells.get(index);
        }

    }

    /**
     * Gets the index of the cell adjacent to the passed index in the specified direction.
     * Returns null if the index is off this tile.
     *
     * @param cellIndex
     * @param direction
     * @return
     */
    private Integer getAdjacentIndex(Integer cellIndex, Direction direction) {
        //row 0 is west
        //cell 0 is north
        int row = Math.round((cellIndex.floatValue() / 8f) - 0.5f);
        int cell = cellIndex % 8;

        if (direction == Direction.NORTH) {
            if (cell <= 0) {
                return null;
            } else {
                return cellIndex - 1;
            }
        }
        if (direction == Direction.SOUTH) {
            if (cell >= 7) {
                return null;
            } else {
                return cellIndex + 1;
            }
        }
        if (direction == Direction.WEST) {
            if (row <= 0) {
                return null;
            } else {
                return cellIndex - 8;
            }
        }
        if (direction == Direction.EAST) {
            if (row >= 7) {
                return null;
            } else {
                return cellIndex + 8;
            }
        }

        return null;

    }

    private boolean cellPanelEdge(Integer cellIndex, Direction facing) {
        //row 0 is west
        //cell 0 is north
        int row = Math.round((cellIndex.floatValue() / 8f) - 0.5f);
        int cell = cellIndex % 8;

        if (row == 0 && facing == Direction.WEST)
            return true;
        if (row == 7 && facing == Direction.EAST)
            return true;
        if (cell == 0 && facing == Direction.NORTH)
            return true;
        if (cell == 7 && facing == Direction.SOUTH)
            return true;

        return false;
    }

    /**
     * Recalculate the outputs of the tile. Will sync to client and return true if any output changes
     * @return boolean true if any outputs have changed
     */
    public boolean updateOutputs() {
        boolean change = false;
        int weak = 0, strong = 0;

        //check edge cells
        for (Direction direction : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            weak=0;strong=0;
            List<Integer> indices = getEdgeCellIndices(direction);
            for (int i:indices) {
                IPanelCell cell = cells.get(i);
                IPanelCell.PanelCellSide side = getPanelCellSide(i, direction);
                int cellStrongOutput = cell.getStrongRsOutput(side);
                int cellWeakOutput = cell.getWeakRsOutput(side);

                if (cell.powerDrops() && world.getBlockState(pos.offset(direction)).getBlock() == Blocks.REDSTONE_WIRE) {
                    cellStrongOutput -= 1;
                    cellWeakOutput -= 1;
                }

                if (cellStrongOutput > strong) {
                    strong = cellStrongOutput;
                }
                if (cellWeakOutput > weak) {
                    weak = cellWeakOutput;
                }
            }

            if (strongPowerToNeighbors.get(direction) == null || strong != strongPowerToNeighbors.get(direction) ||
                    weakPowerToNeighbors.get(direction) == null || weak != weakPowerToNeighbors.get(direction)) {
                change = true;
                strongPowerToNeighbors.put(direction, strong);
                weakPowerToNeighbors.put(direction, weak);
            }

        }

        if (change) sync();

        return change;
    }

    private List<Integer> getEdgeCellIndices(Direction edge)
    {
        List<Integer> cellIndices = new ArrayList<>();
        //row 0 is west
        //cell 0 is north
        if (edge==Direction.NORTH)
        {
            for (int i = 0; i < 64; i += 8) {
                if (cells.containsKey(i)) {
                    cellIndices.add(i);
                }
            }

        }
        else if (edge==Direction.EAST)
        {
            for (int i = 56; i < 64; i++)
            {
                if (cells.containsKey(i))
                {
                    cellIndices.add(i);
                }
            }
        }
        else if (edge==Direction.SOUTH)
        {
            for (int i = 7; i < 64; i += 8)
            {
                if (cells.containsKey(i))
                {
                    cellIndices.add(i);
                }
            }
        }
        else if (edge==Direction.WEST)
        {
            for (int i = 0; i < 8; i ++)
            {
                if (cells.containsKey(i))
                {
                    cellIndices.add(i);
                }
            }
        }
        return cellIndices;
    }


    public int getColor() {
        if (this.Color == null) {
            return DyeColor.GRAY.getColorValue();
        }
        return this.Color;
    }

    //TODO Import/Export
    public String getJson()
    {
        CompoundNBT nbt = new CompoundNBT();
        this.saveToNbt(nbt);
        ITextComponent textComponent = nbt.toFormattedComponent();

        return textComponent.getString();
    }

    public void sync()
    {
        this.world.notifyBlockUpdate(pos,this.getBlockState(),this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

}



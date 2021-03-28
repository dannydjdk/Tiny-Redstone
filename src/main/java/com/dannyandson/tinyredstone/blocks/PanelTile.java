package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.panelcells.Piston;
import com.dannyandson.tinyredstone.blocks.panelcells.StickyPiston;
import com.dannyandson.tinyredstone.blocks.panelcells.TinyBlock;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PanelTile extends TileEntity implements ITickableTileEntity {

    //TODO
    // add-on - gates, clock
    // One probe support
    // troubleshoot issue with powered neighbor blocks not updating their neighbors

    //panel data (saved)
    public Map<Integer, IPanelCell> cells = new HashMap<>();
    public Map<Integer, Direction> cellDirections = new HashMap<>();
    public Map<Direction, Integer> weakPowerFromNeighbors = new HashMap<>();
    public Map<Direction, Integer> strongPowerFromNeighbors = new HashMap<>();
    public Map<Direction, Integer> strongPowerToNeighbors = new HashMap<>();
    public Map<Direction, Integer> weakPowerToNeighbors = new HashMap<>();
    public Map<Direction, Integer> comparatorOverrides = new HashMap<>();
    public Integer Color = DyeColor.GRAY.getColorValue();
    private Integer lightOutput = 0;
    protected boolean flagLightUpdate = false;
    private boolean flagCrashed = false;
    protected IPanelCover panelCover = null;

    //other state fields (not saved)
    private boolean flagSync = false;
    public Integer lookingAtCell = null;
    public Direction lookingAtDirection = null;
    public IPanelCell lookingAtWith = null;


    public PanelTile() {
        super(Registration.REDSTONE_PANEL_TILE.get());
    }

    /* When the world loads from disk, the server needs to send the TileEntity information to the client
    //  it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this:
    //  getUpdatePacket() and onDataPacket() are used for one-at-a-time TileEntity updates
    //  getUpdateTag() and handleUpdateTag() are used by vanilla to collate together into a single chunk update packet
    */

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        int tileEntityType = -1;  // arbitrary number; only used for vanilla TileEntities.
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
        if (panelCover!=null)
            compoundNBT.putString("cover",panelCover.getClass().getCanonicalName());

        return compoundNBT;
    }

    /* This is where you save any data that you don't want to lose when the tile entity unloads*/
    @Override
    public CompoundNBT write(CompoundNBT parentNBTTagCompound) {
        try {
            if (this.strongPowerFromNeighbors.size()==4) {
                CompoundNBT strongPowerFromNeighbors = new CompoundNBT();
                strongPowerFromNeighbors.putInt(Direction.NORTH.getIndex() + "", this.strongPowerFromNeighbors.get(Direction.NORTH));
                strongPowerFromNeighbors.putInt(Direction.SOUTH.getIndex() + "", this.strongPowerFromNeighbors.get(Direction.SOUTH));
                strongPowerFromNeighbors.putInt(Direction.EAST.getIndex() + "", this.strongPowerFromNeighbors.get(Direction.EAST));
                strongPowerFromNeighbors.putInt(Direction.WEST.getIndex() + "", this.strongPowerFromNeighbors.get(Direction.WEST));
                parentNBTTagCompound.put("strong_power_incoming", strongPowerFromNeighbors);
            }
            if (this.weakPowerFromNeighbors.size()==4) {
                CompoundNBT weakPowerFromNeighbors = new CompoundNBT();
                weakPowerFromNeighbors.putInt(Direction.NORTH.getIndex() + "", this.weakPowerFromNeighbors.get(Direction.NORTH));
                weakPowerFromNeighbors.putInt(Direction.SOUTH.getIndex() + "", this.weakPowerFromNeighbors.get(Direction.SOUTH));
                weakPowerFromNeighbors.putInt(Direction.EAST.getIndex() + "", this.weakPowerFromNeighbors.get(Direction.EAST));
                weakPowerFromNeighbors.putInt(Direction.WEST.getIndex() + "", this.weakPowerFromNeighbors.get(Direction.WEST));
                parentNBTTagCompound.put("weak_power_incoming", weakPowerFromNeighbors);
            }
            if (this.strongPowerToNeighbors.size()==4) {
                CompoundNBT strongPowerToNeighbors = new CompoundNBT();
                strongPowerToNeighbors.putInt(Direction.NORTH.getIndex() + "", this.strongPowerToNeighbors.get(Direction.NORTH));
                strongPowerToNeighbors.putInt(Direction.SOUTH.getIndex() + "", this.strongPowerToNeighbors.get(Direction.SOUTH));
                strongPowerToNeighbors.putInt(Direction.EAST.getIndex() + "", this.strongPowerToNeighbors.get(Direction.EAST));
                strongPowerToNeighbors.putInt(Direction.WEST.getIndex() + "", this.strongPowerToNeighbors.get(Direction.WEST));
                parentNBTTagCompound.put("strong_power_outgoing", strongPowerToNeighbors);
            }
            if (this.weakPowerToNeighbors.size()==4) {
                CompoundNBT weakPowerToNeighbors = new CompoundNBT();
                weakPowerToNeighbors.putInt(Direction.NORTH.getIndex() + "", this.weakPowerToNeighbors.get(Direction.NORTH));
                weakPowerToNeighbors.putInt(Direction.SOUTH.getIndex() + "", this.weakPowerToNeighbors.get(Direction.SOUTH));
                weakPowerToNeighbors.putInt(Direction.EAST.getIndex() + "", this.weakPowerToNeighbors.get(Direction.EAST));
                weakPowerToNeighbors.putInt(Direction.WEST.getIndex() + "", this.weakPowerToNeighbors.get(Direction.WEST));
                parentNBTTagCompound.put("weak_power_outgoing", weakPowerToNeighbors);
            }

            CompoundNBT comparatorOverrideNBT = new CompoundNBT();
            for(Direction cDirection : comparatorOverrides.keySet())
            {
                comparatorOverrideNBT.putInt(cDirection.getIndex()+"",comparatorOverrides.get(cDirection));
            }
            parentNBTTagCompound.put("comparator_overrides",comparatorOverrideNBT);

            parentNBTTagCompound.putInt("lightOutput",this.lightOutput);
            parentNBTTagCompound.putBoolean("flagLightUpdate",this.flagLightUpdate);
            parentNBTTagCompound.putBoolean("flagCrashed",this.flagCrashed);

        } catch (NullPointerException exception) {
            TinyRedstone.LOGGER.error("Exception thrown when attempting to save power inputs and outputs: " + exception.toString() + ((exception.getStackTrace().length>0)?exception.getStackTrace()[0].toString():""));
        }

        return super.write(this.saveToNbt(parentNBTTagCompound));
    }

    // This is where you load the data that you saved in writeToNBT
    @Override
    public void read(BlockState blockState, CompoundNBT parentNBTTagCompound) {
        super.read(blockState, parentNBTTagCompound);

        // important rule: never trust the data you read from NBT, make sure it can't cause a crash

        this.loadCellsFromNBT(parentNBTTagCompound);

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

        this.lightOutput = parentNBTTagCompound.getInt("lightOutput");
        this.flagLightUpdate = parentNBTTagCompound.getBoolean("flagLightUpdate");
        this.flagCrashed = parentNBTTagCompound.getBoolean("flagCrashed");

        int color = parentNBTTagCompound.getInt("color");
        if (this.Color != color) {
            this.Color = color;
            this.flagSync=true;
        }

        String coverClass = parentNBTTagCompound.getString("cover");
        if (coverClass.length()>0)
        {
            try {
                panelCover= (IPanelCover) Class.forName(coverClass).getConstructor().newInstance();
            } catch (Exception exception) {
                TinyRedstone.LOGGER.error("Exception attempting to construct IPanelCover class " + coverClass +
                        ": " + exception.getMessage() + " " + ((exception.getStackTrace().length>0)?exception.getStackTrace()[0].toString():""));
            }
        }
        else
        {
            panelCover=null;
        }

    }

    public void loadCellsFromNBT(CompoundNBT parentNBTTagCompound)
    {
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
                            ": " + exception.getMessage() + " " + ((exception.getStackTrace().length>0)?exception.getStackTrace()[0].toString():""));
                }
            }
        }

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

        try {
            if (!flagCrashed) {
                boolean dirty = false;
                //if we have a neighbor with a comparator override (outputs through comparator), check for change
                if (!this.world.isRemote)
                    for (Direction direction : comparatorOverrides.keySet()) {
                        BlockPos neighborPos = pos.offset(direction);
                        BlockState neighborState = world.getBlockState(neighborPos);
                        if (neighborState.hasComparatorInputOverride()) {
                            int comparatorInputOverride = neighborState.getComparatorInputOverride(world, pos.offset(direction));
                            if (comparatorInputOverride != comparatorOverrides.get(direction)) {
                                this.comparatorOverrides.put(direction, comparatorInputOverride);
                                updateSide(direction);
                                dirty = true;
                            }
                        } else {
                            comparatorOverrides.remove(direction);
                            if (updateSide(direction)) {
                                dirty = true;
                            }
                        }

                    }

                List<Integer> pistons = null;
                //call the tick() method in all our cells
                for (Integer index : this.cells.keySet()) {
                    IPanelCell panelCell = this.cells.get(index);
                    boolean update = panelCell.tick();
                    if (update) {
                        if (panelCell instanceof Piston) {
                            if (pistons == null)
                                pistons = new ArrayList<>();
                            pistons.add(index);
                        } else {
                            updateNeighborCells(index);
                        }
                        dirty = true;
                    }

                }

                if (pistons != null) {
                    for (Integer index : pistons) {
                        updatePiston(index);
                    }
                }

                if (this.flagLightUpdate) {
                    this.flagLightUpdate = false;
                    //this.world.getLightManager().checkBlock(pos);
                    BlockState blockState = getBlockState();
                    Block block = blockState.getBlock();
                    if(block instanceof PanelBlock) {
                        ((PanelBlock) block).changeLightValue(world, pos, blockState, getLightOutput());
                    }
                }


                if (dirty || flagSync) {
                    markDirty();
                    updateOutputs();
                    sync();
                    flagSync = false;
                }

                if (world.isRemote) {
                    Integer lookingAtCell=null;
                    ClientPlayerEntity player = Minecraft.getInstance().player;
                    if (player!=null && PanelBlock.isPanelCellItem(player.getHeldItemMainhand().getItem())) {
                        RayTraceResult lookingAt = Minecraft.getInstance().objectMouseOver;
                        if (lookingAt != null && lookingAt.getType() == RayTraceResult.Type.BLOCK && ((BlockRayTraceResult)lookingAt).getFace() == Direction.UP) {
                            BlockPos blockPos = new BlockPos(lookingAt.getHitVec());
                            TileEntity te = world.getTileEntity(blockPos);
                            if (te == this) {
                                double x = lookingAt.getHitVec().x - pos.getX();
                                double z = lookingAt.getHitVec().z - pos.getZ();
                                int row = Math.round((float) (x * 8f) - 0.5f);
                                int cell = Math.round((float) (z * 8f) - 0.5f);
                                int cellIndex = (row * 8) + cell;
                                if (!this.cells.containsKey(cellIndex)) {
                                    lookingAtCell = (row * 8) + cell;
                                    this.lookingAtDirection = player.getHorizontalFacing();
                                    try {
                                        this.lookingAtWith = (IPanelCell) PanelBlock.getPanelCellClassFromItem(player.getHeldItemMainhand().getItem()).getConstructors()[0].newInstance();
                                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                        TinyRedstone.LOGGER.error("Exception thrown when attempting to draw ghost cell: " + e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                    this.lookingAtCell = lookingAtCell;
                }
            }
        }catch(Exception e)
        {
            this.handleCrash(e);
        }

    }

    private void updatePiston(int index) {
        IPanelCell panelCell = cells.get(index);
        if (panelCell instanceof Piston) {

            PanelTile panelTile = this;
            Direction moveDirection = getPanelSideDirection(index, IPanelCell.PanelCellSide.BACK);
            Integer moverIndex = getAdjacentIndex(index, moveDirection);
            if (moverIndex == null) {
                TileEntity te = world.getTileEntity(pos.offset(moveDirection));
                if (te instanceof PanelTile) {
                    panelTile = (PanelTile) te;
                    moverIndex = getNeighborTileCellIndex(moveDirection, index);
                }
            }
            if (!((Piston) panelCell).isExtended() && panelCell instanceof StickyPiston) {
                Integer adjacentIndex = moverIndex;
                moverIndex = getAdjacentIndex(moverIndex, moveDirection);
                if (moverIndex == null) {
                    TileEntity te = world.getTileEntity(pos.offset(moveDirection));
                    if (te instanceof PanelTile) {
                        panelTile = (PanelTile) te;
                        moverIndex = getNeighborTileCellIndex(moveDirection, adjacentIndex);
                    }
                }
                moveDirection = moveDirection.getOpposite();
            }

            world.playSound(
                    pos.getX(), pos.getY(), pos.getZ(),
                    (((Piston) panelCell).isExtended()) ? SoundEvents.BLOCK_PISTON_EXTEND : SoundEvents.BLOCK_PISTON_CONTRACT,
                    SoundCategory.BLOCKS, 0.25f, 2f, false
            );

            if (moverIndex != null) {
                if (panelTile.cells.containsKey(moverIndex))
                    panelTile.moveCell(moverIndex, moveDirection, 0);
                else {
                    Integer adjacentindex = getAdjacentIndex(index, getPanelSideDirection(index, IPanelCell.PanelCellSide.BACK));
                    if (adjacentindex!=null)
                        panelTile.updateNeighborCells(adjacentindex);
                }
            }
        }
    }

    private void moveCell(int index, Direction direction, Integer iteration)
    {
        if (iteration>12)return;

        if(cells.containsKey(index) && cells.get(index).isPushable())
        {
            IPanelCell cell = cells.get(index);
            Integer newindex = getAdjacentIndex(index, direction);
            PanelTile panelTile = this;
            if (newindex == null) {
                //newindex is null, so we are on the edge of the block
                TileEntity te = world.getTileEntity(pos.offset(direction));
                if (te instanceof PanelTile) {
                    panelTile = (PanelTile) te;
                    newindex = this.getNeighborTileCellIndex(direction, index);
                }
            }

            if (newindex != null) {
                if (cell != null) {
                    if (panelTile.cells.containsKey(newindex))
                    {
                        panelTile.moveCell(newindex,direction,iteration+1);
                    }
                    panelTile.cells.put(newindex, cell);
                    this.cells.remove(index);
                    panelTile.cellDirections.put(newindex, this.cellDirections.get(index));
                    this.cellDirections.remove(index);
                    this.updateNeighborCells(index);
                    panelTile.updateNeighborCells(newindex);
                    panelTile.markDirty();
                }
            }
        }

    }

    public boolean canExtendTo(Integer index, Direction direction, Integer iteration)
    {
        if (iteration>12)return false;

        IPanelCell iPanelCell = this.cells.get(index);

        //check if a piston has extended into this block
        if (checkCellForPistonExtension(index))
            return false;

        //if this is an empty cell, we know it can be pushed into
        if (iPanelCell==null)
            return true;

        //if this is not a pushable cell, we know we can't extend into it
        if(!iPanelCell.isPushable())
            return false;

        //otherwise, check if we're on the edge of a tile
        Integer adjacentIndex = this.getAdjacentIndex(index,direction);
        if (adjacentIndex==null)
        {
            TileEntity te = world.getTileEntity(pos.offset(direction));
            if (te instanceof PanelTile)
            {
                //if we're next to another panel tile, tell it to check it's adjacent cell
                PanelTile neighborTile = (PanelTile) te;
                Integer neighborIndex = getNeighborTileCellIndex(direction,index);
                return neighborTile.canExtendTo(neighborIndex,direction,iteration+1);
            }
            else //we must be at the edge of the tile and facing some other type of block, so we can't extend
                return false;
        }
        else {
            //we can be pushed and are not on the edge, so check the next cell and see if it can also be pushed
            return canExtendTo(adjacentIndex,direction,iteration+1);
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

        updateOutputs();

        sync();

    }


    public boolean pingOutwardObservers(Direction facing) {
        //row 0 is west
        //cell 0 is north
        boolean updated = false;

        if (facing == Direction.WEST) {
            for (int i = 0; i < 8; i++) {
                if (cells.containsKey(i) && cells.get(i) instanceof IObservingPanelCell && cellDirections.get(i)==facing) {
                    if(((IObservingPanelCell)cells.get(i)).frontNeighborUpdated())
                        updated=true;
                }
            }
        } else if (facing == Direction.NORTH) {
            for (int i = 0; i < 64; i += 8) {
                if (cells.containsKey(i) && cells.get(i) instanceof IObservingPanelCell && cellDirections.get(i)==facing) {
                    if(((IObservingPanelCell)cells.get(i)).frontNeighborUpdated())
                        updated=true;
                }
            }
        } else if (facing == Direction.EAST) {
            for (int i = 56; i < 64; i++) {
                if (cells.containsKey(i) && cells.get(i) instanceof IObservingPanelCell && cellDirections.get(i)==facing) {
                    if(((IObservingPanelCell)cells.get(i)).frontNeighborUpdated())
                        updated=true;
                }
            }
        } else if (facing == Direction.SOUTH) {
            for (int i = 7; i < 64; i += 8) {
                if (cells.containsKey(i) && cells.get(i) instanceof IObservingPanelCell && cellDirections.get(i)==facing) {
                    if(((IObservingPanelCell)cells.get(i)).frontNeighborUpdated())
                        updated=true;
                }
            }
        }


        return updated;
    }

    public boolean updateSide(Direction facing) {
        //row 0 is west
        //cell 0 is north
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

        if (updateNeighbor(cellIndex,Direction.NORTH,indices))
            updateOutputs=true;
        if (updateNeighbor(cellIndex,Direction.EAST,indices))
            updateOutputs=true;
        if (updateNeighbor(cellIndex,Direction.SOUTH,indices))
            updateOutputs=true;
        if (updateNeighbor(cellIndex,Direction.WEST,indices))
            updateOutputs=true;


        if (updateOutputs) {
            updateOutputs();
        }

        return updateCells(indices, iteration+1) || updateOutputs;
    }

    private boolean updateNeighbor(Integer cellIndex, Direction direction, List<Integer> indices)
    {
        Integer adjacentIndex = getAdjacentIndex(cellIndex, direction);
        boolean updateOutputs = false;
        if (adjacentIndex == null) {
            updateOutputs = true;
            updateNeighborTileCell(direction, cellIndex);
        } else {
            IPanelCell adjacentCell = cells.get(adjacentIndex);
            if (adjacentCell instanceof IObservingPanelCell) {
                Direction direction1 = cellDirections.get(adjacentIndex);
                Direction direction2 = direction.getOpposite();
                if (direction1 == direction2) {
                    ((IObservingPanelCell) adjacentCell).frontNeighborUpdated();
                }
            }
            else if (adjacentCell!=null && !adjacentCell.isIndependentState())
                indices.add(adjacentIndex);
        }
        return updateOutputs;
    }

    public boolean checkCellForPistonExtension(Integer cellIndex) {
        Direction[] directions = new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};

        for(Direction direction : directions)
        {
            IPanelCell cell = getAdjacentCell(cellIndex,direction);
            if (cell instanceof Piston && getAdjacentCellDirection(cellIndex,direction)==direction && ((Piston) cell).isExtended())
                return true;
        }
        return false;
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

            if (thisCell != null) {
                if (!thisCell.isIndependentState()) {

                    Direction frontDirection = getPanelSideDirection(index, IPanelCell.PanelCellSide.FRONT);
                    Direction backDirection = getPanelSideDirection(index, IPanelCell.PanelCellSide.BACK);
                    Direction rightDirection = getPanelSideDirection(index, IPanelCell.PanelCellSide.RIGHT);
                    Direction leftDirection = getPanelSideDirection(index, IPanelCell.PanelCellSide.LEFT);

                    PanelCellNeighbor front = getNeighbor(frontDirection, index);
                    PanelCellNeighbor back = getNeighbor(backDirection, index);
                    PanelCellNeighbor right = getNeighbor(rightDirection, index);
                    PanelCellNeighbor left = getNeighbor(leftDirection, index);

                    if (thisCell.neighborChanged(front, right, back, left)) {
                        updateNeighborCells(index, iteration + 1);
                        change = true;
                    }
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

        Integer neighborIndex = getAdjacentIndex(localCellIndex, facing);

        if (neighborIndex!=null) {
            PanelCellNeighbor panelCellNeighbor=null;
            if (this.cells.containsKey(neighborIndex)) {
                IPanelCell neighborCell = this.cells.get(neighborIndex);
                IPanelCell.PanelCellSide panelCellSide = getPanelCellSide(neighborIndex, facing.getOpposite());
                panelCellNeighbor = new PanelCellNeighbor(this, neighborCell, panelCellSide, facing, neighborIndex);
            }
            else if(checkCellForPistonExtension(neighborIndex))
            {
                panelCellNeighbor = new PanelCellNeighbor(this,null, null, facing, neighborIndex);
            }

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
                    return new PanelCellNeighbor(neighborTile,neighborPanelCell,neighborFacing,facing, neighborCellIndex);
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

        TileEntity te = world.getTileEntity(pos.offset(facing));
        if (te instanceof PanelTile) {
            PanelTile neighborTile = ((PanelTile) te);
            int neighborCellIndex = getNeighborTileCellIndex(facing,localCellIndex);
            neighborTile.updateCell(neighborCellIndex);
        }
    }

    private int getNeighborTileCellIndex(Direction facing, Integer localCellIndex)
    {
        int row = Math.round((localCellIndex.floatValue() / 8f) - 0.5f);
        int cell = localCellIndex % 8;
        int neighborRow, neighborCell;
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            neighborRow = row;
            neighborCell = (cell - 7) * -1;
        } else {
            neighborRow = (row - 7) * -1;
            neighborCell = cell;
        }
        return (neighborRow * 8) + neighborCell;
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
            TileEntity te = world.getTileEntity(pos.offset(direction));
            if (te instanceof PanelTile)
                return ((PanelTile) te).cells.get(getNeighborTileCellIndex(direction,cellIndex));
            return null;
        } else {
            return cells.get(index);
        }

    }

    private Direction getAdjacentCellDirection(Integer cellIndex, Direction direction) {
        Integer index = getAdjacentIndex(cellIndex, direction);
        if (index == null) {
            TileEntity te = world.getTileEntity(pos.offset(direction));
            if (te instanceof PanelTile)
                return ((PanelTile) te).cellDirections.get(getNeighborTileCellIndex(direction,cellIndex));
            return null;
        } else {
            return cellDirections.get(index);
        }

    }

    /**
     * Gets the index of the cell adjacent to the passed index in the specified direction.
     * Returns null if the index is off this tile.
     *
     * @param cellIndex The index of the cell whose neighbor you want to find
     * @param direction The direction of the neighbor from the cell.
     * @return integer representing the neighbor's cell index, or null if we're at the edge of the panel
     */
    private Integer getAdjacentIndex(Integer cellIndex, Direction direction) {
        //row 0 is west
        //cell 0 is north

        if (cellIndex==null) return null;

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
        int weak, strong;

        List<Direction> directionsUpdated = new ArrayList<>();

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

                if (cell instanceof TinyBlock && world.getBlockState(pos.offset(direction)).getBlock() == Blocks.REDSTONE_WIRE) {
                    cellWeakOutput = cellStrongOutput;
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
                directionsUpdated.add(direction);
            }

        }

        int ll = 0;
        for(Integer index : cells.keySet())
        {
            ll+=cells.get(index).lightOutput();
        }
        if (ll!=this.lightOutput) {
            this.lightOutput=ll;
            this.flagLightUpdate=true;
        }

        if (change)
        {
            sync();
            world.notifyNeighborsOfStateChange(pos,this.getBlockState().getBlock());
            for (Direction direction : directionsUpdated) {
                BlockPos neighborPos = pos.offset(direction);
                BlockState neighborBlockState = world.getBlockState(neighborPos);
                if (neighborBlockState!=null && neighborBlockState.isSolid())
                    world.notifyNeighborsOfStateExcept(neighborPos,neighborBlockState.getBlock(),direction.getOpposite());
            }
        }

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

    public int getLightOutput()
    {
        if(panelCover==null || panelCover.allowsLightOutput())
            return this.lightOutput;
        return 0;
    }

    public void sync()
    {
        if (!isCovered() || panelCover.allowsLightOutput())
            this.world.notifyBlockUpdate(pos,this.getBlockState(),this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    protected void handleCrash(Exception e)
    {
        this.flagCrashed=true;

        TinyRedstone.LOGGER.error("Redstone Panel Crashed at " + pos.getX() + "," + pos.getY() + "," + pos.getZ(),e);
    }

    public boolean isCrashed()
    {
        return this.flagCrashed;
    }
    public void resetCrashFlag()
    {
        this.flagCrashed=false;
    }

    public boolean isCovered()
    {
        return panelCover!=null;
    }

}



package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.panelcells.Piston;
import com.dannyandson.tinyredstone.blocks.panelcells.StickyPiston;
import com.dannyandson.tinyredstone.blocks.panelcells.TinyBlock;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.BlockStateProperties;
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
    public Map<Integer, Side> cellDirections = new HashMap<>();
    public Map<Side, Integer> weakPowerFromNeighbors = new HashMap<>();
    public Map<Side, Integer> strongPowerFromNeighbors = new HashMap<>();
    public Map<Side, Integer> strongPowerToNeighbors = new HashMap<>();
    public Map<Side, Integer> weakPowerToNeighbors = new HashMap<>();
    public Map<Side, Integer> comparatorOverrides = new HashMap<>();

    public Integer Color = DyeColor.GRAY.getColorValue();
    private Integer lightOutput = 0;
    protected boolean flagLightUpdate = false;
    private boolean flagCrashed = false;
    protected IPanelCover panelCover = null;

    //other state fields (not saved)
    private boolean flagSync = false;
    public Integer lookingAtCell = null;
    public Side lookingAtDirection = null;
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
            cellNBT.putString("facing", this.cellDirections.get(key).name());
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
                strongPowerFromNeighbors.putInt(Side.FRONT.ordinal() + "", this.strongPowerFromNeighbors.get(Side.FRONT));
                strongPowerFromNeighbors.putInt(Side.RIGHT.ordinal() + "", this.strongPowerFromNeighbors.get(Side.RIGHT));
                strongPowerFromNeighbors.putInt(Side.BACK.ordinal() + "", this.strongPowerFromNeighbors.get(Side.BACK));
                strongPowerFromNeighbors.putInt(Side.LEFT.ordinal() + "", this.strongPowerFromNeighbors.get(Side.LEFT));
                parentNBTTagCompound.put("strong_power_incoming", strongPowerFromNeighbors);
            }
            if (this.weakPowerFromNeighbors.size()==4) {
                CompoundNBT weakPowerFromNeighbors = new CompoundNBT();
                weakPowerFromNeighbors.putInt(Side.FRONT.ordinal() + "", this.weakPowerFromNeighbors.get(Side.FRONT));
                weakPowerFromNeighbors.putInt(Side.RIGHT.ordinal() + "", this.weakPowerFromNeighbors.get(Side.RIGHT));
                weakPowerFromNeighbors.putInt(Side.BACK.ordinal() + "",  this.weakPowerFromNeighbors.get(Side.BACK));
                weakPowerFromNeighbors.putInt(Side.LEFT.ordinal() + "",  this.weakPowerFromNeighbors.get(Side.LEFT));
                parentNBTTagCompound.put("weak_power_incoming", weakPowerFromNeighbors);
            }
            if (this.strongPowerToNeighbors.size()==4) {
                CompoundNBT strongPowerToNeighbors = new CompoundNBT();
                strongPowerToNeighbors.putInt(Side.FRONT.ordinal() + "", this.strongPowerToNeighbors.get(Side.FRONT));
                strongPowerToNeighbors.putInt(Side.RIGHT.ordinal() + "", this.strongPowerToNeighbors.get(Side.RIGHT));
                strongPowerToNeighbors.putInt(Side.BACK.ordinal() + "",  this.strongPowerToNeighbors.get(Side.BACK));
                strongPowerToNeighbors.putInt(Side.LEFT.ordinal() + "",  this.strongPowerToNeighbors.get(Side.LEFT));
                parentNBTTagCompound.put("strong_power_outgoing", strongPowerToNeighbors);
            }
            if (this.weakPowerToNeighbors.size()==4) {
                CompoundNBT weakPowerToNeighbors = new CompoundNBT();
                weakPowerToNeighbors.putInt(Side.FRONT.ordinal() + "", this.weakPowerToNeighbors.get(Side.FRONT));
                weakPowerToNeighbors.putInt(Side.RIGHT.ordinal() + "", this.weakPowerToNeighbors.get(Side.RIGHT));
                weakPowerToNeighbors.putInt(Side.BACK.ordinal() + "",  this.weakPowerToNeighbors.get(Side.BACK));
                weakPowerToNeighbors.putInt(Side.LEFT.ordinal() + "",  this.weakPowerToNeighbors.get(Side.LEFT));
                parentNBTTagCompound.put("weak_power_outgoing", weakPowerToNeighbors);
            }

            CompoundNBT comparatorOverrideNBT = new CompoundNBT();
            for(Side cDirection : comparatorOverrides.keySet())
            {
                comparatorOverrideNBT.putInt(cDirection.ordinal()+"",comparatorOverrides.get(cDirection));
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
            this.strongPowerFromNeighbors.put(Side.FRONT,   strongPowerFromNeighbors.getInt(Side.FRONT.ordinal() + ""));
            this.strongPowerFromNeighbors.put(Side.RIGHT,   strongPowerFromNeighbors.getInt(Side.RIGHT.ordinal() + ""));
            this.strongPowerFromNeighbors.put(Side.BACK,    strongPowerFromNeighbors.getInt(Side.BACK.ordinal() + ""));
            this.strongPowerFromNeighbors.put(Side.LEFT,    strongPowerFromNeighbors.getInt(Side.LEFT.ordinal() + ""));
        }
        CompoundNBT weakPowerFromNeighbors = parentNBTTagCompound.getCompound("weak_power_incoming");
        if (!weakPowerFromNeighbors.isEmpty()) {
            this.weakPowerFromNeighbors.put(Side.FRONT, weakPowerFromNeighbors.getInt(Side.FRONT.ordinal() + ""));
            this.weakPowerFromNeighbors.put(Side.RIGHT, weakPowerFromNeighbors.getInt(Side.RIGHT.ordinal() + ""));
            this.weakPowerFromNeighbors.put(Side.BACK,  weakPowerFromNeighbors.getInt(Side.BACK.ordinal()  + ""));
            this.weakPowerFromNeighbors.put(Side.LEFT,  weakPowerFromNeighbors.getInt(Side.LEFT.ordinal()  + ""));
        }

        CompoundNBT strongPowerToNeighbors = parentNBTTagCompound.getCompound("strong_power_outgoing");
        if (!strongPowerToNeighbors.isEmpty()) {
            this.strongPowerToNeighbors.put(Side.FRONT, strongPowerToNeighbors.getInt(Side.FRONT.ordinal() + ""));
            this.strongPowerToNeighbors.put(Side.RIGHT, strongPowerToNeighbors.getInt(Side.RIGHT.ordinal() + ""));
            this.strongPowerToNeighbors.put(Side.BACK,  strongPowerToNeighbors.getInt(Side.BACK.ordinal() + ""));
            this.strongPowerToNeighbors.put(Side.LEFT,  strongPowerToNeighbors.getInt(Side.LEFT.ordinal() + ""));
        }
        CompoundNBT weakPowerToNeighbors = parentNBTTagCompound.getCompound("weak_power_outgoing");
        if (!weakPowerToNeighbors.isEmpty()) {
            this.weakPowerToNeighbors.put(Side.FRONT, weakPowerToNeighbors.getInt(Side.FRONT.ordinal() + ""));
            this.weakPowerToNeighbors.put(Side.RIGHT, weakPowerToNeighbors.getInt(Side.RIGHT.ordinal() + ""));
            this.weakPowerToNeighbors.put(Side.BACK,  weakPowerToNeighbors.getInt(Side.BACK.ordinal() + ""));
            this.weakPowerToNeighbors.put(Side.LEFT,  weakPowerToNeighbors.getInt(Side.LEFT.ordinal() + ""));
        }

        CompoundNBT comparatorOverridesNBT = parentNBTTagCompound.getCompound("comparator_overrides");
        if (!comparatorOverridesNBT.isEmpty())
        {
            if (comparatorOverridesNBT.contains(Side.FRONT.ordinal()+""))
                this.comparatorOverrides.put(Side.FRONT,comparatorOverridesNBT.getInt(Side.FRONT.ordinal()+""));
            if (comparatorOverridesNBT.contains(Side.RIGHT.ordinal()+""))
                this.comparatorOverrides.put(Side.RIGHT,comparatorOverridesNBT.getInt(Side.RIGHT.ordinal()+""));
            if (comparatorOverridesNBT.contains(Side.BACK.ordinal()+""))
                this.comparatorOverrides.put(Side.BACK,comparatorOverridesNBT.getInt(Side.BACK.ordinal()+""));
            if (comparatorOverridesNBT.contains(Side.LEFT.ordinal()+""))
                this.comparatorOverrides.put(Side.LEFT,comparatorOverridesNBT.getInt(Side.LEFT.ordinal()+""));
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


                    if (cellNBT.contains("direction"))
                    {
                        //backward compatibility
                        Direction direction = Direction.byIndex(cellNBT.getInt("direction"));
                        if (direction==Direction.NORTH) this.cellDirections.put(i,Side.FRONT);
                        else if (direction==Direction.EAST) this.cellDirections.put(i,Side.RIGHT);
                        else if (direction==Direction.SOUTH) this.cellDirections.put(i,Side.BACK);
                        else if (direction==Direction.WEST) this.cellDirections.put(i,Side.LEFT);
                    }
                    else
                        this.cellDirections.put(i, Side.valueOf(cellNBT.getString("facing")));
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
                    for (Side side : comparatorOverrides.keySet()) {
                        Direction direction = getDirectionFromSide(side);
                        BlockPos neighborPos = pos.offset(direction);
                        BlockState neighborState = world.getBlockState(neighborPos);
                        if (neighborState.hasComparatorInputOverride()) {
                            int comparatorInputOverride = neighborState.getComparatorInputOverride(world, pos.offset(direction));
                            if (comparatorInputOverride != comparatorOverrides.get(direction)) {
                                this.comparatorOverrides.put(side, comparatorInputOverride);
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
                    this.world.getLightManager().checkBlock(pos);
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
                        if (lookingAt != null && lookingAt.getType() == RayTraceResult.Type.BLOCK && ((BlockRayTraceResult)lookingAt).getFace() == getBlockState().get(BlockStateProperties.FACING).getOpposite()) {
                            BlockPos blockPos = new BlockPos(lookingAt.getHitVec());
                            TileEntity te = world.getTileEntity(blockPos);
                            if (te == this) {
                                PosInPanelCell posInPanelCell =  PosInPanelCell.fromHitVec(this,pos,lookingAt.getHitVec());
                                if (!this.cells.containsKey(posInPanelCell.getIndex())) {
                                    lookingAtCell=posInPanelCell.getIndex();
                                    this.lookingAtDirection = getSideFromDirection(getPlayerDirectionFacing(player));
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
            Side movingToward = getPanelCellSide(index,Side.BACK);
            Direction moveDirection = getPanelSideDirection(index, movingToward);
            Integer moverIndex = getAdjacentIndex(index, movingToward);
            if (moverIndex == null) {
                TileEntity te = world.getTileEntity(pos.offset(moveDirection));
                if (te instanceof PanelTile) {
                    panelTile = (PanelTile) te;
                    moverIndex = getNeighborTileCellIndex(moveDirection, index);
                }
            }
            if (!((Piston) panelCell).isExtended() && panelCell instanceof StickyPiston) {
                Integer adjacentIndex = moverIndex;
                moverIndex = getAdjacentIndex(moverIndex, movingToward);
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
                    Integer adjacentindex = getAdjacentIndex(index, getPanelCellSide(index, Side.BACK));
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
            Integer newindex = getAdjacentIndex(index, getSideFromDirection(direction));
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

    public boolean canExtendTo(Integer index, Side side, Integer iteration)
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
        Integer adjacentIndex = this.getAdjacentIndex(index,side);
        if (adjacentIndex==null)
        {
            Direction direction = getDirectionFromSide(side);
            TileEntity te = world.getTileEntity(pos.offset(direction));
            if (te instanceof PanelTile)
            {
                //if we're next to another panel tile, tell it to check it's adjacent cell
                PanelTile neighborTile = (PanelTile) te;
                Integer neighborIndex = getNeighborTileCellIndex(direction,index);
                return neighborTile.canExtendTo(neighborIndex,side,iteration+1);
            }
            else //we must be at the edge of the tile and facing some other type of block, so we can't extend
                return false;
        }
        else {
            //we can be pushed and are not on the edge, so check the next cell and see if it can also be pushed
            return canExtendTo(adjacentIndex,side,iteration+1);
        }
    }

    @Override
    public void rotate(Rotation rotationIn) {


        Map<Integer, IPanelCell> cells = new HashMap<>();
        Map<Integer, Side> cellDirections = new HashMap<>();

        for (Integer i = 0; i < 64; i++) {
            if (this.cells.containsKey(i)) {
                PanelCellPos cellPos1 = PanelCellPos.fromIndex(i);
                PanelCellPos cellPos2;
                Side side1 = this.cellDirections.get(i);
                Side side2;

                //int row1 = Math.round((i.floatValue() / 8f) - 0.5f);
                //int cell1 = i % 8;
                //int row2, cell2;

                if (rotationIn == Rotation.COUNTERCLOCKWISE_90) {
                    cellPos2 = PanelCellPos.fromRowColumn(cellPos1.getColumn(),((cellPos1.getRow() - 4) * -1) + 3);
                    side2 = side1.rotateYCCW();
                    //row2 = cell1;
                    //cell2 = ((row1 - 4) * -1) + 3;
                } else if (rotationIn == Rotation.CLOCKWISE_180) {
                    cellPos2 = PanelCellPos.fromRowColumn(((cellPos1.getRow() - 4) * -1) + 3,((cellPos1.getColumn() - 4) * -1) + 3);
                    side2 = side1.getOpposite();
                    //row2 = ((row1 - 4) * -1) + 3;
                    //cell2 = ((cell1 - 4) * -1) + 3;
                } else {
                    //default rotation 90°
                    cellPos2 = PanelCellPos.fromRowColumn(((cellPos1.getColumn() - 4) * -1) + 3,cellPos1.getRow());
                    side2 = side1.rotateYCW();
                }

                cells.put(cellPos2.getIndex(), this.cells.get(i));
                cellDirections.put(cellPos2.getIndex(),side2);

            }
        }

        this.cells = cells;
        this.cellDirections = cellDirections;

        updateSide(Side.FRONT);
        updateSide(Side.RIGHT);
        updateSide(Side.BACK);
        updateSide(Side.LEFT);

        if (!world.isRemote)
            markDirty();

        updateOutputs();

        sync();

    }


    public boolean pingOutwardObservers(Direction facing) {

        boolean updated = false;
        Side side = getSideFromDirection(facing);

        if (side==Side.LEFT) {
            for (int i = 0; i < 8; i++) {
                if (cells.containsKey(i) && cells.get(i) instanceof IObservingPanelCell && cellDirections.get(i)==side) {
                    if(((IObservingPanelCell)cells.get(i)).frontNeighborUpdated())
                        updated=true;
                }
            }
        } else if (side==Side.FRONT) {
            for (int i = 0; i < 64; i += 8) {
                if (cells.containsKey(i) && cells.get(i) instanceof IObservingPanelCell && cellDirections.get(i)==side) {
                    if(((IObservingPanelCell)cells.get(i)).frontNeighborUpdated())
                        updated=true;
                }
            }
        } else if (side==Side.RIGHT) {
            for (int i = 56; i < 64; i++) {
                if (cells.containsKey(i) && cells.get(i) instanceof IObservingPanelCell && cellDirections.get(i)==side) {
                    if(((IObservingPanelCell)cells.get(i)).frontNeighborUpdated())
                        updated=true;
                }
            }
        } else if (side==Side.BACK) {
            for (int i = 7; i < 64; i += 8) {
                if (cells.containsKey(i) && cells.get(i) instanceof IObservingPanelCell && cellDirections.get(i)==side) {
                    if(((IObservingPanelCell)cells.get(i)).frontNeighborUpdated())
                        updated=true;
                }
            }
        }


        return updated;
    }

    public boolean updateSide(Direction facing) {
        return updateSide(getSideFromDirection(facing));
    }
    public boolean updateSide(Side side){
        List<Integer> cellIndices = new ArrayList<>();
        if (side==Side.LEFT) {
            for (int i = 0; i < 8; i++) {
                if (cells.containsKey(i)) {
                    cellIndices.add(i);
                }
            }
        } else if (side==Side.FRONT) {
            for (int i = 0; i < 64; i += 8) {
                if (cells.containsKey(i)) {
                    cellIndices.add(i);
                }
            }
        } else if (side==Side.RIGHT) {
            for (int i = 56; i < 64; i++) {
                if (cells.containsKey(i)) {
                    cellIndices.add(i);
                }
            }
        } else if (side==Side.BACK) {
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

        if (updateNeighbor(cellIndex,Side.FRONT,indices))
            updateOutputs=true;
        if (updateNeighbor(cellIndex,Side.RIGHT,indices))
            updateOutputs=true;
        if (updateNeighbor(cellIndex,Side.BACK,indices))
            updateOutputs=true;
        if (updateNeighbor(cellIndex,Side.LEFT,indices))
            updateOutputs=true;


        if (updateOutputs) {
            updateOutputs();
        }

        return updateCells(indices, iteration+1) || updateOutputs;
    }

    private boolean updateNeighbor(Integer cellIndex, Side side, List<Integer> indices)
    {
        Integer adjacentIndex = getAdjacentIndex(cellIndex, side);

        boolean updateOutputs = false;
        if (adjacentIndex == null) {
            updateOutputs = true;
            updateNeighborTileCell(getDirectionFromSide(side), cellIndex);
        } else {
            IPanelCell adjacentCell = cells.get(adjacentIndex);
            if (adjacentCell instanceof IObservingPanelCell) {
                Side direction1 = cellDirections.get(adjacentIndex);
                Side direction2 = side.getOpposite();
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
        for(Side panelSide : new Side[]{Side.FRONT,Side.RIGHT,Side.BACK,Side.LEFT})
        {
            IPanelCell cell = getAdjacentCell(cellIndex,panelSide);
            if (cell instanceof Piston && getAdjacentCellDirection(cellIndex,panelSide)==panelSide && ((Piston) cell).isExtended())
                return true;
        }
        return false;
    }


    /**
     * Update a cell with a potential input change
     *
     * @param indices list of cell indexes to update
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

                    Side frontDirection =  cellDirections.get(index);
                    Side backDirection = frontDirection.getOpposite();
                    Side rightDirection = frontDirection.rotateYCW();
                    Side leftDirection = frontDirection.rotateYCCW();

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
     * @param side Toward which side of the panel the adjacent cell or block is relative to this cell.
     * @param localCellIndex Index of the panel cell on this panel.
     * @return an Integer array of size 2. First element is weak power. Second is strong power.
     */
    private PanelCellNeighbor getNeighbor(Side side, Integer localCellIndex) {

        PanelCellPos localPos = PanelCellPos.fromIndex(localCellIndex);
        PanelCellPos neighborPos = localPos.offset(side);

        if (neighborPos!=null) {
            Integer neighborIndex = neighborPos.getIndex();
            PanelCellNeighbor panelCellNeighbor=null;

            if (this.cells.containsKey(neighborIndex)) {
                IPanelCell neighborCell = this.cells.get(neighborIndex);
                Side neighborSide = getPanelCellSide(neighborIndex, side.getOpposite());
                panelCellNeighbor = new PanelCellNeighbor(this, neighborCell, neighborSide, side, neighborIndex);
            }
            else if(checkCellForPistonExtension(neighborIndex))
            {
                panelCellNeighbor = new PanelCellNeighbor(this,null, null, side, neighborIndex);
            }

            return panelCellNeighbor;

        } else if (cellPanelEdge(localCellIndex,side)) {
            TileEntity te = world.getTileEntity(pos.offset(getDirectionFromSide(side)));
            if (te instanceof PanelTile) {
                PanelTile neighborTile = ((PanelTile) te);
                PanelCellPos neighborPos2;
                if (side == Side.FRONT || side==Side.BACK) {
                    neighborPos2 = PanelCellPos.fromRowColumn(localPos.getRow(),(localPos.getColumn() - 7) * -1);
                } else { //EAST & WEST
                    neighborPos2 = PanelCellPos.fromRowColumn((localPos.getRow() - 7) * -1,localPos.getColumn());
                }
                Integer neighbor2Index = neighborPos2.getIndex();
                IPanelCell neighborPanelCell = neighborTile.cells.get(neighbor2Index);
                Side neighborFacing = neighborTile.getPanelCellSide(neighbor2Index, side.getOpposite());
                if (neighborFacing != null) {
                    return new PanelCellNeighbor(neighborTile,neighborPanelCell,neighborFacing,side,neighbor2Index);
                }
                return null;
            } else {
                BlockPos blockPos = this.pos.offset(getDirectionFromSide(side));
                return new PanelCellNeighbor(this,blockPos,side);
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

    private Side getPanelCellSide(int cellIndex, Direction facing) {
        return getPanelCellSide(cellIndex,getSideFromDirection(facing));
    }

    private Side getPanelCellSide(int cellIndex,Side panelSide)
    {
        Side cellDirection = cellDirections.get(cellIndex);

        if (cellDirection == null) return null;

        if (cellDirection==panelSide)
            return Side.FRONT;
        if (cellDirection==panelSide.getOpposite())
            return Side.BACK;
        if (cellDirection==panelSide.rotateYCW())
            return Side.LEFT;
        if (cellDirection==panelSide.rotateYCCW())
            return Side.RIGHT;

        return null;
    }

    private Direction getPanelSideDirection(int cellIndex, Side side) {
        Direction cellDirection = getDirectionFromSide(cellDirections.get(cellIndex));
        if (cellDirection == null) return null;

        if (side == Side.FRONT)
            return cellDirection;
        if (side == Side.BACK)
            return cellDirection.getOpposite();
        if (side == Side.RIGHT)
            return cellDirection.rotateY();
        if (side == Side.LEFT)
            return cellDirection.rotateYCCW();

        return null;
    }

    private IPanelCell getAdjacentCell(Integer cellIndex, Side side) {
        Integer index = getAdjacentIndex(cellIndex, side);
        Direction direction = getDirectionFromSide(side);
        if (index == null) {
            TileEntity te = world.getTileEntity(pos.offset(direction));
            if (te instanceof PanelTile)
                return ((PanelTile) te).cells.get(getNeighborTileCellIndex(direction,cellIndex));
            return null;
        } else {
            return cells.get(index);
        }

    }

    private Side getAdjacentCellDirection(Integer cellIndex, Side side) {
        Integer index = getAdjacentIndex(cellIndex, side);
        Direction direction = getDirectionFromSide(side);
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
     * @param side The direction of the neighbor from the cell.
     * @return integer representing the neighbor's cell index, or null if we're at the edge of the panel
     */
    @Deprecated
    private Integer getAdjacentIndex(Integer cellIndex, Side side) {

        if (cellIndex==null) return null;

        PanelCellPos cellPos = PanelCellPos.fromIndex(cellIndex);


        if (side==Side.FRONT) {
            if (cellPos.getColumn() <= 0) {
                return null;
            } else {
                return cellIndex - 1;
            }
        }
        if (side==Side.BACK) {
            if (cellPos.getColumn() >= 7) {
                return null;
            } else {
                return cellIndex + 1;
            }
        }
        if (side==Side.LEFT) {
            if (cellPos.getRow() <= 0) {
                return null;
            } else {
                return cellIndex - 8;
            }
        }
        if (side==Side.RIGHT) {
            if (cellPos.getRow() >= 7) {
                return null;
            } else {
                return cellIndex + 8;
            }
        }

        return null;

    }

    private boolean cellPanelEdge(Integer cellIndex, Side side) {

        PanelCellPos cellPos = PanelCellPos.fromIndex(cellIndex);

        if (cellPos.getRow() == 0 && side==Side.LEFT)
            return true;
        if (cellPos.getRow() == 7 && side==Side.RIGHT)
            return true;
        if (cellPos.getColumn() == 0 && side==Side.FRONT)
            return true;
        if (cellPos.getColumn() == 7 && side==Side.BACK)
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
        for (Side panelSide : new Side[]{Side.FRONT,Side.RIGHT,Side.BACK,Side.LEFT}) {
            Direction direction = getDirectionFromSide(panelSide);
            weak=0;strong=0;
            List<Integer> indices = getEdgeCellIndices(direction);
            for (int i:indices) {
                IPanelCell cell = cells.get(i);
                Side side = getPanelCellSide(i, direction);
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

            if (strongPowerToNeighbors.get(panelSide) == null || strong != strongPowerToNeighbors.get(panelSide) ||
                    weakPowerToNeighbors.get(panelSide) == null || weak != weakPowerToNeighbors.get(panelSide)) {
                change = true;
                strongPowerToNeighbors.put(panelSide, strong);
                weakPowerToNeighbors.put(panelSide, weak);
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
        Side side = getSideFromDirection(edge);


        if (side==Side.FRONT)
        {
            for (int i = 0; i < 64; i += 8) {
                if (cells.containsKey(i)) {
                    cellIndices.add(i);
                }
            }

        }
        else if (side==Side.RIGHT)
        {
            for (int i = 56; i < 64; i++)
            {
                if (cells.containsKey(i))
                {
                    cellIndices.add(i);
                }
            }
        }
        else if (side==Side.BACK)
        {
            for (int i = 7; i < 64; i += 8)
            {
                if (cells.containsKey(i))
                {
                    cellIndices.add(i);
                }
            }
        }
        else if (side==Side.LEFT)
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

    protected Direction getDirectionFromSide(Side side) {
        Direction facing = getBlockState().get(BlockStateProperties.FACING);
        switch (side) {
            case FRONT:
                switch (facing) {
                    case DOWN:
                    case EAST:
                    case WEST:
                        return Direction.NORTH;
                    case UP:
                        return Direction.SOUTH;
                    case SOUTH:
                        return Direction.DOWN;
                    case NORTH:
                        return Direction.UP;
                }
                break;
            case RIGHT:
                switch (facing) {
                    case DOWN:
                    case NORTH:
                    case SOUTH:
                    case UP:
                        return Direction.EAST;
                    case EAST:
                        return Direction.UP;
                    case WEST:
                        return Direction.DOWN;
                }
                break;
            case BACK:
                switch (facing) {
                    case DOWN:
                    case EAST:
                    case WEST:
                        return Direction.SOUTH;
                    case UP:
                        return Direction.NORTH;
                    case SOUTH:
                        return Direction.UP;
                    case NORTH:
                        return Direction.DOWN;
                }
                break;
            case LEFT:
                switch (facing) {
                    case DOWN:
                    case NORTH:
                    case SOUTH:
                    case UP:
                        return Direction.WEST;
                    case EAST:
                        return Direction.DOWN;
                    case WEST:
                        return Direction.UP;
                }
        }
        //this should not be reached
        return null;
    }

    protected Side getSideFromDirection(Direction direction) {
        Direction facing = getBlockState().get(BlockStateProperties.FACING);
        switch (direction){
            case NORTH:
                switch (facing) {
                    case DOWN:
                    case EAST:
                    case WEST:
                        return Side.FRONT;
                    case UP:
                        return Side.BACK;
                }
                break;
            case EAST:
                switch (facing){
                    case DOWN:
                    case NORTH:
                    case SOUTH:
                    case UP:
                        return Side.RIGHT;
                }
                break;
            case SOUTH:
                switch (facing){
                    case UP:
                        return Side.FRONT;
                    case DOWN:
                    case EAST:
                    case WEST:
                        return Side.BACK;
                }
                break;
            case WEST:
                switch (facing){
                    case UP:
                    case DOWN:
                    case NORTH:
                    case SOUTH:
                        return Side.LEFT;
                }
                break;
            case UP:
                switch (facing){
                    case NORTH:
                        return Side.FRONT;
                    case EAST:
                        return Side.RIGHT;
                    case SOUTH:
                        return Side.BACK;
                    case WEST:
                        return Side.LEFT;
                }
                break;
            case DOWN:
                switch (facing){
                    case SOUTH:
                        return Side.FRONT;
                    case WEST:
                        return Side.RIGHT;
                    case NORTH:
                        return Side.BACK;
                    case EAST:
                        return Side.LEFT;
                }
        }

        //this should not be reached
        return null;
    }

    protected Direction getPlayerDirectionFacing(PlayerEntity player){
        Direction panelFacing = getBlockState().get(BlockStateProperties.FACING);
        if (panelFacing==Direction.UP||panelFacing==Direction.DOWN){
            return  player.getHorizontalFacing();
        }
        Direction[] playerFacings = Direction.getFacingDirections(player);
        for(Direction facing : playerFacings)
        {
            if (facing!=panelFacing && facing!=panelFacing.getOpposite())
                return facing;
        }
        return  player.getHorizontalFacing();
    }
}



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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanelTile extends TileEntity implements ITickableTileEntity {

    //panel data (saved)
    private Map<Integer, IPanelCell> cells = new HashMap<>();
    private Map<Integer, Side> cellDirections = new HashMap<>();
    protected Map<Side, Integer> weakPowerFromNeighbors = new HashMap<>();
    protected Map<Side, Integer> strongPowerFromNeighbors = new HashMap<>();
    protected Map<Side, Integer> strongPowerToNeighbors = new HashMap<>();
    protected Map<Side, Integer> weakPowerToNeighbors = new HashMap<>();
    protected Map<Side, Integer> comparatorOverrides = new HashMap<>();

    protected Integer Color = DyeColor.GRAY.getColorValue();
    private Integer lightOutput = 0;
    protected boolean flagLightUpdate = false;
    private boolean flagCrashed = false;
    protected IPanelCover panelCover = null;

    //other state fields (not saved)
    private boolean flagSync = false;
    protected Integer lookingAtCell = null;
    protected Side lookingAtDirection = null;
    protected IPanelCell lookingAtWith = null;


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
                if (this.cells.size()==0)
                    this.flagSync=true;
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
                        BlockPos neighborPos = pos.offset(getDirectionFromSide(side));
                        BlockState neighborState = world.getBlockState(neighborPos);
                        if (neighborState.hasComparatorInputOverride()) {
                            int comparatorInputOverride = neighborState.getComparatorInputOverride(world, neighborPos);
                            if (comparatorInputOverride != comparatorOverrides.get(side)) {
                                this.comparatorOverrides.put(side, comparatorInputOverride);
                                updateSide(side);
                                dirty = true;
                            }
                        } else {
                            comparatorOverrides.remove(side);
                            if (updateSide(side)) {
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
                            updateNeighborCells(PanelCellPos.fromIndex(this,index));
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


                if (flagSync)
                {
                    updateSide(Side.FRONT);
                    updateSide(Side.RIGHT);
                    updateSide(Side.BACK);
                    updateSide(Side.LEFT);
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
                                if (posInPanelCell!=null && !this.cells.containsKey(posInPanelCell.getIndex())) {
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
            PanelCellPos pistonPos = PanelCellPos.fromIndex(this,index);

            //get the side of the panel that the piston head is facing (back of tiny piston cell)
            Side movingToward = cellDirections.get(index).getOpposite();
            PanelCellPos moverPos = pistonPos.offset(movingToward);

            if (!((Piston) panelCell).isExtended() && panelCell instanceof StickyPiston) {
                moverPos = moverPos.offset(movingToward);
                movingToward = movingToward.getOpposite();
            }

            world.playSound(
                    pos.getX(), pos.getY(), pos.getZ(),
                    (((Piston) panelCell).isExtended()) ? SoundEvents.BLOCK_PISTON_EXTEND : SoundEvents.BLOCK_PISTON_CONTRACT,
                    SoundCategory.BLOCKS, 0.25f, 2f, false
            );

            if (moverPos != null) {
                PanelTile moverPanelTile = moverPos.getPanelTile();
                if (moverPanelTile.cells.containsKey(moverPos.getIndex()))
                    panelTile.moveCell(moverPos, movingToward, 0);
                else {
                    panelTile.updateNeighborCells(moverPos);
                }
            }
        }
    }

    /**
     * move a component to an adjacent position and iterate into adjacent cells
     * @param cellPos position to move a cell from
     * @param towardSide direction to move it toward
     * @param iteration how many times have we iterated
     * @return whether the move was successful and/or if adjacent cell can move into this pos
     */
    private boolean moveCell(PanelCellPos cellPos, Side towardSide, Integer iteration) {
        if (iteration > 12) return false;

        IPanelCell cell = cellPos.getIPanelCell();

        if (cell == null) return true;
        if (!cell.isPushable()) return false;

        PanelCellPos newPos = cellPos.offset(towardSide);

        //try to move the neighbor first and make sure it's successful
        if (newPos==null || ( newPos.getIPanelCell() != null && !newPos.getPanelTile().moveCell(newPos,towardSide,iteration+1)))
            return false;

        int oldIndex = cellPos.getIndex();
        int newIndex = newPos.getIndex();
        newPos.getPanelTile().cells.put(newIndex, cell);
        newPos.getPanelTile().cellDirections.put(newIndex,cellPos.getCellFacing());
        cellPos.getPanelTile().cells.remove(oldIndex);
        cellPos.getPanelTile().cellDirections.remove(oldIndex);

        newPos.getPanelTile().updateNeighborCells(newPos);
        cellPos.getPanelTile().updateNeighborCells(cellPos);

        newPos.getPanelTile().markDirty();
        cellPos.getPanelTile().markDirty();

        return true;
    }

    public boolean canExtendTo(PanelCellPos cellPos, Side side, Integer iteration)
    {
        if (iteration>12)return false;

        IPanelCell iPanelCell = cellPos.getIPanelCell();

        //check if a piston has extended into this block
        if (checkCellForPistonExtension(cellPos))
            return false;

        //if this is an empty cell, we know it can be pushed into
        if (iPanelCell==null)
            return true;

        //if this is not a pushable cell, we know we can't extend into it
        if(!iPanelCell.isPushable())
            return false;

        //otherwise, check if we're on the edge of a tile
        PanelCellPos adjacentPos = cellPos.offset(side);
        if (adjacentPos==null)
        {
            //we must be at the edge of the tile and facing some other type of block, so we can't extend
            return false;
        }
        else {
            //we can be pushed and are not on the edge, so check the next cell and see if it can also be pushed
            return adjacentPos.getPanelTile().canExtendTo(adjacentPos,side,iteration+1);
        }
    }

    @Override
    public void rotate(Rotation rotationIn) {


        Map<Integer, IPanelCell> cells = new HashMap<>();
        Map<Integer, Side> cellDirections = new HashMap<>();

        for (Integer i = 0; i < 64; i++) {
            if (this.cells.containsKey(i)) {
                PanelCellPos cellPos1 = PanelCellPos.fromIndex(this,i);
                PanelCellPos cellPos2;
                Side side1 = this.cellDirections.get(i);
                Side side2;

                if (rotationIn == Rotation.COUNTERCLOCKWISE_90) {
                    cellPos2 = PanelCellPos.fromRowColumn(this,cellPos1.getColumn(),((cellPos1.getRow() - 4) * -1) + 3);
                    side2 = side1.rotateYCCW();
                } else if (rotationIn == Rotation.CLOCKWISE_180) {
                    cellPos2 = PanelCellPos.fromRowColumn(this,((cellPos1.getRow() - 4) * -1) + 3,((cellPos1.getColumn() - 4) * -1) + 3);
                    side2 = side1.getOpposite();
                } else {
                    //default rotation 90°
                    cellPos2 = PanelCellPos.fromRowColumn(this,((cellPos1.getColumn() - 4) * -1) + 3,cellPos1.getRow());
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
        boolean updated = false;

        if (side==Side.LEFT) {
            for (int i = 0; i < 8; i++) {
                if (cells.containsKey(i)) {
                    if (updateCell(i))
                        updated=true;
                }
            }
        } else if (side==Side.FRONT) {
            for (int i = 0; i < 64; i += 8) {
                if (cells.containsKey(i)) {
                    if (updateCell(i))
                        updated=true;
                }
            }
        } else if (side==Side.RIGHT) {
            for (int i = 56; i < 64; i++) {
                if (cells.containsKey(i)) {
                    if (updateCell(i))
                        updated=true;
                }
            }
        } else if (side==Side.BACK) {
            for (int i = 7; i < 64; i += 8) {
                if (cells.containsKey(i)) {
                    if (updateCell(i))
                        updated=true;
                }
            }
        }


        return updated;
    }

    public boolean updateNeighborCells(PanelCellPos cellPos) {
        return updateNeighborCells(cellPos, 1);
    }

    private boolean updateNeighborCells(PanelCellPos cellPos, Integer iteration) {
        List<PanelCellPos> cellPosList = new ArrayList<>();
        boolean updateOutputs = false;

        if (updateNeighbor(cellPos,Side.FRONT,cellPosList))
            updateOutputs=true;
        if (updateNeighbor(cellPos,Side.RIGHT,cellPosList))
            updateOutputs=true;
        if (updateNeighbor(cellPos,Side.BACK,cellPosList))
            updateOutputs=true;
        if (updateNeighbor(cellPos,Side.LEFT,cellPosList))
            updateOutputs=true;


        if (updateOutputs) {
            updateOutputs();
        }

        boolean updated = false;
        for (PanelCellPos updatePos : cellPosList)
        {
            if(updateCell(updatePos,iteration+1))
                updated=true;
        }
        return updated || updateOutputs;
    }

    private boolean updateNeighbor(PanelCellPos cellPos, Side side, List<PanelCellPos> cellPosList) {
        PanelCellPos neighborPos = cellPos.offset(side);

        if (neighborPos == null)
            return true;

        IPanelCell adjacentCell = neighborPos.getIPanelCell();
        if (adjacentCell instanceof IObservingPanelCell) {
            Side direction1 = neighborPos.getCellFacing();
            Side direction2 = side.getOpposite();
            if (direction1 == direction2) {
                ((IObservingPanelCell) adjacentCell).frontNeighborUpdated();
            }
        } else if (adjacentCell != null && !adjacentCell.isIndependentState())
            cellPosList.add(neighborPos);

        return false;
    }

    public boolean checkCellForPistonExtension(PanelCellPos cellPos) {
        for(Side panelSide : new Side[]{Side.FRONT,Side.RIGHT,Side.BACK,Side.LEFT})
        {
            PanelCellPos neighborPos = cellPos.offset(panelSide);
            if (neighborPos!=null) {
                IPanelCell cell = neighborPos.getIPanelCell();
                if (cell instanceof Piston && neighborPos.getCellFacing() == panelSide && ((Piston) cell).isExtended())
                    return true;
            }
        }
        return false;
    }

    protected boolean updateCell(Integer cellIndex) {
        PanelCellPos cellPos = PanelCellPos.fromIndex(this,cellIndex);
        return updateCell(cellPos, 1);
    }
    protected boolean updateCell(PanelCellPos cellPos) {
        return updateCell(cellPos, 1);
    }
    /**
     * Update a cell with a potential input change
     *
     * @param cellPos position of cell to update
     * @return true if this update caused the panel output to change
     */
    private boolean updateCell(PanelCellPos cellPos, int iteration) {

        boolean change = false;
        if (iteration > 63) {
            TinyRedstone.LOGGER.warn("Redstone panel iterated too many times.");
            return false;
        }

        //if this cell position is on a different panel than this one, call this method on that panel
        if (cellPos.getPanelTile()!=this)
            return cellPos.getPanelTile().updateCell(cellPos,iteration);

        IPanelCell thisCell = cellPos.getIPanelCell();

        if (thisCell != null && !thisCell.isIndependentState()){
                Side frontDirection = cellPos.getCellFacing();
                Side backDirection = frontDirection.getOpposite();
                Side rightDirection = frontDirection.rotateYCW();
                Side leftDirection = frontDirection.rotateYCCW();

                PanelCellNeighbor front = getNeighbor(frontDirection, cellPos);
                PanelCellNeighbor back = getNeighbor(backDirection, cellPos);
                PanelCellNeighbor right = getNeighbor(rightDirection, cellPos);
                PanelCellNeighbor left = getNeighbor(leftDirection, cellPos);

                if (thisCell.neighborChanged(front, right, back, left)) {
                    updateNeighborCells(cellPos, iteration + 1);
                    change = true;
                }
        }


        if (change)
            this.markDirty();
        return change;
    }

    /**
     * Gets a PanelCellNeighbor object providing data about the neighboring cell or block.
     *
     * @param side Toward which side of the panel the adjacent cell or block is relative to this cell.
     * @param localCellPos Position of the panel cell on this panel.
     * @return an Integer array of size 2. First element is weak power. Second is strong power.
     */
    @CheckForNull
    private PanelCellNeighbor getNeighbor(Side side, PanelCellPos localCellPos) {

        PanelCellPos neighborPos = localCellPos.offset(side);

        if (neighborPos != null) {
            IPanelCell neighborCell = neighborPos.getIPanelCell();

            if (neighborCell != null) {
                Side neighborSide = neighborPos.getPanelTile().getPanelCellSide(neighborPos, side.getOpposite());
                return new PanelCellNeighbor(neighborPos, neighborCell, neighborSide, side);
            } else if (checkCellForPistonExtension(neighborPos)) {
                return new PanelCellNeighbor(neighborPos, null, null, side);
            }

        } else {
            BlockPos blockPos = this.pos.offset(getDirectionFromSide(side));
            return new PanelCellNeighbor(this, blockPos, side);
        }


        return null;
    }

    private Side getPanelCellSide(PanelCellPos cellPos, Direction facing) {
        return getPanelCellSide(cellPos,getSideFromDirection(facing));
    }

    /**
     * Get which side of the cell is facing the given side of the panel
     * @param cellPos position of cell being queried
     * @param panelSide the side of the redstone panel
     * @return the side of the cell that is facing the given side of the panel
     */
    private Side getPanelCellSide(PanelCellPos cellPos,Side panelSide)
    {
        Side cellDirection = cellPos.getPanelTile().getCellFacing(cellPos);

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
                PanelCellPos cellPos = PanelCellPos.fromIndex(this,i);
                IPanelCell cell = cellPos.getIPanelCell();
                Side side = getPanelCellSide(cellPos, direction);
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

    @CheckForNull
    public IPanelCell getIPanelCell(PanelCellPos cellPos){
        if (cellPos==null)return null;
        return this.cells.get(cellPos.getIndex());
    }

    @CheckForNull
    public Side getCellFacing(PanelCellPos cellPos){
        if (cellPos==null)return null;
        return this.cellDirections.get(cellPos.getIndex());
    }

    public int getCellCount()
    {
        return cells.size();
    }

    public void removeCell(PanelCellPos cellPos)
    {
        if (cellPos.getIPanelCell() != null) {
            int cellIndex = cellPos.getIndex();
            //remove from panel
            cellDirections.remove(cellIndex);
            cells.remove(cellIndex);
            updateNeighborCells(cellPos);

            sync();
        }
    }

    public void addCell(PanelCellPos cellPos,IPanelCell panelCell, Side facing)
    {
        int cellIndex = cellPos.getIndex();
        cellDirections.put(cellIndex, facing);
        cells.put(cellIndex, panelCell);
        if (!panelCell.isIndependentState()) {
            updateCell(cellIndex);
        }
        updateNeighborCells(cellPos);
    }

    public boolean hasCellsOnFace(Direction direction)
    {
        Side facing = getSideFromDirection(direction);
        if (facing==Side.LEFT) {
            for (int i = 0; i < 8; i++) {
                if (cells.containsKey(i)) {
                    return true;
                }
            }
        } else if (facing == Side.FRONT) {
            for (int i = 0; i < 64; i += 8) {
                if (cells.containsKey(i)) {
                    return true;
                }
            }
        } else if (facing == Side.RIGHT) {
            for (int i = 56; i < 64; i++) {
                if (cells.containsKey(i)) {
                    return true;
                }
            }
        } else if (facing == Side.BACK) {
            for (int i = 7; i < 64; i += 8) {
                if (cells.containsKey(i)) {
                    return true;
                }
            }
        }
        return false;
    }
}



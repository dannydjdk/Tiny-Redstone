package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.Config;
import com.dannyandson.tinyredstone.PanelOverflowException;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IObservingPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCover;
import com.dannyandson.tinyredstone.blocks.panelcells.*;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Constants;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanelTile extends BlockEntity {

    //panel data (saved)
    private Map<Integer, IPanelCell> cells = new HashMap<>();
    private Map<Integer, Side> cellDirections = new HashMap<>();
    protected Map<Side, Integer> strongPowerToNeighbors = new HashMap<>();
    protected Map<Side, Integer> weakPowerToNeighbors = new HashMap<>();

    protected Integer Color = RenderHelper.getTextureDiffusedColor(DyeColor.GRAY);
    private Integer lightOutput = 0;
    protected boolean flagLightUpdate = false;
    private boolean flagCrashed = false;
    private boolean flagOverflow = false;
    protected IPanelCover panelCover = null;

    //other state fields (not saved)
    private boolean flagUpdate = false;
    private boolean flagSync = true;
    protected PanelCellGhostPos panelCellGhostPos;
    private VoxelShape voxelShape = null;

    //for backward compat. Remove on next major update (2.x.x).
    private boolean fixLegacyFacing = false;

    public PanelTile(BlockPos p_155229_, BlockState p_155230_) {
        super(Registration.REDSTONE_PANEL_TILE.get(), p_155229_, p_155230_);
    }

    //Tell Minecraft to render this block whenever any of the block space is within view.
    //By default, it only renders when the base model is within view.
    @Override
    public AABB getRenderBoundingBox()
    {
        return new AABB(getBlockPos());
    }

    /* When the world loads from disk, the server needs to send the TileEntity information to the client
    //  it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this:
    //  getUpdatePacket() and onDataPacket() are used for one-at-a-time TileEntity updates
    //  getUpdateTag() and handleUpdateTag() are used by vanilla to collate together into a single chunk update packet
    */

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag nbtTagCompound = new CompoundTag();
        this.save(nbtTagCompound);
        int tileEntityType = -1;  // arbitrary number; only used for vanilla TileEntities.
        return new ClientboundBlockEntityDataPacket(this.worldPosition, tileEntityType, nbtTagCompound);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (this.level.isClientSide) {
            this.cells.clear();
            this.cellDirections.clear();
        }
        this.load(pkt.getTag());   // read from the nbt in the packet
    }

    /* Creates a tag containing the TileEntity information, used by vanilla to transmit from server to client*/
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbtTagCompound = new CompoundTag();
        this.save(nbtTagCompound);
        return nbtTagCompound;
    }

    /* Populates this TileEntity with information from the tag, used by vanilla to transmit from server to client*/
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (this.level.isClientSide) {
            this.cells.clear();
            this.cellDirections.clear();
        }
        this.load(tag);
    }

    public CompoundTag saveToNbt(CompoundTag CompoundTag) {

        CompoundTag cellsNBT = new CompoundTag();

        for (Integer key : this.cells.keySet()) {
            IPanelCell cell = this.cells.get(key);
            CompoundTag cellNBT = new CompoundTag();

            cellNBT.putString("class", cell.getClass().getCanonicalName());
            cellNBT.putString("facing", this.cellDirections.get(key).name());
            cellNBT.put("data", cell.writeNBT());

            cellsNBT.put(key.toString(), cellNBT);
        }

        CompoundTag.put("cells", cellsNBT);
        if(this.Color!=DyeColor.GRAY.getId())
            CompoundTag.putInt("color", this.Color);
        if (panelCover!=null)
            CompoundTag.putString("cover",panelCover.getClass().getCanonicalName());

        return CompoundTag;
    }

    /* This is where you save any data that you don't want to lose when the tile entity unloads*/
    @Override
    public CompoundTag save(CompoundTag parentNBTTagCompound) {
        try {
            if (this.strongPowerToNeighbors.size()==5) {
                CompoundTag strongPowerToNeighbors = new CompoundTag();
                strongPowerToNeighbors.putInt(Side.FRONT.ordinal() + "", this.strongPowerToNeighbors.get(Side.FRONT));
                strongPowerToNeighbors.putInt(Side.RIGHT.ordinal() + "", this.strongPowerToNeighbors.get(Side.RIGHT));
                strongPowerToNeighbors.putInt(Side.BACK.ordinal() + "",  this.strongPowerToNeighbors.get(Side.BACK));
                strongPowerToNeighbors.putInt(Side.LEFT.ordinal() + "",  this.strongPowerToNeighbors.get(Side.LEFT));
                strongPowerToNeighbors.putInt(Side.TOP.ordinal() + "",  this.strongPowerToNeighbors.get(Side.TOP));
                parentNBTTagCompound.put("strong_power_outgoing", strongPowerToNeighbors);
            }
            if (this.weakPowerToNeighbors.size()==5) {
                CompoundTag weakPowerToNeighbors = new CompoundTag();
                weakPowerToNeighbors.putInt(Side.FRONT.ordinal() + "", this.weakPowerToNeighbors.get(Side.FRONT));
                weakPowerToNeighbors.putInt(Side.RIGHT.ordinal() + "", this.weakPowerToNeighbors.get(Side.RIGHT));
                weakPowerToNeighbors.putInt(Side.BACK.ordinal() + "",  this.weakPowerToNeighbors.get(Side.BACK));
                weakPowerToNeighbors.putInt(Side.LEFT.ordinal() + "",  this.weakPowerToNeighbors.get(Side.LEFT));
                weakPowerToNeighbors.putInt(Side.TOP.ordinal() + "",  this.weakPowerToNeighbors.get(Side.TOP));
                parentNBTTagCompound.put("weak_power_outgoing", weakPowerToNeighbors);
            }

            parentNBTTagCompound.putInt("lightOutput",this.lightOutput);
            parentNBTTagCompound.putBoolean("flagLightUpdate",this.flagLightUpdate);
            parentNBTTagCompound.putBoolean("flagCrashed",this.flagCrashed);
            parentNBTTagCompound.putBoolean("flagOverflow",this.flagOverflow);

        } catch (NullPointerException exception) {
            TinyRedstone.LOGGER.error("Exception thrown when attempting to save power inputs and outputs: " + exception.toString() + ((exception.getStackTrace().length>0)?exception.getStackTrace()[0].toString():""));
        }

        return super.save(this.saveToNbt(parentNBTTagCompound));
    }

    // This is where you load the data that you saved in writeToNBT
    @Override
    public void load(CompoundTag parentNBTTagCompound) {
        int previousLightOutput = this.lightOutput;

        super.load(parentNBTTagCompound);

        // important rule: never trust the data you read from NBT, make sure it can't cause a crash

        this.loadCellsFromNBT(parentNBTTagCompound,true);

        CompoundTag strongPowerToNeighbors = parentNBTTagCompound.getCompound("strong_power_outgoing");
        if (!strongPowerToNeighbors.isEmpty()) {
            this.strongPowerToNeighbors.put(Side.FRONT, strongPowerToNeighbors.getInt(Side.FRONT.ordinal() + ""));
            this.strongPowerToNeighbors.put(Side.RIGHT, strongPowerToNeighbors.getInt(Side.RIGHT.ordinal() + ""));
            this.strongPowerToNeighbors.put(Side.BACK,  strongPowerToNeighbors.getInt(Side.BACK.ordinal() + ""));
            this.strongPowerToNeighbors.put(Side.LEFT,  strongPowerToNeighbors.getInt(Side.LEFT.ordinal() + ""));
            this.strongPowerToNeighbors.put(Side.TOP,  strongPowerToNeighbors.getInt(Side.TOP.ordinal() + ""));
        }
        CompoundTag weakPowerToNeighbors = parentNBTTagCompound.getCompound("weak_power_outgoing");
        if (!weakPowerToNeighbors.isEmpty()) {
            this.weakPowerToNeighbors.put(Side.FRONT, weakPowerToNeighbors.getInt(Side.FRONT.ordinal() + ""));
            this.weakPowerToNeighbors.put(Side.RIGHT, weakPowerToNeighbors.getInt(Side.RIGHT.ordinal() + ""));
            this.weakPowerToNeighbors.put(Side.BACK,  weakPowerToNeighbors.getInt(Side.BACK.ordinal() + ""));
            this.weakPowerToNeighbors.put(Side.LEFT,  weakPowerToNeighbors.getInt(Side.LEFT.ordinal() + ""));
            this.weakPowerToNeighbors.put(Side.TOP,  weakPowerToNeighbors.getInt(Side.TOP.ordinal() + ""));
        }

        this.lightOutput = parentNBTTagCompound.getInt("lightOutput");
        this.flagLightUpdate = parentNBTTagCompound.getBoolean("flagLightUpdate");
        this.flagCrashed = parentNBTTagCompound.getBoolean("flagCrashed");
        this.flagOverflow = parentNBTTagCompound.getBoolean("flagOverflow");

        if (parentNBTTagCompound.contains("color")) {
            int color = parentNBTTagCompound.getInt("color");
            if (this.Color != color) {
                this.Color = color;
                this.flagUpdate = true;
            }
        }

        String coverClass = parentNBTTagCompound.getString("cover");
        if (coverClass.length()>0)
        {
            try {
                panelCover= (IPanelCover) Class.forName(coverClass).getConstructor().newInstance();
            } catch (Exception exception) {
                TinyRedstone.LOGGER.error("Exception attempting to construct IPanelCover class " + coverClass, exception);
            }
        }
        else
        {
            panelCover=null;
        }

        if(this.lightOutput != previousLightOutput) {
            this.level.getLightEngine().checkBlock(worldPosition);
        }

    }

    public void loadCellsFromNBT(CompoundTag parentNBTTagCompound,boolean fixFacing)
    {
        CompoundTag cellsNBT = parentNBTTagCompound.getCompound("cells");

        for (String index : cellsNBT.getAllKeys()) {
            CompoundTag cellNBT = cellsNBT.getCompound(index);
            if (cellNBT.contains("data")) {
                String className = cellNBT.getString("class");
                try {
                    IPanelCell cell = (IPanelCell) Class.forName(className).getConstructor().newInstance();
                    cell.readNBT(cellNBT.getCompound("data"));
                    Integer i = Integer.parseInt(index);
                    this.cells.put(i, cell);


                    if (cellNBT.contains("direction"))
                    {
                        //backward compatibility
                        Direction direction = Direction.from3DDataValue(cellNBT.getInt("direction"));
                        if (direction==Direction.NORTH) this.cellDirections.put(i,Side.FRONT);
                        else if (direction==Direction.EAST) this.cellDirections.put(i,Side.RIGHT);
                        else if (direction==Direction.SOUTH) this.cellDirections.put(i,Side.BACK);
                        else if (direction==Direction.WEST) this.cellDirections.put(i,Side.LEFT);

                        //for panels placed before the direction update, set facing to DOWN
                        if (fixFacing)
                            this.fixLegacyFacing = true;

                    }
                    else
                        this.cellDirections.put(i, Side.valueOf(cellNBT.getString("facing")));
                } catch (Exception exception) {
                    TinyRedstone.LOGGER.error("Exception attempting to construct IPanelCell class " + className, exception);
                }
            }
        }
        clearVoxelShape();
    }

    public void tick() {
        try {
            if (!flagCrashed && !flagOverflow) {
                boolean dirty = false;
                //backward compatibility. Remove on major update (2.x.x).
                if (fixLegacyFacing){
                    level.setBlockAndUpdate(worldPosition, Registration.REDSTONE_PANEL_BLOCK.get().defaultBlockState().setValue(BlockStateProperties.FACING,Direction.DOWN));
                    fixLegacyFacing=false;
                    dirty=true;
                }

                //call the tick() method in all our cells and grab any updated pistons
                List<Integer> pistons = null;
                for (Integer index : this.cells.keySet()) {
                    PanelCellPos cellPos = PanelCellPos.fromIndex(this,index);
                    IPanelCell panelCell = this.cells.get(index);
                    boolean update = panelCell.tick(cellPos);
                    if (update) {
                        if (panelCell instanceof Piston) {
                            if (pistons == null)
                                pistons = new ArrayList<>();
                            pistons.add(index);
                        } else {
                            if(panelCell instanceof Button)
                            {
                                level.playLocalSound(
                                        worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                                        panelCell instanceof StoneButton ? SoundEvents.STONE_BUTTON_CLICK_OFF : SoundEvents.WOODEN_BUTTON_CLICK_OFF,
                                        SoundSource.BLOCKS, 0.25f, 2f, false
                                );
                            }
                            updateNeighborCells(cellPos);
                        }
                        dirty = true;
                    }

                }

                //if any pistons updated state, try to update them
                if (pistons != null) {
                    for (Integer index : pistons) {
                        updatePiston(index);
                    }
                }

                if (this.flagLightUpdate) {
                    this.flagLightUpdate = false;
                    this.level.getLightEngine().checkBlock(worldPosition);
                }


                if (flagUpdate)
                {
                    updateSide(Side.FRONT);
                    updateSide(Side.RIGHT);
                    updateSide(Side.BACK);
                    updateSide(Side.LEFT);
                }
                if (dirty || flagUpdate) {
                    setChanged();
                    updateOutputs();
                }

                if (flagSync || dirty || flagUpdate) {
                    sync();
                    flagSync=false;
                    flagUpdate=false;
                }

                if (level.isClientSide) {
                    PanelCellGhostPos gPos = PanelTileRenderer.getPlayerLookingAtCell(this);
                    if (gPos!=null)
                        gPos.getPanelTile().panelCellGhostPos=gPos;
                    else
                        this.panelCellGhostPos=null;
                }
            }
        }catch(Exception e)
        {
            this.handleCrash(e);
        }

    }

    private void updatePiston(int index) throws PanelOverflowException{
        IPanelCell panelCell = cells.get(index);
        if (panelCell instanceof Piston) {
            PanelTile panelTile = this;
            PanelCellPos pistonPos = PanelCellPos.fromIndex(this,index);

            //get the side of the panel that the piston head is facing (back of tiny piston cell)
            Side movingToward = pistonPos.getCellFacing().getOpposite();
            PanelCellPos moverPos = pistonPos.offset(movingToward);

            if (!((Piston) panelCell).isExtended() && panelCell instanceof StickyPiston && moverPos!=null && moverPos.getIPanelCell()==null) {
                moverPos = moverPos.offset(movingToward);
                movingToward = movingToward.getOpposite();
                if (moverPos.getIPanelCell()!=null && moverPos.getIPanelCell().needsSolidBase())
                    moverPos=null;
            }

            level.playLocalSound(
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    (((Piston) panelCell).isExtended()) ? SoundEvents.PISTON_EXTEND : SoundEvents.PISTON_CONTRACT,
                    SoundSource.BLOCKS, 0.25f, 2f, false
            );

            if (moverPos != null) {
                PanelTile moverPanelTile = moverPos.getPanelTile();
                if (moverPanelTile.cells.containsKey(moverPos.getIndex()))
                    panelTile.moveCell(moverPos, movingToward, 0);
                else {
                    panelTile.updateNeighborCells(moverPos);
                }
            }
            PanelCellPos abovePiston = pistonPos.offset(Side.TOP);
            if (abovePiston!=null && abovePiston.getIPanelCell()!=null)
                panelTile.updateCell(abovePiston);
        }
    }

    /**
     * move a component to an adjacent position and iterate into adjacent cells
     * @param cellPos position to move a cell from
     * @param towardSide direction to move it toward
     * @param iteration how many times have we iterated
     * @return whether the move was successful and/or if adjacent cell can move into this pos
     */
    private boolean moveCell(PanelCellPos cellPos, Side towardSide, Integer iteration) throws PanelOverflowException {
        if (iteration > 12) return false;

        IPanelCell cell = cellPos.getIPanelCell();

        if (cell == null) return true;
        if (cell.needsSolidBase()) {
            Registration.REDSTONE_PANEL_BLOCK.get().removeCell(cellPos, null);
            return true;
        }
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

        newPos.getPanelTile().setChanged();
        newPos.getPanelTile().flagSync=true;
        newPos.getPanelTile().clearVoxelShape();
        cellPos.getPanelTile().setChanged();
        cellPos.getPanelTile().flagSync=true;
        cellPos.getPanelTile().clearVoxelShape();

        if (cell instanceof IObservingPanelCell)
            ((IObservingPanelCell) cell).frontNeighborUpdated();

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

        //if this cell needs a solid base, it will be removed, so return true
        if(!iPanelCell.needsSolidBase())
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

    public void rotate(Rotation rotationIn) {


        Map<Integer, IPanelCell> cells = new HashMap<>();
        Map<Integer, Side> cellDirections = new HashMap<>();

        for (Integer i: this.cells.keySet()) {

                PanelCellPos cellPos1 = PanelCellPos.fromIndex(this,i);
                PanelCellPos cellPos2;
                Side side1 = this.cellDirections.get(i);
                Side side2;

                if (rotationIn == Rotation.COUNTERCLOCKWISE_90) {
                    cellPos2 = PanelCellPos.fromRowColumn(this,cellPos1.getColumn(),((cellPos1.getRow() - 4) * -1) + 3, cellPos1.getLevel());
                    side2 = side1.rotateYCCW();
                } else if (rotationIn == Rotation.CLOCKWISE_180) {
                    cellPos2 = PanelCellPos.fromRowColumn(this,((cellPos1.getRow() - 4) * -1) + 3,((cellPos1.getColumn() - 4) * -1) + 3, cellPos1.getLevel());
                    side2 = side1.getOpposite();
                } else {
                    //default rotation 90°
                    cellPos2 = PanelCellPos.fromRowColumn(this,((cellPos1.getColumn() - 4) * -1) + 3,cellPos1.getRow(), cellPos1.getLevel());
                    side2 = side1.rotateYCW();
                }

                cells.put(cellPos2.getIndex(), this.cells.get(i));
                cellDirections.put(cellPos2.getIndex(),side2);

        }

        this.cells = cells;
        this.cellDirections = cellDirections;

        try {
            updateSide(Side.FRONT);
            updateSide(Side.RIGHT);
            updateSide(Side.BACK);
            updateSide(Side.LEFT);

            if (!level.isClientSide)
                setChanged();

            updateOutputs();

            clearVoxelShape();
            flagSync();
        }catch (PanelOverflowException e) {
            this.handleCrash(e);
        }

    }


    public boolean pingOutwardObservers(Direction facing) {

        boolean updated = false;
        Side side = getSideFromDirection(facing);
        List<Integer> cellIndices = getEdgeCellIndices(side);
        for (Integer i : cellIndices)
            if (cells.containsKey(i) && cells.get(i) instanceof IObservingPanelCell && cellDirections.get(i)==side) {
                if(((IObservingPanelCell)cells.get(i)).frontNeighborUpdated())
                    updated=true;
            }

        return updated;
    }

    public boolean updateSide(Direction facing) throws PanelOverflowException {
        return updateSide(getSideFromDirection(facing));
    }
    public boolean updateSide(Side side) throws PanelOverflowException{
        boolean updated = false;

        List<Integer> cellIndices = getEdgeCellIndices(side);

        for (Integer i : cellIndices) {
            if (cells.containsKey(i)) {
                if (updateCell(i))
                    updated = true;
            }
        }

        return updated;
    }

    public boolean updateNeighborCells(PanelCellPos cellPos) throws PanelOverflowException{
        return updateNeighborCells(cellPos, 1);
    }

    private boolean updateNeighborCells(PanelCellPos cellPos, Integer iteration) throws PanelOverflowException{
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
        if (updateNeighbor(cellPos,Side.TOP,cellPosList))
            updateOutputs=true;
        if (updateNeighbor(cellPos,Side.BOTTOM,cellPosList))
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
        } else if (adjacentCell != null && (!adjacentCell.isIndependentState()||(adjacentCell.needsSolidBase()&&side.getOpposite()==neighborPos.getBaseDirection())))
            cellPosList.add(neighborPos);

        return false;
    }

    public boolean checkCellForPistonExtension(PanelCellPos cellPos) {
        for(Side panelSide : new Side[]{Side.FRONT,Side.RIGHT,Side.BACK,Side.LEFT,Side.TOP,Side.BOTTOM})
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

    protected boolean updateCell(Integer cellIndex) throws PanelOverflowException{
        PanelCellPos cellPos = PanelCellPos.fromIndex(this,cellIndex);
        return updateCell(cellPos, 1);
    }
    protected boolean updateCell(PanelCellPos cellPos) throws PanelOverflowException{
        return updateCell(cellPos, 1);
    }
    /**
     * Update a cell with a potential input change
     *
     * @param cellPos position of cell to update
     * @return true if this update caused the panel output to change
     */
    private boolean updateCell(PanelCellPos cellPos, int iteration) throws PanelOverflowException{

        boolean change = false;
        if (iteration > (16 * Config.CIRCUIT_MAX_ITERATION.get()) || this.flagOverflow) {
            if (!this.flagOverflow)
                TinyRedstone.LOGGER.warn("Redstone panel at " + worldPosition.getX() + "," + worldPosition.getY() + "," + worldPosition.getZ() + " iterated too many times.");
            this.flagOverflow=true;
            throw new PanelOverflowException("Redstone Panel iterated too many times.");
            //return false;
        }

        //if this cell position is on a different panel than this one, call this method on that panel
        if (cellPos.getPanelTile()!=this)
            return cellPos.getPanelTile().updateCell(cellPos,iteration);

        IPanelCell thisCell = cellPos.getIPanelCell();

        if (thisCell != null) {

            if (thisCell.needsSolidBase()) {
                Side baseDirection = cellPos.getBaseDirection();
                PanelCellPos basePos = cellPos.offset(baseDirection);
                if (basePos != null && (basePos.getIPanelCell() == null || (!basePos.getIPanelCell().isPushable()) && !(basePos.getIPanelCell() instanceof Piston && basePos.getCellFacing()==cellPos.getBaseDirection().getOpposite() ) )) {
                    Registration.REDSTONE_PANEL_BLOCK.get().removeCell(cellPos, null);
                    change = true;
                }
            }
            //TODO consider making this more efficient by only updating affected sides
            if (!change && !thisCell.isIndependentState() && thisCell.neighborChanged(cellPos)) {
                updateNeighborCells(cellPos, iteration + 1);
                if (thisCell instanceof RedstoneDust) {
                    PanelCellPos above = cellPos.offset(Side.TOP), below = cellPos.offset(Side.BOTTOM);
                    if (above != null)
                        updateNeighborCells(above, iteration + 1);
                    if (below != null)
                        updateNeighborCells(below, iteration + 1);
                }
                change = true;
            }
        }


        if (change) {
            this.setChanged();
            flagSync();
        }
        return change;
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
    public Side getPanelCellSide(PanelCellPos cellPos,Side panelSide)
    {
        Side cellDirection = cellPos.getPanelTile().getCellFacing(cellPos);

        if (cellDirection == null) return null;

        if (cellDirection==panelSide)
            return Side.FRONT;
        if ((panelSide==Side.TOP||panelSide==Side.BOTTOM)&&cellDirection!=Side.TOP&&cellDirection!=Side.BOTTOM)
            return panelSide;
        if (cellDirection==panelSide.getOpposite())
            return Side.BACK;
        if (cellDirection==panelSide.rotateYCW())
            return Side.LEFT;
        if (cellDirection==panelSide.rotateYCCW())
            return Side.RIGHT;
        if (cellDirection==panelSide.rotateForward())
            return Side.TOP;
        if (cellDirection==panelSide.rotateBack())
            return Side.BOTTOM;

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
        for (Side panelSide : new Side[]{Side.FRONT,Side.RIGHT,Side.BACK,Side.LEFT,Side.TOP}) {
            Direction direction = getDirectionFromSide(panelSide);
            weak=0;strong=0;
            List<Integer> indices = getEdgeCellIndices(direction);
            for (int i:indices) {
                PanelCellPos cellPos = PanelCellPos.fromIndex(this,i);
                IPanelCell cell = cellPos.getIPanelCell();
                Side side = getPanelCellSide(cellPos, direction);
                int cellStrongOutput = cell.getStrongRsOutput(side);
                int cellWeakOutput = cell.getWeakRsOutput(side);

                if (cell.powerDrops() && level.getBlockState(worldPosition.relative(direction)).getBlock() == Blocks.REDSTONE_WIRE) {
                    cellStrongOutput -= 1;
                    cellWeakOutput -= 1;
                }

                if (cell instanceof TinyBlock && level.getBlockState(worldPosition.relative(direction)).getBlock() == Blocks.REDSTONE_WIRE) {
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
            flagSync();
            level.updateNeighborsAt(worldPosition,this.getBlockState().getBlock());
            for (Direction direction : directionsUpdated) {
                BlockPos neighborPos = worldPosition.relative(direction);
                BlockState neighborBlockState = level.getBlockState(neighborPos);
                if (neighborBlockState!=null && neighborBlockState.canOcclude())
                    level.updateNeighborsAtExceptFromFacing(neighborPos,neighborBlockState.getBlock(),direction.getOpposite());
            }
        }

        return change;
    }

    private List<Integer> getEdgeCellIndices(Direction edge) {
        return getEdgeCellIndices(getSideFromDirection(edge));
    }
    private List<Integer> getEdgeCellIndices(Side side){

        List<Integer> cellIndices = new ArrayList<>();

        for (int i1 = 0 ; i1<447.0 ; i1+=64) {
            if (side == Side.LEFT) {
                for (int i = i1; i < i1+8; i++) {
                    if (cells.containsKey(i)) {
                        cellIndices.add(i);
                    }
                }
            } else if (side == Side.FRONT) {
                for (int i = i1; i < i1+64; i += 8) {
                    if (cells.containsKey(i)) {
                        cellIndices.add(i);
                    }
                }
            } else if (side == Side.RIGHT) {
                for (int i = i1+56; i < i1+64; i++) {
                    if (cells.containsKey(i)) {
                        cellIndices.add(i);
                    }
                }
            } else if (side == Side.BACK) {
                for (int i = i1+7; i < i1+64; i += 8) {
                    if (cells.containsKey(i)) {
                        cellIndices.add(i);
                    }
                }
            }
        }
        if (side==Side.TOP)
        {
            for (int i = 384; i < 448; i++) {
                if (cells.containsKey(i)) {
                    cellIndices.add(i);
                }
            }
        }else if (side==Side.BOTTOM)
        {
            for (int i = 0; i < 64; i++) {
                if (cells.containsKey(i)) {
                    cellIndices.add(i);
                }
            }
        }

        return cellIndices;
    }


    public int getColor() {
        if (this.Color == null) {
            return RenderHelper.getTextureDiffusedColor(DyeColor.GRAY);
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
        if (!level.isClientSide && (!isCovered() || panelCover.allowsLightOutput()))
            this.level.sendBlockUpdated(worldPosition,this.getBlockState(),this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public void handleCrash(Exception e)
    {
        if (e instanceof PanelOverflowException pe)
            this.flagOverflow=true;
        else
            this.flagCrashed=true;

        TinyRedstone.LOGGER.error("Redstone Panel Crashed at " + worldPosition.getX() + "," + worldPosition.getY() + "," + worldPosition.getZ(),e);
        this.sync();
    }

    public boolean isCrashed()
    {
        return this.flagCrashed;
    }
    public void resetCrashFlag()
    {
        this.flagCrashed=false;
    }

    public boolean isOverflown()
    {
        return this.flagOverflow;
    }
    public void resetOverflownFlag()
    {
        this.flagOverflow=false;
    }
    public boolean isCovered()
    {
        return panelCover!=null;
    }

    protected Direction getDirectionFromSide(Side side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
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
            case TOP:
                return facing.getOpposite();
            case BOTTOM:
                return facing;
        }
        //this should not be reached
        return null;
    }

    public Side getSideFromDirection(Direction direction) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        switch (direction){
            case NORTH:
                switch (facing) {
                    case DOWN:
                    case EAST:
                    case WEST:
                        return Side.FRONT;
                    case UP:
                        return Side.BACK;
                    case NORTH:
                        return Side.BOTTOM;
                    case SOUTH:
                        return Side.TOP;
                }
                break;
            case EAST:
                switch (facing){
                    case DOWN:
                    case NORTH:
                    case SOUTH:
                    case UP:
                        return Side.RIGHT;
                    case EAST:
                        return Side.BOTTOM;
                    case WEST:
                        return Side.TOP;
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
                    case SOUTH:
                        return Side.BOTTOM;
                    case NORTH:
                        return Side.TOP;
                }
                break;
            case WEST:
                switch (facing){
                    case UP:
                    case DOWN:
                    case NORTH:
                    case SOUTH:
                        return Side.LEFT;
                    case WEST:
                        return Side.BOTTOM;
                    case EAST:
                        return Side.TOP;
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
                    case UP:
                        return Side.BOTTOM;
                    case DOWN:
                        return Side.TOP;
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
                    case DOWN:
                        return Side.BOTTOM;
                    case UP:
                        return Side.TOP;
                }
        }


        return null;
    }

    protected Direction getPlayerDirectionFacing(Player player, boolean allowVertical){

        Direction panelFacing = getBlockState().getValue(BlockStateProperties.FACING);

        Direction[] playerFacings = Direction.orderedByNearest(player);
        for(Direction facing : playerFacings)
        {
            if (allowVertical || (facing!=panelFacing && facing!=panelFacing.getOpposite()))
                return facing;
        }
        return  player.getDirection();

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

    public List<PanelCellPos> getCellPositions()
    {
        List<PanelCellPos> poss = new ArrayList<>();
        for (Integer index : cells.keySet())
        {
            poss.add(PanelCellPos.fromIndex(this,index));
        }
        return poss;
    }

    public int getCellCount()
    {
        return cells.size();
    }

    public void removeCell(PanelCellPos cellPos)
    {
        if (cellPos.getIPanelCell() != null) {
            int cellIndex = cellPos.getIndex();
            boolean isRedstoneDust = cellPos.getIPanelCell() instanceof RedstoneDust;

            //remove from panel
            cellDirections.remove(cellIndex);
            cells.remove(cellIndex);

            BlockPos pos = cellPos.getPanelTile().getBlockPos();
            cellPos.getPanelTile().getLevel().playLocalSound(
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.STONE_BREAK,
                    SoundSource.BLOCKS, 0.15f, 2f, false
            );

            try {
                //let neighbors know about vacancy
                updateNeighborCells(cellPos);
                //for tiny redstone dust, alert the whole neighborhood
                if (isRedstoneDust) {
                    PanelCellPos above = cellPos.offset(Side.TOP),
                            below = cellPos.offset(Side.BOTTOM);
                    if (above != null)
                        updateNeighborCells(above);
                    if (below != null)
                        updateNeighborCells(below);
                }

                clearVoxelShape();
                flagSync();
            }catch (PanelOverflowException e){
                this.handleCrash(e);
            }
        }
    }

    public void removeAllCells(@Nullable Player player){
        Object[] indices = cells.keySet().toArray();
        for (Object index : indices){
            ((PanelBlock)this.getBlockState().getBlock()).removeCell(PanelCellPos.fromIndex(this,(Integer) index),player);
        }
    }

    public void addCell(PanelCellPos cellPos,IPanelCell panelCell, Side facing, Player player) throws PanelOverflowException
    {
        int cellIndex = cellPos.getIndex();
        cellDirections.put(cellIndex, facing);
        cells.put(cellIndex, panelCell);

        BlockPos pos = cellPos.getPanelTile().getBlockPos();
        cellPos.getPanelTile().getLevel().playLocalSound(
                pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.STONE_PLACE,
                SoundSource.BLOCKS, 0.15f, 2f, false
        );

        panelCell.onPlace(cellPos,player);
        updateNeighborCells(cellPos);
        clearVoxelShape();
    }

    public boolean hasCellsOnFace(Direction direction)
    {
        return !getEdgeCellIndices(direction).isEmpty();
    }
    public void flagSync()
    {
        this.flagSync=true;
    }

    public VoxelShape getVoxelShape(){

        if (isCovered())return Shapes.block();

        if (voxelShape==null) {
            switch (this.getBlockState().getValue(BlockStateProperties.FACING)) {
                case UP:
                    voxelShape = Block.box(0, 14, 0, 16, 16, 16);
                    break;
                case NORTH:
                    voxelShape = Block.box(0, 0, 0, 16, 16, 2);
                    break;
                case EAST:
                    voxelShape = Block.box(14, 0, 0, 16, 16, 16);
                    break;
                case SOUTH:
                    voxelShape = Block.box(0, 0, 14, 16, 16, 16);
                    break;
                case WEST:
                    voxelShape = Block.box(0, 0, 0, 2, 16, 16);
                    break;
                default: //DOWN
                    voxelShape = Block.box(0, 0, 0, 16, 2, 16);
            }

            if (!isCovered()) {
                for (Integer index : cells.keySet()) {
                    PanelCellPos cellPos = PanelCellPos.fromIndex(this, index);
                    IPanelCell cell = cellPos.getIPanelCell();
                    if (cell != null) {
                        PanelCellVoxelShape cellShape = cell.getShape();
                        float rowStart = cellPos.getRow() * 2f + (float) cellShape.getPoint1().x * 2f;
                        float rowEnd = cellPos.getRow() * 2f + (float) cellShape.getPoint2().x * 2f;
                        float columnStart = cellPos.getColumn() * 2f + (float) cellShape.getPoint1().z * 2f;
                        float columnEnd = cellPos.getColumn() * 2f + (float) cellShape.getPoint2().z * 2f;
                        float levelStart = 2 + cellPos.getLevel() * 2f + (float) cellShape.getPoint1().y * 2f;
                        float levelEnd = 2 + cellPos.getLevel() * 2f + (float) cellShape.getPoint2().y * 2f;

                        switch (this.getBlockState().getValue(BlockStateProperties.FACING)) {
                            case UP:
                                voxelShape = Shapes.or(voxelShape, Block.box(rowStart, 16 - levelEnd, 16 - columnEnd, rowEnd, 16 - levelStart, 16 - columnStart));
                                break;
                            case NORTH:
                                voxelShape = Shapes.or(voxelShape, Block.box(rowStart, 16 - columnEnd, levelStart, rowEnd, 16 - columnStart, levelEnd));
                                break;
                            case EAST:
                                voxelShape = Shapes.or(voxelShape, Block.box(16 - levelEnd, rowStart, columnStart, 16 - levelStart, rowEnd, columnEnd));
                                break;
                            case SOUTH:
                                voxelShape = Shapes.or(voxelShape, Block.box(rowStart, columnStart, 16 - levelEnd, rowEnd, columnEnd, 16 - levelStart));
                                break;
                            case WEST:
                                voxelShape = Shapes.or(voxelShape, Block.box(levelStart, 16 - rowEnd, columnStart, levelEnd, 16 - rowStart, columnEnd));
                                break;
                            default: //DOWN
                                voxelShape = Shapes.or(voxelShape, Block.box(rowStart, levelStart, columnStart, rowEnd, levelEnd, columnEnd));
                        }
                    }
                }
            }
        }

        return voxelShape;
    }
    public void clearVoxelShape()
    {
        voxelShape=null;
    }
}



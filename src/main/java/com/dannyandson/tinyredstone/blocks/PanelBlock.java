package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IColorablePanelCell;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCover;
import com.dannyandson.tinyredstone.blocks.panelcells.RedstoneDust;
import com.dannyandson.tinyredstone.gui.ClearPanelGUI;
import com.dannyandson.tinyredstone.gui.PanelCrashGUI;
import com.dannyandson.tinyredstone.gui.TinyBlockGUI;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.util.Direction.*;

public class PanelBlock extends Block {

    private static final Map<Direction,VoxelShape> BASE = new HashMap<>();
    static{
        BASE.put(UP ,
                Block.box(0, 16, 0,16, 14, 16)
        );
        BASE.put(DOWN ,
                Block.box(0,0,0,16,2,16)
        );
        BASE.put(NORTH ,
                Block.box(0, 0, 0,16, 16, 2)
        );
        BASE.put(EAST ,
                Block.box(16,0,0,14,16,16)
        );
        BASE.put(SOUTH ,
                Block.box(0,0,16,16,16,14)
        );
        BASE.put(WEST ,
                Block.box(0,0,0,2,16,16)
        );
    }

    private static final Map<Item, Class<? extends IPanelCell>> itemPanelCellMap = new HashMap<>();
    private static final Map<Class<? extends IPanelCell>, Item> panelCellItemMap = new HashMap<>();
    private static final Map<Item, Class<? extends IPanelCover>> itemPanelCoverMap = new HashMap<>();
    private static final Map<Class<? extends IPanelCover>, Item> panelCoverItemMap = new HashMap<>();

    public PanelBlock() {
        super(Properties.of(Material.STONE)
                .sound(SoundType.STONE)
                .strength(2.0f)

        );
    }

    public Class<? extends IPanelCell> getIPanelCellByItem(Item item) {
        return itemPanelCellMap.get(item);
    }

    public Item getItemByIPanelCell(Class<? extends IPanelCell> panelCell) {
        return panelCellItemMap.get(panelCell);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PanelTile();
    }


    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
        builder.add(Registration.HAS_PANEL_BASE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Boolean hasBase = context.getItemInHand().getItem()==Registration.REDSTONE_PANEL_ITEM.get();
        if (context.getItemInHand().hasTag()) {
            CompoundNBT itemTag = context.getItemInHand().getTag().getCompound("BlockEntityTag");
            if (itemTag.contains("hasBase") && !itemTag.getBoolean("hasBase"))
                hasBase=false;
        }
        if(hasBase) {
            return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite()).setValue(Registration.HAS_PANEL_BASE, true);
        }
        return defaultBlockState().setValue(BlockStateProperties.FACING, DOWN).setValue(Registration.HAS_PANEL_BASE,false);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext context) {
        TileEntity te =  source.getBlockEntity(pos);
        if(te instanceof PanelTile)
        {
            PanelTile panelTile = (PanelTile) te;
            if (panelTile.panelCellHovering!=null) {
                VoxelShape cellShape = panelTile.getCellVoxelShape(panelTile.panelCellHovering);
                if (cellShape != null)
                    if (panelTile.hasBase())
                        return VoxelShapes.or(
                                BASE.get(state.getValue(BlockStateProperties.FACING)),
                                cellShape
                        );
                    else
                        return cellShape;
            }
        }
        return getCollisionShape(state, source, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader source, BlockPos pos) {
        TileEntity te =  source.getBlockEntity(pos);
        if(te instanceof PanelTile)
        {
            return ((PanelTile) te).getVoxelShape();
        }

        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext context) {
        TileEntity te =  source.getBlockEntity(pos);
        if(te instanceof PanelTile)
        {
            return ((PanelTile) te).getVoxelShape();
        }
        if (state.hasProperty(Registration.HAS_PANEL_BASE) && state.getValue(Registration.HAS_PANEL_BASE))
            return BASE.get(state.getValue(BlockStateProperties.FACING));
        if (context == ISelectionContext.empty()) {
            //if there's no PanelTile and an empty context, Minecraft is caching the shape for the block state
            //this is used to calculate light blocking and also when mods like Create query block state collisions
            //without providing context
            return Block.box(2, 2, 2, 14, 14, 14);
        }
        return VoxelShapes.empty();
    }

    @Override
    public BlockRenderType getRenderShape(BlockState p_149645_1_) {
        return BlockRenderType.INVISIBLE;
    }

    /**
     * This block can provide redstone power
     * @return true if this block can provide redstone power
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isSignalSource(BlockState iBlockState) {
        return true;
    }

    /**
     * Determine if this block will provide power to redstone and can make a redstone connection on the side provided.
     * Useful to control which sides are outputs for redstone wires.
     * <p>
     * Don't use for inputs; for redstone which is just "passing by", it will make the redstone connect to the side of the block
     * but it won't actually inject weak power into the block.
     *
     * @param blockReader                 The current world
     * @param pos                         Block position in world of the wire that is trying to connect
     * @param directionFromNeighborToThis if not null: the side of the wire that is trying to make a horizontal connection to this block. If null: test for a stepped connection (i.e. the wire is trying to run up or down the side of solid block in order to connect to this block)
     * @return true if this is a power output for redstone, so that redstone wire should connect to it
     */
    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader blockReader, BlockPos pos, @Nullable Direction directionFromNeighborToThis) {
        TileEntity tileentity = blockReader.getBlockEntity(pos);
        if (tileentity instanceof PanelTile && directionFromNeighborToThis != null) {
            PanelTile panelTile = (PanelTile) tileentity;
            Direction facing = directionFromNeighborToThis.getOpposite();

            return panelTile.hasCellsOnFace(facing);

        }
        return super.canConnectRedstone(state, blockReader, pos, directionFromNeighborToThis);
    }

    /**
     * How much weak power does this block provide to the adjacent block?
     * See https://greyminecraftcoder.blogspot.com/2020/05/redstone-1152.html for more information
     *
     * @param blockReader   Minecraft block reader
     * @param pos   the position of this block
     * @param state the blockstate of this block
     * @param directionFromNeighborToThis   eg EAST means that this is to the EAST of the block which is asking for weak power
     * @return The power provided [0 - 15]
     */
    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(BlockState state, IBlockReader blockReader, BlockPos pos, Direction directionFromNeighborToThis) {
        TileEntity tileentity = blockReader.getBlockEntity(pos);
        if (tileentity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileentity;

            Integer power = panelTile.weakPowerToNeighbors.get(panelTile.getSideFromDirection(directionFromNeighborToThis.getOpposite()));

            return (power == null) ? 0 : power;

        }
        return 0;
    }

    /**
     * .
     *
     * @param blockReader Minecraft block reader
     * @param pos                         the position of this block
     * @param state                       the blockstate of this block
     * @param directionFromNeighborToThis eg EAST means that this is to the EAST of the block which is asking for strong power
     * @return The power provided [0 - 15]
     */

    @Override
    @SuppressWarnings("deprecation")
    public int getDirectSignal(BlockState state, IBlockReader blockReader, BlockPos pos, Direction directionFromNeighborToThis) {
        TileEntity tileentity = blockReader.getBlockEntity(pos);
        if (tileentity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileentity;

            //first get strong (direct) power
            Integer power = panelTile.strongPowerToNeighbors.get(panelTile.getSideFromDirection(directionFromNeighborToThis.getOpposite()));

            //if it is not a full signal and vanilla redstone is looking for signals from wires (is it not checking a block for direct power)
            //and panel tiles are checking signals from wires (they are not checking a block for direct power)
            //provide the power provided by tiny redstone dust.
            if ((power==null || power < 15) && Blocks.REDSTONE_WIRE.isSignalSource(state) && PanelTile.getCheckWireSignals()) {
                Integer power2 = panelTile.wirePowerToNeighbors.get(panelTile.getSideFromDirection(directionFromNeighborToThis.getOpposite()));
                power = (power==null||(power2!=null && power2>power))?power2:power;
            }

            return (power == null) ? 0 : power;
        }
        return 0;
    }

    // Called when a neighbouring block changes.
    // Only called on the server side.
    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState currentState, World world, BlockPos pos, Block blockIn, BlockPos neighborPos, boolean isMoving) {

        Direction direction;
        if (pos.east().equals(neighborPos))
            direction = EAST;
        else if (pos.south().equals(neighborPos))
            direction = SOUTH;
        else if (pos.west().equals(neighborPos))
            direction = WEST;
        else if (pos.north().equals(neighborPos))
            direction = NORTH;
        else if (pos.above().equals(neighborPos))
            direction = UP;
        else if (pos.below().equals(neighborPos))
            direction = DOWN;
        else
            return;

        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof PanelTile) {
                boolean change = false;
                PanelTile panelTile = (PanelTile) tileentity;
            try {

                Side side = panelTile.getSideFromDirection(direction);
                if (side != null) {

                    if (panelTile.pingOutwardObservers(direction))
                        change = true;

                    if (panelTile.updateSide(direction))
                        change = true;

                    if (panelTile.updateOutputs()) {
                        if (!world.isClientSide)
                            panelTile.setChanged();
                    }
                    if (change) {
                        panelTile.flagSync();
                    }
                }
            } catch (Exception e) {
                panelTile.handleCrash(e);
            }
        }
    }

    @Override
    public void onRemove(BlockState p_196243_1_, World world, BlockPos pos, BlockState p_196243_4_, boolean p_196243_5_) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileentity;
            panelTile.wirePowerToNeighbors.clear();
            panelTile.weakPowerToNeighbors.clear();
            panelTile.strongPowerToNeighbors.clear();

            world.updateNeighborsAt(pos, this);
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighborBlockState = world.getBlockState(neighborPos);
                if (neighborBlockState != null && neighborBlockState.canOcclude())
                    world.updateNeighborsAt(neighborPos, neighborBlockState.getBlock());
            }
        }

        super.onRemove(p_196243_1_, world, pos, p_196243_4_, p_196243_5_);
    }

    @Nullable
    @Override
    public ToolType getHarvestTool(BlockState state) {
        return ToolType.get("wrench");
    }

    private ItemStack getItemWithNBT(IBlockReader worldIn, BlockPos pos, BlockState state) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileentity;
            ItemStack itemstack = getCloneItemStack(worldIn, pos, state);
            CompoundNBT compoundNBT = panelTile.saveToNbt(new CompoundNBT());
            compoundNBT.putBoolean("hasBase",panelTile.hasBase());
            if (!compoundNBT.isEmpty()) {
                itemstack.addTagElement("BlockEntityTag", compoundNBT);
            }
            return itemstack;
        }
        return null;
    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
     * this block
     */
    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity te = worldIn.getBlockEntity(pos);
        PanelTile panelTile = null;
        if (te instanceof PanelTile){
            panelTile = (PanelTile) te;
            panelTile.onBlockDestroy();
        }
        if(!player.isCreative() && (panelTile==null || panelTile.hasBase() || panelTile.getCellCount()>0)) {
            ItemStack itemstack =
                    (panelTile.getCellCount()>0||panelTile.Color!=DyeColor.GRAY.getColorValue()||panelTile.panelCover!=null)
                            ? getItemWithNBT(worldIn, pos, state)
                            : new ItemStack(this);
            if(itemstack != null) {
                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) te;
            if (!panelTile.isCovered()) {
                PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile, state.getValue(BlockStateProperties.FACING), panelTile.getPlayerCollisionHitResult(player));
                IPanelCell cell = panelTile.getIPanelCell(panelCellPos);
                if (cell != null) {
                    ItemStack itemStack = panelCellItemMap.get(cell.getClass()).getDefaultInstance();
                    CompoundNBT itemTag = cell.getItemTag();
                    if (itemTag != null) {
                        for (String key : itemTag.getAllKeys()) {
                            itemStack.addTagElement(key, itemTag.get(key));
                        }
                    }
                    return itemStack;
                }
            }
        }
        ItemStack itemStack = getItemWithNBT(world, pos, state);
        if(itemStack == null) return super.getPickBlock(state, target, world, pos, player);
        return itemStack;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {

        boolean handled = false;
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof PanelTile && hand==Hand.MAIN_HAND) {
            PanelTile panelTile = (PanelTile) te;
            try {

                PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(panelTile, pos, result);
                Item heldItem = player.getItemInHand(hand).getItem();

                if (panelTile.isCrashed() || panelTile.isOverflown()) {
                    //open crash GUI if on client and panel is in crashed state
                    if (world.isClientSide)
                        PanelCrashGUI.open(panelTile);
                    handled = true;
                } else if ((posInPanelCell==null || posInPanelCell.getIPanelCell()==null) && heldItem == Registration.REDSTONE_WRENCH.get() && !player.isCrouching() && !panelTile.isCovered()) {
                    //rotate panel if holding wrench
                    panelTile.rotate(Rotation.CLOCKWISE_90);
                    handled = true;
                    state.updateNeighbourShapes(world, pos, 3);
                } else if (posInPanelCell != null) {
                    if (heldItem == Registration.REDSTONE_WRENCH.get() && player.isCrouching()) {
                        //harvest block on sneak right click with wrench
                        this.playerWillDestroy(world, pos, state, player);
                        if(!world.isClientSide) world.destroyBlock(pos, true);
                        handled = true;
                    } else if (heldItem == Registration.TINY_COLOR_SELECTOR.get() && posInPanelCell.getIPanelCell() instanceof IColorablePanelCell) {
                        if(world.isClientSide)
                            TinyBlockGUI.open(panelTile, posInPanelCell.getIndex(), (IColorablePanelCell)posInPanelCell.getIPanelCell());
                        handled = true;
                    } else if (heldItem instanceof DyeItem && posInPanelCell.getIPanelCell()==null) {
                        //dye the panel if right clicking with a dye
                        int color = ((DyeItem) heldItem).getDyeColor().getColorValue();
                        if (color != panelTile.Color) {
                            panelTile.Color = ((DyeItem) heldItem).getDyeColor().getColorValue();
                            //remove an item from the player's stack
                            if (!player.isCreative())
                                player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);
                            world.sendBlockUpdated(pos,state,state,0);
                        }
                        handled = true;
                    } else if (heldItem.equals(Items.BARRIER) && player.getScoreboardName().equals("Dev")) {
                        //Allows testing of crash management in Dev environment.
                        throw new Exception("Test Exception");
                    } else if (itemPanelCoverMap.containsKey(heldItem) && !panelTile.isCovered()) {
                        //right clicked with a panel cover
                        try {
                            Object panelCoverObject = itemPanelCoverMap.get(heldItem).getConstructors()[0].newInstance();
                            if (panelCoverObject instanceof IPanelCover) {
                                panelTile.panelCover = (IPanelCover) panelCoverObject;
                                ((IPanelCover) panelCoverObject).onPlace(panelTile,player);
                                panelTile.flagLightUpdate = true;
                                panelTile.flagVoxelShapeUpdate();

                                //do one last sync after covering panel
                                if (!world.isClientSide)
                                    world.sendBlockUpdated(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE);

                                //remove an item from the player's stack
                                if (!player.isCreative())
                                    player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);

                                handled = true;

                            }
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            TinyRedstone.LOGGER.error("Exception thrown while" + e.getMessage());
                        }
                    } else if (posInPanelCell.getIPanelCell() != null && !panelTile.isCovered() && posInPanelCell.getIPanelCell().hasActivation(player) && !player.isCrouching()) {

                        //if player clicked on a panel cell, activate it
                        if (posInPanelCell.getIPanelCell().onBlockActivated(posInPanelCell, posInPanelCell.getSegment(), player)) {
                            panelTile.updateCell(posInPanelCell);
                            panelTile.updateNeighborCells(posInPanelCell);
                            if (posInPanelCell.getIPanelCell() instanceof RedstoneDust) {
                                PanelCellPos above = posInPanelCell.offset(Side.TOP), below = posInPanelCell.offset(Side.BOTTOM);
                                if (above !=null)
                                    panelTile.updateNeighborCells(above);
                                if (below!=null)
                                    panelTile.updateNeighborCells(below);
                            }
                        }
                        handled = true;
                    } else if (itemPanelCellMap.containsKey(heldItem) && !panelTile.isCovered()) {
                        //if player is holding an item registered as a panel cell, try to place that cell on the panel
                        PanelCellPos placementPos = posInPanelCell;
                        if(placementPos.getIPanelCell()!=null)
                        {
                            placementPos = posInPanelCell.offset(panelTile.getSideFromDirection(result.getDirection()));
                        }

                        //but first, check to see if cell exists and is empty
                        if (placementPos!=null && placementPos.getIPanelCell()==null && !panelTile.checkCellForPistonExtension(placementPos)) {
                            try {
                                //catch any exception thrown while attempting to construct from the registered IPanelCell class
                                Object panelCell = itemPanelCellMap.get(heldItem).getConstructors()[0].newInstance();

                                if (panelCell instanceof IPanelCell) {
                                    IPanelCell cell = (IPanelCell) panelCell;
                                    boolean placementOK = true;
                                    Side rotationLock = RotationLock.getServerRotationLock(player);
                                    Side cellFacing = rotationLock == null
                                            ? panelTile.getSideFromDirection(panelTile.getPlayerDirectionFacing(player, cell.canPlaceVertical()))
                                            : rotationLock;

                                    if (cell.needsSolidBase()) {
                                        Side attachingSideDir = panelTile.getSideFromDirection(result.getDirection()).getOpposite();
                                        Side attachingSideRel = (attachingSideDir==Side.TOP || attachingSideDir==Side.BOTTOM)?attachingSideDir:Side.FRONT;
                                        if (
                                            //check if the cell can attach to the side of the block facing
                                                !cell.canAttachToBaseOnSide(attachingSideRel) || (
                                                        //if so, check if it's being placed against a full block
                                                        !posInPanelCell.equals(placementPos) && (
                                                                posInPanelCell.getIPanelCell() == null
                                                                        || !posInPanelCell.getIPanelCell().isPushable()
                                                        )
                                                )
                                        ) {
                                            placementOK = false;
                                        }
                                        else {
                                            //set the direction of the base block
                                            cell.setBaseSide(attachingSideRel);
                                            //set the cell direction to face the base block
                                            if (attachingSideRel==Side.FRONT)
                                                cellFacing=attachingSideDir;
                                        }
                                    }
                                    if (placementOK) {
                                        //place the cell on the panel

                                        placementPos.getPanelTile().addCell(
                                                placementPos,
                                                cell,
                                                cellFacing,
                                                player
                                        );

                                        //remove an item from the player's stack
                                        if (!player.isCreative())
                                            player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);

                                        state.updateNeighbourShapes(world,pos,3);
                                    }
                                    handled = true;
                                }

                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                TinyRedstone.LOGGER.error(e.getMessage());
                            }
                        }
                    } else if (panelTile.isOverflown())
                    {
                        //open crash GUI if on client and panel is in crashed state
                        if (world.isClientSide)
                            PanelCrashGUI.open(panelTile);
                        handled = true;
                    }

                    panelTile.flagSync();
                    if (!world.isClientSide) {
                        panelTile.setChanged();
                    }
                    panelTile.updateOutputs();
                }

            }catch (Exception e)
            {
                panelTile.handleCrash(e);
            }
        }
        if(handled)
            return ActionResultType.CONSUME;
        return super.use(state, world, pos, player, hand, result);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        int ll = 0;
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) te;
            ll=panelTile.getLightOutput();
        }
        return Math.min(ll,world.getMaxLightLevel());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void attack(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        Item heldItem = player.getMainHandItem().getItem();

        if ((heldItem==Registration.REDSTONE_WRENCH.get() || PanelBlock.itemPanelCellMap.containsKey(heldItem) || PanelBlock.itemPanelCoverMap.containsKey(heldItem)))
        {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof PanelTile) {
                PanelTile panelTile = (PanelTile) te;

                try {
                    if (panelTile.isCovered())
                    {
                        removeCover(panelTile,player);
                        panelTile.flagLightUpdate=true;
                    }
                    else {
                        if(heldItem==Registration.REDSTONE_WRENCH.get() && player.isCrouching()) {
                            if(world.isClientSide())
                                ClearPanelGUI.open(panelTile);
                        }
                        else {
                            BlockRayTraceResult result = panelTile.getPlayerCollisionHitResult(player);
                            PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile, state.getValue(BlockStateProperties.FACING), result);

                            if (panelCellPos != null) {
                                if (panelCellPos.getIPanelCell() != null) {
                                    //if player left clicks with wrench, remove cell
                                    removeCell(panelCellPos, player);

                                    state.updateNeighbourShapes(world,pos,3);
                                }
                            }
                        }
                    }
                }catch (Exception e)
                {
                    panelTile.handleCrash(e);
                }
            }
        }
    }

   protected void removeCell(PanelCellPos cellPos, @Nullable PlayerEntity player)
    {
        if (cellPos.getIPanelCell()!=null) {

            PanelTile panelTile = cellPos.getPanelTile();
            World world = panelTile.getLevel();
            BlockPos pos = panelTile.getBlockPos();

            // drop panel cell item
            if (player==null || !player.isCreative()) {
                Item item = panelCellItemMap.get(cellPos.getIPanelCell().getClass());
                ItemStack itemStack = new ItemStack(item);
                CompoundNBT itemTag = cellPos.getIPanelCell().getItemTag();
                if (itemTag!=null){
                    for (String key : itemTag.getAllKeys()){
                        itemStack.addTagElement(key,itemTag.get(key));
                    }
                }
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY()+.5, pos.getZ(), itemStack);
                world.addFreshEntity(itemEntity);
                if (player!=null)
                    itemEntity.setPos(player.getX(),player.getY(),player.getZ());
            }

            //remove from panel
            panelTile.removeCell(cellPos);

            panelTile.getBlockState().updateNeighbourShapes(world,pos,3);
        }

    }

    private void removeCover(PanelTile panelTile,PlayerEntity player)
    {
        if (panelTile.isCovered())
        {
            World world = panelTile.getLevel();
            BlockPos pos = panelTile.getBlockPos();

            // drop panel cell item
            if (!player.isCreative()) {
                Item item = panelCoverItemMap.get(panelTile.panelCover.getClass());
                ItemStack itemStack = new ItemStack(item);
                CompoundNBT coverTag = panelTile.panelCover.getItemTag();
                if (coverTag!=null) {
                    for (String key : coverTag.getAllKeys()) {
                        itemStack.addTagElement(key, coverTag.get(key));
                    }
                }
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY()+.5, pos.getZ(), itemStack);
                world.addFreshEntity(itemEntity);
                itemEntity.setPos(player.getX(),player.getY(),player.getZ());
            }

            //remove from panel
            panelTile.panelCover = null;
            panelTile.flagVoxelShapeUpdate();
            panelTile.flagSync();

        }
    }

    public static void registerPanelCell(Class<? extends IPanelCell> iPanelCellClass, Item correspondingItem)
    {
        itemPanelCellMap.put(correspondingItem,iPanelCellClass);
        panelCellItemMap.put(iPanelCellClass,correspondingItem);
    }

    public static void registerPanelCover(Class<? extends IPanelCover> iPanelCellCover, Item correspondingItem)
    {
        itemPanelCoverMap.put(correspondingItem,iPanelCellCover);
        panelCoverItemMap.put(iPanelCellCover,correspondingItem);
    }

    public static Item getPanelCellItemFromClass(Class<? extends IPanelCell> iPanelCellClass)
    {
        return panelCellItemMap.get(iPanelCellClass);
    }

    public static Class<? extends IPanelCell> getPanelCellClassFromItem(Item item)
    {
        return itemPanelCellMap.get(item);
    }

    public static boolean isPanelCellItem(Item item)
    {
        return itemPanelCellMap.containsKey(item);
    }

}

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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IntegerProperty;
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

    protected static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 15);

    private static final Map<Item, Class<? extends IPanelCell>> itemPanelCellMap = new HashMap<>();
    private static final Map<Class<? extends IPanelCell>, Item> panelCellItemMap = new HashMap<>();
    private static final Map<Item, Class<? extends IPanelCover>> itemPanelCoverMap = new HashMap<>();
    private static final Map<Class<? extends IPanelCover>, Item> panelCoverItemMap = new HashMap<>();

    public PanelBlock() {
        super(Properties.of(Material.STONE)
                .sound(SoundType.STONE)
                .strength(2.0f)
                .lightLevel((state) -> state.getValue(LIGHT_LEVEL))
        );
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_LEVEL, 0));
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
        builder.add(BlockStateProperties.FACING, LIGHT_LEVEL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext context) {
        TileEntity te =  source.getBlockEntity(pos);
        if(te instanceof PanelTile)
        {
            return ((PanelTile) te).getVoxelShape();
        }
        return BASE.get(state.getValue(BlockStateProperties.FACING));
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

            Integer power = panelTile.strongPowerToNeighbors.get(panelTile.getSideFromDirection(directionFromNeighborToThis.getOpposite()));

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
            CompoundNBT compoundnbt = panelTile.saveToNbt(new CompoundNBT());
            if (!compoundnbt.isEmpty()) {
                itemstack.addTagElement("BlockEntityTag", compoundnbt);
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
        if(!player.isCreative()) {
            ItemStack itemstack = getItemWithNBT(worldIn, pos, state);
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
            PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile, state.getValue(BlockStateProperties.FACING), Registration.REDSTONE_WRENCH.get().getBlockRayTraceResult(player.level,player));
            IPanelCell cell = panelTile.getIPanelCell(panelCellPos);
            if (cell != null) {
                return panelCellItemMap.get(cell.getClass()).getDefaultInstance();
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

                if (posInPanelCell != null) {
                    Item heldItem = player.getItemInHand(hand).getItem();

                    if (panelTile.isCrashed()) {
                        //open crash GUI if on client and panel is in crashed state
                        if (world.isClientSide)
                            PanelCrashGUI.open(panelTile);
                        handled = true;
                    } else if (posInPanelCell.getIPanelCell()==null && heldItem == Registration.REDSTONE_WRENCH.get() && !player.isCrouching() && !panelTile.isCovered()) {
                        //rotate panel if holding wrench
                        panelTile.rotate(Rotation.CLOCKWISE_90);
                        handled = true;
                    } else if (heldItem == Registration.REDSTONE_WRENCH.get() && player.isCrouching()) {
                        //harvest block on sneak right click with wrench
                        this.playerWillDestroy(world, pos, state, player);
                        if(!world.isClientSide) world.destroyBlock(pos, true);
                        handled = true;
                    } else if (heldItem == Registration.TINY_COLOR_SELECTOR.get() && posInPanelCell.getIPanelCell() instanceof IColorablePanelCell) {
                        if(world.isClientSide)
                            TinyBlockGUI.open(panelTile, posInPanelCell.getIndex(), (IColorablePanelCell)posInPanelCell.getIPanelCell());
                    } else if (heldItem instanceof DyeItem) {
                        //dye the panel if right clicking with a dye
                        int color = ((DyeItem) heldItem).getDyeColor().getColorValue();
                        if (color != panelTile.Color) {
                            panelTile.Color = ((DyeItem) heldItem).getDyeColor().getColorValue();
                            //remove an item from the player's stack
                            if (!player.isCreative())
                                player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);
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
                                panelTile.flagLightUpdate = true;

                                //do one last sync after covering panel
                                if (!world.isClientSide)
                                    world.sendBlockUpdated(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE);

                                //remove an item from the player's stack
                                if (!player.isCreative())
                                    player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);

                            }
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            TinyRedstone.LOGGER.error("Exception thrown while" + e.getMessage());
                        }
                    } else if (posInPanelCell.getIPanelCell() != null && !panelTile.isCovered() && posInPanelCell.getIPanelCell().hasActivation() && !player.isCrouching()) {

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
                        PanelCellPos pos1 = posInPanelCell;
                        if(pos1.getIPanelCell()!=null)
                        {
                            pos1 = posInPanelCell.offset(panelTile.getSideFromDirection(result.getDirection()));
                        }

                        //but first, check to see if cell exists and is empty
                        if (pos1!=null && pos1.getIPanelCell()==null && !panelTile.checkCellForPistonExtension(pos1)) {
                            try {
                                //catch any exception thrown while attempting to construct from the registered IPanelCell class
                                Object panelCell = itemPanelCellMap.get(heldItem).getConstructors()[0].newInstance();

                                if (panelCell instanceof IPanelCell) {

                                    boolean placementOK = true;

                                    if (((IPanelCell) panelCell).needsSolidBase()) {
                                        PanelCellPos basePos = pos1.offset(Side.BOTTOM);
                                        if (basePos != null && (basePos.getIPanelCell() == null || !basePos.getIPanelCell().isPushable())) {
                                            placementOK = false;
                                        }
                                    }

                                    if (placementOK) {
                                        //place the cell on the panel
                                        Side rotationLock = RotationLock.getServerRotationLock(player);

                                        pos1.getPanelTile().addCell(
                                                pos1,
                                                (IPanelCell) panelCell,
                                                rotationLock == null
                                                        ? panelTile.getSideFromDirection(panelTile.getPlayerDirectionFacing(player, ((IPanelCell) panelCell).canPlaceVertical()))
                                                        : rotationLock,
                                                player
                                        );

                                        //remove an item from the player's stack
                                        if (!player.isCreative())
                                            player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);
                                    }
                                }

                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                TinyRedstone.LOGGER.error(e.getMessage());
                            }
                        }
                        handled = true;
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
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(LIGHT_LEVEL);
    }

    public void setLightValue(World world, BlockPos pos, BlockState state, int lightValue) {
        lightValue = Math.min(lightValue,world.getMaxLightLevel());
        if(state.getValue(LIGHT_LEVEL) == lightValue) return;
        world.setBlockAndUpdate(pos, state.setValue(LIGHT_LEVEL, lightValue));
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
                            BlockRayTraceResult result = Registration.REDSTONE_WRENCH.get().getBlockRayTraceResult(world, player);
                            PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile, state.getValue(BlockStateProperties.FACING), result);

                            if (panelCellPos != null) {
                                if (panelCellPos.getIPanelCell() != null) {
                                    //if player left clicks with wrench, remove cell
                                    removeCell(panelCellPos, player);
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
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY()+.5, pos.getZ(), itemStack);
                world.addFreshEntity(itemEntity);
                if (player!=null)
                    itemEntity.setPos(player.getX(),player.getY(),player.getZ());
            }

            //remove from panel
            panelTile.removeCell(cellPos);

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
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY()+.5, pos.getZ(), itemStack);
                world.addFreshEntity(itemEntity);
                itemEntity.setPos(player.getX(),player.getY(),player.getZ());
            }

            //remove from panel
            panelTile.panelCover = null;

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

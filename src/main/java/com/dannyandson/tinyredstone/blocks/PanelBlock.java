package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.gui.PanelCrashGUI;
import com.dannyandson.tinyredstone.setup.Registration;
import mcp.MethodsReturnNonnullByDefault;
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
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
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
                Block.makeCuboidShape(0, 16, 0,16, 14, 16)
        );
        BASE.put(DOWN ,
                Block.makeCuboidShape(0,0,0,16,2,16)
        );
        BASE.put(NORTH ,
                Block.makeCuboidShape(0, 0, 0,16, 16, 2)
        );
        BASE.put(EAST ,
                Block.makeCuboidShape(16,0,0,14,16,16)
        );
        BASE.put(SOUTH ,
                Block.makeCuboidShape(0,0,16,16,16,14)
        );
        BASE.put(WEST ,
                Block.makeCuboidShape(0,0,0,2,16,16)
        );
    }

    private static final Map<Item, Class<? extends IPanelCell>> itemPanelCellMap = new HashMap<>();
    private static final Map<Class<? extends IPanelCell>, Item> panelCellItemMap = new HashMap<>();
    private static final Map<Item, Class<? extends IPanelCover>> itemPanelCoverMap = new HashMap<>();
    private static final Map<Class<? extends IPanelCover>, Item> panelCoverItemMap = new HashMap<>();

    public PanelBlock() {
        super(Properties.create(Material.ROCK)
                .sound(SoundType.STONE)
                .hardnessAndResistance(2.0f)

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
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(BlockStateProperties.FACING, context.getFace().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext selectionContext) {
        return BASE.get(state.get(BlockStateProperties.FACING));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return BASE.get(state.get(BlockStateProperties.FACING));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return BASE.get(state.get(BlockStateProperties.FACING));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    /**
     * This block can provide redstone power
     * @return true if this block can provide redstone power
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(BlockState iBlockState) {
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
        TileEntity tileentity = blockReader.getTileEntity(pos);
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
    public int getWeakPower(BlockState state, IBlockReader blockReader, BlockPos pos, Direction directionFromNeighborToThis) {
        TileEntity tileentity = blockReader.getTileEntity(pos);
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
    public int getStrongPower(BlockState state, IBlockReader blockReader, BlockPos pos, Direction directionFromNeighborToThis) {
        TileEntity tileentity = blockReader.getTileEntity(pos);
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
        else if (pos.up().equals(neighborPos))
            direction = UP;
        else if (pos.down().equals(neighborPos))
            direction = DOWN;
        else
            return;

        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof PanelTile) {
                boolean change = false;
                PanelTile panelTile = (PanelTile) tileentity;
            try {

                Side side = panelTile.getSideFromDirection(direction);
                //side would be null if neighbor is above or below panel, and therefore irrelevant
                if (side != null) {

                    if (panelTile.pingOutwardObservers(direction))
                        change = true;

                    BlockState neighborState = world.getBlockState(pos.offset(direction));
                    if (neighborState.hasComparatorInputOverride() && !world.isRemote) {
                        panelTile.comparatorOverrides.put(side, neighborState.getComparatorInputOverride(world, pos.offset(direction)));
                    }

                    if (panelTile.updateSide(direction))
                        change = true;

                    if (panelTile.updateOutputs()) {
                        if (!world.isRemote)
                            panelTile.markDirty();
                    }
                    if (change) {
                        panelTile.sync();
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
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileentity;
            ItemStack itemstack = getItem(worldIn, pos, state);
            CompoundNBT compoundnbt = panelTile.saveToNbt(new CompoundNBT());
            if (!compoundnbt.isEmpty()) {
                itemstack.setTagInfo("BlockEntityTag", compoundnbt);
            }
            return itemstack;
        }
        return null;
    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
     * this block
     */
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        if(!player.isCreative()) {
            ItemStack itemstack = getItemWithNBT(worldIn, pos, state);
            if(itemstack != null) {
                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickupDelay();
                worldIn.addEntity(itementity);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) te;
            PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile, state.get(BlockStateProperties.FACING), target.getHitVec());
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
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {

        boolean handled = false;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof PanelTile && hand==Hand.MAIN_HAND) {
            PanelTile panelTile = (PanelTile) te;
            try {
                PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(panelTile, pos, result.getHitVec());

                if (posInPanelCell != null) {
                    Item heldItem = player.getHeldItem(hand).getItem();

                    if (panelTile.isCrashed()) {
                        if (world.isRemote)
                            PanelCrashGUI.open(panelTile);
                        handled = true;
                    } else if (heldItem == Registration.REDSTONE_WRENCH.get() && !player.isSneaking() && !panelTile.isCovered()) {
                        //rotate
                        panelTile.rotate(Rotation.CLOCKWISE_90);
                        handled = true;
                    } else if (heldItem == Registration.REDSTONE_WRENCH.get() && player.isSneaking()) {
                        //harvest block on sneak right click with wrench
                        this.onBlockHarvested(world, pos, state, player);
                        replaceBlock(state, Blocks.AIR.getDefaultState(), world, pos, 1);
                        handled = true;
                    } else if (heldItem instanceof DyeItem) {
                        int color = ((DyeItem) heldItem).getDyeColor().getColorValue();
                        if (color != panelTile.Color) {
                            panelTile.Color = ((DyeItem) heldItem).getDyeColor().getColorValue();
                            //remove an item from the player's stack
                            if (!player.isCreative())
                                player.getHeldItem(hand).setCount(player.getHeldItem(hand).getCount() - 1);
                        }
                        handled = true;
                    } else if (heldItem.equals(Items.BARRIER) && player.getScoreboardName().equals("Dev"))
                    {
                        //Allows testing of crash management in Dev environment.
                        throw new Exception("Test Exception");
                    } else if(result.getFace() == state.get(BlockStateProperties.FACING).getOpposite()) {
                        if(itemPanelCoverMap.containsKey(heldItem) && !panelTile.isCovered()){
                            try {
                                Object panelCoverObject = itemPanelCoverMap.get(heldItem).getConstructors()[0].newInstance();
                                if (panelCoverObject instanceof IPanelCover)
                                {
                                    panelTile.panelCover = (IPanelCover) panelCoverObject;
                                    panelTile.flagLightUpdate=true;

                                    //do one last sync after covering panel
                                    if (!world.isRemote)
                                        world.notifyBlockUpdate(pos,state,state, Constants.BlockFlags.BLOCK_UPDATE);

                                    //remove an item from the player's stack
                                    if (!player.isCreative())
                                        player.getHeldItem(hand).setCount(player.getHeldItem(hand).getCount() - 1);

                                }
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                TinyRedstone.LOGGER.error("Exception thrown while" + e.getMessage());
                            }
                        } else if (posInPanelCell.getIPanelCell()!=null && !panelTile.isCovered()) {
                            if (heldItem == Registration.REDSTONE_WRENCH.get() && player.isSneaking()) {
                                //if player sneak right clicks with wrench, remove cell
                                removeCell(posInPanelCell, panelTile, player);
                            } else {
                                //if player clicked on a panel cell, activate it

                                if(posInPanelCell.getIPanelCell().onBlockActivated(posInPanelCell, posInPanelCell.getSegment())) {
                                    panelTile.updateCell(posInPanelCell);
                                    panelTile.updateNeighborCells(posInPanelCell);
                                }
                            }
                            handled = true;
                        } else if (itemPanelCellMap.containsKey(heldItem) && !panelTile.isCovered()) {
                            //if player is holding an item registered as a panel cell, try to place that cell on the panel

                            //but first, check to see if a piston is extended into that space
                            if (!panelTile.checkCellForPistonExtension(posInPanelCell)) {
                                try {
                                    //catch any exception thrown while attempting to construct from the registered IPanelCell class
                                    Object panelCell = itemPanelCellMap.get(heldItem).getConstructors()[0].newInstance();

                                    if (panelCell instanceof IPanelCell) {
                                        //place the cell on the panel
                                        Direction playerFacing= panelTile.getPlayerDirectionFacing(player);

                                        panelTile.addCell(posInPanelCell,(IPanelCell) panelCell,panelTile.getSideFromDirection(playerFacing));

                                        //remove an item from the player's stack
                                        if (!player.isCreative())
                                            player.getHeldItem(hand).setCount(player.getHeldItem(hand).getCount() - 1);
                                    }

                                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                    TinyRedstone.LOGGER.error(e.getMessage());
                                }
                            }
                            handled = true;
                        }
                    }

                    panelTile.sync();
                    if (!world.isRemote) {
                        panelTile.markDirty();
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
        return super.onBlockActivated(state, world, pos, player, hand, result);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        int ll = 0;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) te;
            ll=panelTile.getLightOutput();
        }
        return Math.min(ll,world.getMaxLightLevel());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        Item heldItem = player.getHeldItemMainhand().getItem();
        double playerReachDistance = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
        Vector3d playerEyes = player.getEyePosition(1f);
        BlockRayTraceResult blockRayTraceResult = world.rayTraceBlocks(new RayTraceContext(
                playerEyes, // from
                playerEyes.add(player.getLook(1f).mul(playerReachDistance, playerReachDistance, playerReachDistance)), // to
                RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE,
                player
        ));
        if ((heldItem==Registration.REDSTONE_WRENCH.get() || PanelBlock.itemPanelCellMap.containsKey(heldItem) || PanelBlock.itemPanelCoverMap.containsKey(heldItem)) && blockRayTraceResult.getFace() == state.get(BlockStateProperties.FACING).getOpposite())
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof PanelTile) {
                PanelTile panelTile = (PanelTile) te;

                try {
                    if (panelTile.isCovered())
                    {
                        removeCover(panelTile,player);
                        panelTile.flagLightUpdate=true;
                    }
                    else {
                        BlockRayTraceResult result = Registration.REDSTONE_WRENCH.get().getBlockRayTraceResult(world, player);
                        PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile,state.get(BlockStateProperties.FACING), result.getHitVec());

                        if (panelCellPos != null) {
                            if (panelCellPos.getIPanelCell()!=null) {
                                //if player left clicks with wrench, remove cell
                                removeCell(panelCellPos, panelTile, player);
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

   private void removeCell(PanelCellPos cellPos, PanelTile panelTile, PlayerEntity player)
    {
        if (cellPos.getIPanelCell()!=null) {

            World world = panelTile.getWorld();
            BlockPos pos = panelTile.getPos();

            // drop panel cell item
            if (!player.isCreative()) {
                Item item = panelCellItemMap.get(cellPos.getIPanelCell().getClass());
                ItemStack itemStack = new ItemStack(item);
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY()+.5, pos.getZ(), itemStack);
                world.addEntity(itemEntity);
                itemEntity.setPosition(player.getPosX(),player.getPosY(),player.getPosZ());
            }

            //remove from panel
            panelTile.removeCell(cellPos);

        }

    }

    private void removeCover(PanelTile panelTile,PlayerEntity player)
    {
        if (panelTile.isCovered())
        {
            World world = panelTile.getWorld();
            BlockPos pos = panelTile.getPos();

            // drop panel cell item
            if (!player.isCreative()) {
                Item item = panelCoverItemMap.get(panelTile.panelCover.getClass());
                ItemStack itemStack = new ItemStack(item);
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY()+.5, pos.getZ(), itemStack);
                world.addEntity(itemEntity);
                itemEntity.setPosition(player.getPosX(),player.getPosY(),player.getPosZ());
            }

            //remove from panel
            panelTile.panelCover = null;

            panelTile.sync();

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

package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.gui.PanelCrashGUI;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.util.Direction.*;

public class PanelBlock extends Block {

    private static final Vector3d BASE_MIN_CORNER = new Vector3d(0.0, 0.0, 0.0);
    private static final Vector3d BASE_MAX_CORNER = new Vector3d(16.0, 2.0, 16.0);
    private static final VoxelShape BASE = Block.makeCuboidShape(BASE_MIN_CORNER.getX(), BASE_MIN_CORNER.getY(), BASE_MIN_CORNER.getZ(),
            BASE_MAX_CORNER.getX(), BASE_MAX_CORNER.getY(), BASE_MAX_CORNER.getZ());

    private static Map<Item, Class<? extends IPanelCell>> itemPanelCellMap = new HashMap<>();
    private static Map<Class<? extends IPanelCell>, Item> panelCellItemMap = new HashMap<>();
    private static Map<Item, Class<? extends IPanelCover>> itemPanelCoverMap = new HashMap<>();
    private static Map<Class<? extends IPanelCover>, Item> panelCoverItemMap = new HashMap<>();

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
        return getDefaultState().with(BlockStateProperties.FACING, context.getNearestLookingDirection());
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext selectionContext) {
        return BASE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return BASE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return BASE;
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

            //row 0 is west
            //column 0 is north

            if (facing == WEST) {
                for (int i = 0; i < 8; i++) {
                    if (panelTile.cells.containsKey(i)) {
                        return true;
                    }
                }
            } else if (facing == NORTH) {
                for (int i = 0; i < 64; i += 8) {
                    if (panelTile.cells.containsKey(i)) {
                        return true;
                    }
                }
            } else if (facing == EAST) {
                for (int i = 56; i < 64; i++) {
                    if (panelTile.cells.containsKey(i)) {
                        return true;
                    }
                }
            } else if (facing == SOUTH) {
                for (int i = 7; i < 64; i += 8) {
                    if (panelTile.cells.containsKey(i)) {
                        return true;
                    }
                }
            }
        }
        return false;
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

            Integer power = panelTile.weakPowerToNeighbors.get(directionFromNeighborToThis.getOpposite());

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

            Integer power = panelTile.strongPowerToNeighbors.get(directionFromNeighborToThis.getOpposite());

            return (power == null) ? 0 : power;
        }
        return 0;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        Direction[] directions = new Direction[]{WEST, EAST, NORTH, SOUTH};
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileentity;

            for (Direction whichFace : directions) {
                BlockPos neighborPos = pos.offset(whichFace);

                int powerLevel = world.getRedstonePower(neighborPos, whichFace.getOpposite());
                int strongPowerLevel = world.getStrongPower(neighborPos, whichFace.getOpposite());

                panelTile.weakPowerFromNeighbors.put(whichFace, powerLevel);
                panelTile.strongPowerFromNeighbors.put(whichFace, strongPowerLevel);

            }
        }
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
        else
            return;

        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof PanelTile) {
            boolean change = false;

            PanelTile panelTile = (PanelTile) tileentity;
            if(panelTile.pingOutwardObservers(direction))
                change=true;

            int powerLevel = world.getRedstonePower(neighborPos, direction);
            int strongPowerLevel = world.getStrongPower(neighborPos, direction);

            BlockState neighborState = world.getBlockState(pos.offset(direction));
            if (neighborState.hasComparatorInputOverride() && !world.isRemote) {
                panelTile.comparatorOverrides.put(direction, neighborState.getComparatorInputOverride(world, pos.offset(direction)));
            }

            panelTile.weakPowerFromNeighbors.put(direction, powerLevel);
            panelTile.strongPowerFromNeighbors.put(direction, strongPowerLevel);
            if(panelTile.updateSide(direction))
                change=true;


            if (panelTile.updateOutputs()) {
                if (!world.isRemote)
                    panelTile.markDirty();
                world.notifyNeighborsOfStateChange(pos, this);
            }
            if (change)
            {
                panelTile.sync();
            }
        }
    }

    @Nullable
    @Override
    public ToolType getHarvestTool(BlockState state) {
        return ToolType.get("wrench");
    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
     * this block
     */
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof PanelTile && !player.isCreative()) {
            PanelTile panelTile = (PanelTile) tileentity;
            if (!worldIn.isRemote) {
                ItemStack itemstack = getItem(worldIn, pos, state);
                CompoundNBT compoundnbt = panelTile.saveToNbt(new CompoundNBT());
                if (!compoundnbt.isEmpty()) {
                    itemstack.setTagInfo("BlockEntityTag", compoundnbt);
                }

                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickupDelay();
                worldIn.addEntity(itementity);
            }
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }


    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {

        boolean handled = false;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof PanelTile && hand==Hand.MAIN_HAND) {
            PanelTile panelTile = (PanelTile) te;
            try {
                PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(pos, result.getHitVec(), panelTile);

                if (posInPanelCell != null) {
                    Item heldItem = player.getHeldItem(hand).getItem();
                    int cellIndex = posInPanelCell.getIndex();

                    if (panelTile.isCrashed()) {
                        if (world.isRemote)
                            PanelCrashGUI.open(panelTile);
                        handled = true;
                    } else if (heldItem == Registration.REDSTONE_WRENCH.get() && !player.isSneaking() && !panelTile.isCovered()) {
                        //rotate
                        panelTile.rotate(Rotation.CLOCKWISE_90);
                        handled = true;
                    }else if(itemPanelCoverMap.containsKey(heldItem) && !panelTile.isCovered()){
                        try {
                            Object panelCoverObject = itemPanelCoverMap.get(heldItem).getConstructors()[0].newInstance();
                            if (panelCoverObject instanceof IPanelCover)
                            {
                                panelTile.panelCover = (IPanelCover) panelCoverObject;

                                //remove an item from the player's stack
                                if (!player.isCreative())
                                    player.getHeldItem(hand).setCount(player.getHeldItem(hand).getCount() - 1);

                            }
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            TinyRedstone.LOGGER.error("Exception thrown while" + e.getMessage());
                        }
                    } else if (panelTile.cells.containsKey(cellIndex) && !panelTile.isCovered()) {
                        if (heldItem == Registration.REDSTONE_WRENCH.get() && player.isSneaking()) {
                            //if player sneak right clicks with wrench, remove cell
                            removeCell(cellIndex, panelTile, player);
                        } else {
                            //if player clicked on a panel cell, activate it

                            if(panelTile.cells.get(cellIndex).onBlockActivated(panelTile, cellIndex, posInPanelCell.getSegment())) {
                                panelTile.updateCell(cellIndex);
                                panelTile.updateNeighborCells(cellIndex);
                            }
                        }
                        handled = true;
                    } else if (heldItem == Registration.REDSTONE_WRENCH.get() && player.isSneaking()) {
                        //harvest block on sneak right click with wrench
                        this.onBlockHarvested(world, pos, state, player);
                        replaceBlock(state, Blocks.AIR.getDefaultState(), world, pos, 1);
                        handled = true;
                    } else if (itemPanelCellMap.containsKey(heldItem) && !panelTile.isCovered()) {
                        //if player is holding an item registered as a panel cell, try to place that cell on the panel

                        //but first, check to see if a piston is extended into that space
                        if (!panelTile.checkCellForPistonExtension(cellIndex)) {
                            try {
                                //catch any exception thrown while attempting to construct from the registered IPanelCell class
                                Object panelCell = itemPanelCellMap.get(heldItem).getConstructors()[0].newInstance();

                                if (panelCell instanceof IPanelCell) {
                                    //place the cell on the panel
                                    panelTile.cellDirections.put(cellIndex, player.getHorizontalFacing());
                                    panelTile.cells.put(cellIndex, (IPanelCell) panelCell);

                                    if (!((IPanelCell) panelCell).isIndependentState()) {
                                        panelTile.updateCell(cellIndex);
                                    }
                                    panelTile.updateNeighborCells(cellIndex);

                                    //remove an item from the player's stack
                                    if (!player.isCreative())
                                        player.getHeldItem(hand).setCount(player.getHeldItem(hand).getCount() - 1);
                                }

                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                TinyRedstone.LOGGER.error(e.getMessage());
                            }
                        }
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
                    }

                    panelTile.sync();
                    if (!world.isRemote) {
                        panelTile.markDirty();
                    }
                    if (panelTile.updateOutputs())
                        world.notifyNeighborsOfStateChange(pos, this);
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
        if (heldItem==Registration.REDSTONE_WRENCH.get() || PanelBlock.itemPanelCellMap.containsKey(heldItem) || PanelBlock.itemPanelCoverMap.containsKey(heldItem))
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof PanelTile) {
                PanelTile panelTile = (PanelTile) te;

                try {
                    if (panelTile.isCovered())
                    {
                        removeCover(panelTile,player);
                    }
                    else {
                        BlockRayTraceResult result = Registration.REDSTONE_WRENCH.get().getBlockRayTraceResult(world, player);
                        PanelCellPos panelCellPos = PanelCellPos.fromHitVec(pos, result.getHitVec());


                        if (panelCellPos != null) {
                            int cellIndex = panelCellPos.getIndex();
                            if (panelTile.cells.containsKey(cellIndex)) {
                                //if player left clicks with wrench, remove cell
                                removeCell(cellIndex, panelTile, player);

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

   private void removeCell(int cellIndex, PanelTile panelTile, PlayerEntity player)
    {
        if (panelTile.cells.containsKey(cellIndex)) {

            World world = panelTile.getWorld();
            BlockPos pos = panelTile.getPos();

            // drop panel cell item
            if (!player.isCreative()) {
                Item item = panelCellItemMap.get(panelTile.cells.get(cellIndex).getClass());
                ItemStack itemStack = new ItemStack(item);
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY()+.5, pos.getZ(), itemStack);
                world.addEntity(itemEntity);
                itemEntity.setPosition(player.getPosX(),player.getPosY(),player.getPosZ());
            }

            //remove from panel
            panelTile.cellDirections.remove(cellIndex);
            panelTile.cells.remove(cellIndex);
            panelTile.updateNeighborCells(cellIndex);

            panelTile.sync();

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

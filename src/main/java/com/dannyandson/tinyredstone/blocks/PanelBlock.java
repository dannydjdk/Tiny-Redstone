package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
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
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.util.Direction.*;

public class PanelBlock extends Block {

    private static final VoxelShape RENDER_SHAPE = VoxelShapes.empty();

    private static final Vector3d BASE_MIN_CORNER = new Vector3d(0.0, 0.0, 0.0);
    private static final Vector3d BASE_MAX_CORNER = new Vector3d(16.0, 2.0, 16.0);
    private static final VoxelShape BASE = Block.makeCuboidShape(BASE_MIN_CORNER.getX(), BASE_MIN_CORNER.getY(), BASE_MIN_CORNER.getZ(),
            BASE_MAX_CORNER.getX(), BASE_MAX_CORNER.getY(), BASE_MAX_CORNER.getZ());

    private static Map<Item, Class<? extends IPanelCell>> itemPanelCellMap = new HashMap<>();
    private static Map<Class<? extends IPanelCell>, Item> panelCellItemMap = new HashMap<>();

    public PanelBlock() {
        super(Properties.create(Material.ROCK)
                .sound(SoundType.STONE)
                .hardnessAndResistance(2.0f)

        );
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

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return BASE;
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return BASE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    /**
     * This block can provide redstone power
     *
     * @return
     */
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
     * The meter provides weak power to the block above it.
     * The meter flashes the power according to how strong the input signals are
     * See https://greyminecraftcoder.blogspot.com/2020/05/redstone-1152.html for more information
     *
     * @param blockReader
     * @param pos                         the position of this block
     * @param state                       the blockstate of this block
     * @param directionFromNeighborToThis eg EAST means that this is to the EAST of the block which is asking for weak power
     * @return The power provided [0 - 15]
     */
    @Override
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
     * @param blockReader
     * @param pos                         the position of this block
     * @param state                       the blockstate of this block
     * @param directionFromNeighborToThis eg EAST means that this is to the EAST of the block which is asking for strong power
     * @return The power provided [0 - 15]
     */

    @Override
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

            if (panelTile.updateOutputs())
                world.notifyNeighborsOfStateChange(pos, this);
        }
    }
    // ------ various block methods that react to changes and are responsible for updating the redstone power information

    // Called when a neighbouring block changes.
    // Only called on the server side- so it doesn't help us alter rendering on the client side.
    @Override
    public void neighborChanged(BlockState currentState, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {

        List<Direction> changedPowerFromNeighbors = new ArrayList<>();
        Direction[] directions = new Direction[]{WEST, EAST, NORTH, SOUTH};
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileentity;
            panelTile.comparatorOverrides.clear();

            for (Direction whichFace : directions) {
                BlockPos neighborPos = pos.offset(whichFace);

                int powerLevel = world.getRedstonePower(neighborPos, whichFace);
                int strongPowerLevel = world.getStrongPower(neighborPos, whichFace);

                BlockState neighborState = world.getBlockState(pos.offset(whichFace));
                if (neighborState.hasComparatorInputOverride()) {
                    powerLevel = neighborState.getComparatorInputOverride(world, pos.offset(whichFace));
                    panelTile.comparatorOverrides.add(whichFace);
                }

                if (panelTile.weakPowerFromNeighbors.get(whichFace) == null || panelTile.weakPowerFromNeighbors.get(whichFace) != powerLevel ||
                        panelTile.strongPowerFromNeighbors.get(whichFace) == null || panelTile.strongPowerFromNeighbors.get(whichFace) != strongPowerLevel) {
                    changedPowerFromNeighbors.add(whichFace);
                    panelTile.weakPowerFromNeighbors.put(whichFace, powerLevel);
                    panelTile.strongPowerFromNeighbors.put(whichFace, strongPowerLevel);
                }
            }

            for (Direction direction : changedPowerFromNeighbors) {
                panelTile.updateSide(direction);
            }
            if (panelTile.updateOutputs()) {
                if (!world.isRemote)
                    panelTile.markDirty();
                world.notifyNeighborsOfStateChange(pos, this);
            }
        }
    }


    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
     * this block
     */
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof PanelTile && !player.isCreative()) {
            PanelTile panelTile = (PanelTile) tileentity;
            if (!worldIn.isRemote /* && player.isCreative() /* && !panelTile.isEmpty() */) {
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

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof PanelTile && hand==Hand.MAIN_HAND) {
            PanelTile panelTile = (PanelTile) te;
            double x = result.getHitVec().x - pos.getX();
            double z = result.getHitVec().z - pos.getZ();
            int row = Math.round((float) (x * 8f) - 0.5f);
            int cell = Math.round((float) (z * 8f) - 0.5f);

            if (row >= 0 && row < 8 && cell >= 0 && cell < 8) {
                Item heldItem = player.getHeldItem(hand).getItem();
                int cellNumber = (row * 8) + cell;

                if (heldItem == Registration.REDSTONE_WRENCH.get() && !player.isSneaking()) {
                    //rotate
                    panelTile.rotate(Rotation.CLOCKWISE_90);
                } else if (panelTile.cells.containsKey(cellNumber)) {
                    if (heldItem == Registration.REDSTONE_WRENCH.get() && player.isSneaking()) {
                        //if player sneak right clicks with wrench, remove cell
                        removeCell(cellNumber, panelTile, player);
                    } else {
                        //if player clicked on a panel cell, activate it
                        double segmentX = (x - (row/8d))*8d;
                        double segmentZ = (z - (cell/8d))*8d;
                        int segmentRow = Math.round((float)(segmentX*3f)-0.5f);
                        int segmentColumn = Math.round((float)(segmentZ*3f)-0.5f);

                        int segmentRow1; int segmentColumn1;
                        Direction cellDirection = panelTile.cellDirections.get(cellNumber);
                        if (cellDirection == NORTH) {
                            segmentRow1 = segmentColumn;
                            segmentColumn1 = ((segmentRow - 1) * -1) + 1;
                        } else if (cellDirection == EAST) {
                            segmentRow1 = ((segmentRow - 1) * -1) + 1;
                            segmentColumn1 = ((segmentColumn - 1) * -1) + 1;
                        } else if (cellDirection == SOUTH){
                            segmentRow1 = ((segmentColumn - 1) * -1) + 1;
                            segmentColumn1 = segmentRow;
                        }
                        else
                        {
                            segmentRow1 = segmentRow;
                            segmentColumn1 = segmentColumn;
                        }

                        int segmentClicked=(segmentRow1*3)+segmentColumn1;

                        if(panelTile.cells.get(cellNumber).onBlockActivated(panelTile, cellNumber, segmentClicked)) {
                            panelTile.updateCell(cellNumber);
                            panelTile.updateNeighborCells(cellNumber);
                        }
                    }
                }else if(heldItem == Registration.REDSTONE_WRENCH.get() && player.isSneaking())
                {
                    //TODO harvest block on sneak right click with wrench
                    //harvestBlock(world,player,pos,state,panelTile,new ItemStack(Registration.REDSTONE_PANEL_ITEM.get()));
                }
                else if (itemPanelCellMap.containsKey(heldItem)) {
                    //if player is holding an item registered as a panel cell, try to place that cell on the panel
                    try {
                        //catch any exception thrown while attempting to construct from the registered IPanelCell class
                        Object panelCell = itemPanelCellMap.get(heldItem).getConstructors()[0].newInstance();

                        if (panelCell instanceof IPanelCell) {
                            //place the cell on the panel
                            panelTile.cellDirections.put(cellNumber, player.getHorizontalFacing());
                            panelTile.cells.put(cellNumber, (IPanelCell) panelCell);

                            if (((IPanelCell) panelCell).isIndependentState())
                            {
                                //for "stateless" cells (such as redstone block), just directly update neighbors
                                panelTile.updateNeighborCells(cellNumber);
                            }
                            else
                                panelTile.updateCell(cellNumber);

                            //remove an item from the player's stack
                            if (!player.isCreative())
                                player.getHeldItem(hand).setCount(player.getHeldItem(hand).getCount() - 1);
                        }

                    } catch (InstantiationException e) {
                        TinyRedstone.LOGGER.error(e.getMessage());
                    } catch (IllegalAccessException e) {
                        TinyRedstone.LOGGER.error(e.getMessage());
                    } catch (InvocationTargetException e) {
                        TinyRedstone.LOGGER.error(e.getMessage());
                    }

                }else if (heldItem instanceof DyeItem)
                {
                    int color = ((DyeItem)heldItem).getDyeColor().getColorValue();
                    if (color!=panelTile.Color) {
                        panelTile.Color = ((DyeItem) heldItem).getDyeColor().getColorValue();
                        //remove an item from the player's stack
                        if (!player.isCreative())
                            player.getHeldItem(hand).setCount(player.getHeldItem(hand).getCount() - 1);
                    }
                }




                panelTile.sync();
                if (!world.isRemote) {
                    panelTile.markDirty();
                } else {
                    //player.sendMessage(ITextComponent.getTextComponentOrEmpty("row:" + row + " cell: " + cell + " index:" + cellNumber), player.getUniqueID());
                }
                if (panelTile.updateOutputs())
                    world.notifyNeighborsOfStateChange(pos, this);
            }

        }
        return super.onBlockActivated(state, world, pos, player, hand, result);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        Item heldItem = player.getHeldItemMainhand().getItem();
        if (heldItem==Registration.REDSTONE_WRENCH.get() || PanelBlock.itemPanelCellMap.containsKey(heldItem))
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof PanelTile) {
                PanelTile panelTile = (PanelTile) te;
                BlockRayTraceResult result = Registration.REDSTONE_WRENCH.get().getBlockRayTraceResult(world, player);

                double x = result.getHitVec().x - pos.getX();
                double z = result.getHitVec().z - pos.getZ();
                int row = Math.round((float) (x * 8f) - 0.5f);
                int cell = Math.round((float) (z * 8f) - 0.5f);

                if (row >= 0 && row < 8 && cell >= 0 && cell < 8) {
                    int cellNumber = (row * 8) + cell;
                    if (panelTile.cells.containsKey(cellNumber)) {
                        //if player left clicks with wrench, remove cell
                        removeCell(cellNumber, panelTile, player);

                    }
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
            }

            //remove from panel
            panelTile.cellDirections.remove(cellIndex);
            panelTile.cells.remove(cellIndex);
            panelTile.updateNeighborCells(cellIndex);

            panelTile.sync();

        }

    }

    public static void registerPanelCell(Class<? extends IPanelCell> iPanelCellClass, Item correspondingItem)
    {
        itemPanelCellMap.put(correspondingItem,iPanelCellClass);
        panelCellItemMap.put(iPanelCellClass,correspondingItem);
    }


}
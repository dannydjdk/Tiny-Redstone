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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.core.Direction.*;

public class PanelBlock extends BaseEntityBlock {

    private static final Map<Direction, VoxelShape> BASE = new HashMap<>();
    static{
        BASE.put(UP ,
                Block.box(0, 14, 0,16, 16, 16)
        );
        BASE.put(DOWN ,
                Block.box(0,0,0,16,2,16)
        );
        BASE.put(NORTH ,
                Block.box(0, 0, 0,16, 16, 2)
        );
        BASE.put(EAST ,
                Block.box(14,0,0,16,16,16)
        );
        BASE.put(SOUTH ,
                Block.box(0,0,14,16,16,16)
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

    public static Item getItemByIPanelCell(Class<? extends IPanelCell> panelCell) {
        return panelCellItemMap.get(panelCell);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new PanelTile(blockPos,blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (level1, blockPos, blockState, t) -> {
            if (t instanceof PanelTile panelTile)
                panelTile.tick();
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
        builder.add(Registration.HAS_PANEL_BASE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Boolean hasBase = context.getItemInHand().getItem()==Registration.REDSTONE_PANEL_ITEM.get();
        if (context.getItemInHand().hasTag()) {
            CompoundTag itemTag = context.getItemInHand().getTag().getCompound("BlockEntityTag");
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
    public VoxelShape getShape(BlockState state, BlockGetter source, BlockPos pos, CollisionContext context) {
        if (source.getBlockEntity(pos) instanceof PanelTile panelTile)
        {
            if (panelTile.panelCellHovering!=null) {
                VoxelShape cellShape = panelTile.getCellVoxelShape(panelTile.panelCellHovering);
                if (cellShape != null)
                    if (panelTile.hasBase())
                        return Shapes.or(
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
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter source, BlockPos pos) {
        BlockEntity te =  source.getBlockEntity(pos);
        if(te instanceof PanelTile)
        {
            return ((PanelTile) te).getVoxelShape();
        }

        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter source, BlockPos pos, CollisionContext context) {
        BlockEntity te =  source.getBlockEntity(pos);
        if(te instanceof PanelTile)
        {
            return ((PanelTile) te).getVoxelShape();
        }
        if (state.hasProperty(Registration.HAS_PANEL_BASE) && state.getValue(Registration.HAS_PANEL_BASE))
            return BASE.get(state.getValue(BlockStateProperties.FACING));
        if (context == CollisionContext.empty()) {
            //if there's no PanelTile and an empty context, Minecraft is caching the shape for the block state
            //this is used to calculate light blocking and also when mods like Create query block state collisions
            //without providing context
            return Block.box(2, 2, 2, 14, 14, 14);
        }
        return Shapes.empty();
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

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction direction) {
        if (world.getBlockEntity(pos) instanceof PanelTile panelTile && direction!=null){
            Direction facing = direction.getOpposite();
            return panelTile.hasCellsOnFace(facing);
        }
        return super.canConnectRedstone(state, world, pos, direction);
    }

    /**
     * Called to determine whether to allow the block to handle its own indirect power rather than using the default rules.
     * @return Whether Block#isProvidingWeakPower should be called when determining indirect power
     */
    @Override
    public boolean shouldCheckWeakPower(BlockState state, LevelReader world, BlockPos pos, Direction directionFromNeighborToThis) {
        //returning false to override default behavior and allow the block entity to specify its redstone output
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
    public int getSignal(BlockState state, BlockGetter blockReader, BlockPos pos, Direction directionFromNeighborToThis) {
        BlockEntity tileentity = blockReader.getBlockEntity(pos);
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
    public int getDirectSignal(BlockState state, BlockGetter blockReader, BlockPos pos, Direction directionFromNeighborToThis) {
        if (blockReader.getBlockEntity(pos) instanceof PanelTile panelTile) {

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
    public void neighborChanged(BlockState currentState, Level world, BlockPos pos, Block blockIn, BlockPos neighborPos, boolean isMoving) {

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

        if (world.getBlockEntity(pos) instanceof PanelTile panelTile) {
                boolean change = false;
            try {

                Side side = panelTile.getSideFromDirection(direction);
                if (side != null) {

                    if (panelTile.pingOutwardObservers(direction))
                        change = true;

                    panelTile.updateSide(direction);

                    if (panelTile.isFlagOutputUpdate()) {
                        panelTile.updateOutputs();
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
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        return true;
    }

    private ItemStack getItemWithNBT(BlockGetter worldIn, BlockPos pos, BlockState state) {
        if (worldIn.getBlockEntity(pos) instanceof PanelTile panelTile) {
            ItemStack itemstack = getCloneItemStack(worldIn, pos, state);
            CompoundTag compoundNBT = panelTile.saveToNbt(new CompoundTag());
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
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        PanelTile panelTile = null;
        if (worldIn.getBlockEntity(pos) instanceof PanelTile pt){
            panelTile=pt;
            panelTile.onBlockDestroy();
        }

        if(!player.isCreative() && (panelTile==null || panelTile.hasBase() || panelTile.getCellCount()>0)) {
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
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) te;
            if (!panelTile.isCovered()) {
                PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile, state.getValue(BlockStateProperties.FACING), panelTile.getPlayerCollisionHitResult(player));
                IPanelCell cell = panelTile.getIPanelCell(panelCellPos);
                if (cell != null) {
                    ItemStack itemStack = panelCellItemMap.get(cell.getClass()).getDefaultInstance();
                    CompoundTag itemTag = cell.getItemTag();
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
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {

        boolean handled = false;
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof PanelTile panelTile && hand==InteractionHand.MAIN_HAND) {
            try {
                blockHitResult = panelTile.getPlayerCollisionHitResult(player);
                PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(panelTile, pos, blockHitResult);

                if (posInPanelCell != null) {
                    Item heldItem = player.getItemInHand(hand).getItem();

                    if (panelTile.isCrashed() || panelTile.isOverflown()) {
                        //open crash GUI if on client and panel is in crashed state
                        if (world.isClientSide)
                            PanelCrashGUI.open(panelTile);
                        handled = true;
                    } else if (posInPanelCell.getIPanelCell()==null && heldItem == Registration.REDSTONE_WRENCH.get() && !player.isCrouching() && !panelTile.isCovered()) {
                        //rotate panel if holding wrench
                        panelTile.rotate(Rotation.CLOCKWISE_90);
                        handled = true;
                        state.updateNeighbourShapes(world,pos,UPDATE_ALL);
                    } else if (heldItem == Registration.REDSTONE_WRENCH.get() && player.isCrouching()) {
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
                        int color = RenderHelper.getTextureDiffusedColor(((DyeItem) heldItem).getDyeColor());
                        if (color != panelTile.Color) {
                            panelTile.Color = color;
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
                                ((IPanelCover) panelCoverObject).onPlace(panelTile,player);
                                panelTile.flagLightUpdate = true;
                                panelTile.flagVoxelShapeUpdate();

                                //do one last sync after covering panel
                                if (!world.isClientSide)
                                    world.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);

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
                            placementPos = posInPanelCell.offset(panelTile.getSideFromDirection(blockHitResult.getDirection()));
                        }

                        //but first, check to see if cell exists and is empty
                        if (placementPos!=null && placementPos.getIPanelCell()==null && !panelTile.checkCellForPistonExtension(placementPos)) {
                            try {
                                //catch any exception thrown while attempting to construct from the registered IPanelCell class
                                Object panelCell = itemPanelCellMap.get(heldItem).getConstructors()[0].newInstance();

                                if (panelCell instanceof IPanelCell cell) {

                                    boolean placementOK = true;

                                    Side rotationLock = RotationLock.getServerRotationLock(player);
                                    Side cellFacing = rotationLock == null
                                            ? panelTile.getSideFromDirection(panelTile.getPlayerDirectionFacing(player, cell.canPlaceVertical()))
                                            : rotationLock;

                                    if (cell.needsSolidBase()) {
                                        Side attachingSideDir = panelTile.getSideFromDirection(blockHitResult.getDirection()).getOpposite();
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

                                        state.updateNeighbourShapes(world,pos,UPDATE_ALL);
                                    }

                                    handled = true;
                                }

                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                //catch any exception thrown while attempting to construct from the registered IPanelCell class
                                //this may happen if an invalid IPanelCell class is registered by Tiny Redstone or an add-on mod
                                //or from an invalid or outdated blueprint
                                TinyRedstone.LOGGER.error(e);
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
                    if (panelTile.isFlagOutputUpdate())
                        panelTile.updateOutputs();
                }

            }catch (Exception e)
            {
                panelTile.handleCrash(e);
            }
        }
        if(handled)
            return InteractionResult.CONSUME;
        return super.use(state, world, pos, player, hand, blockHitResult);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos)
    {
        int ll = 0;
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) te;
            ll=panelTile.getLightOutput();
        }
        return Math.min(ll,world.getMaxLightLevel());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void attack(BlockState state, Level world, BlockPos pos, Player player) {
        Item heldItem = player.getMainHandItem().getItem();

        if ((heldItem==Registration.REDSTONE_WRENCH.get() || PanelBlock.itemPanelCellMap.containsKey(heldItem) || PanelBlock.itemPanelCoverMap.containsKey(heldItem)))
        {
            BlockEntity te = world.getBlockEntity(pos);
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
                            BlockHitResult result = panelTile.getPlayerCollisionHitResult(player);
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

   protected void removeCell(PanelCellPos cellPos, @Nullable Player player)
    {
        if (cellPos.getIPanelCell()!=null) {

            PanelTile panelTile = cellPos.getPanelTile();
            Level world = panelTile.getLevel();
            BlockPos pos = panelTile.getBlockPos();

            // drop panel cell item
            if (player==null || !player.isCreative()) {
                Item item = panelCellItemMap.get(cellPos.getIPanelCell().getClass());
                ItemStack itemStack = new ItemStack(item);
                CompoundTag itemTag = cellPos.getIPanelCell().getItemTag();
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

            panelTile.getBlockState().updateNeighbourShapes(world,pos,UPDATE_ALL);

        }

    }

    private void removeCover(PanelTile panelTile,Player player)
    {
        if (panelTile.isCovered())
        {
            Level world = panelTile.getLevel();
            BlockPos pos = panelTile.getBlockPos();

            // drop panel cell item
            if (!player.isCreative()) {
                Item item = panelCoverItemMap.get(panelTile.panelCover.getClass());
                ItemStack itemStack = new ItemStack(item);
                CompoundTag coverTag = panelTile.panelCover.getItemTag();
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

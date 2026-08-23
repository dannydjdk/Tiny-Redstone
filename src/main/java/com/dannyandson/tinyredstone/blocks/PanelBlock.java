package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.Config;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IColorablePanelCell;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCover;
import com.dannyandson.tinyredstone.blocks.panelcells.RedstoneDust;
import com.dannyandson.tinyredstone.gui.ClearPanelGUI;
import com.dannyandson.tinyredstone.gui.PanelCrashGUI;
import com.dannyandson.tinyredstone.gui.TinyBlockGUI;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.core.Direction.*;

public class PanelBlock extends BaseEntityBlock {

    // --- 1.21+ REQUIRED: codec() for BaseEntityBlock ---
    public static final MapCodec<PanelBlock> CODEC = simpleCodec(PanelBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    // --------------------------------------------------

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

    public PanelBlock(Properties props) {
        super(props);
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
        builder.add(ModRegistration.HAS_PANEL_BASE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Boolean hasBase = context.getItemInHand().getItem()== ModRegistration.REDSTONE_PANEL_ITEM.get();
        CompoundTag itemTag = ItemStackHelper.getBlockEntityTag(context.getItemInHand());
        if (itemTag != null) {
            if (itemTag.contains("hasBase") && !itemTag.getBooleanOr("hasBase", false))
                hasBase=false;
        }
        if(hasBase) {
            return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite()).setValue(ModRegistration.HAS_PANEL_BASE, true);
        }
        return defaultBlockState().setValue(BlockStateProperties.FACING, DOWN).setValue(ModRegistration.HAS_PANEL_BASE,false);
    }

    /**
     * 26.1: Vanilla's automatic item→block entity data transfer only works with
     * DataComponents.BLOCK_ENTITY_DATA. Since we store data in CUSTOM_DATA under
     * a "BlockEntityTag" key, we must manually load it into the block entity on placement.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            CompoundTag itemTag = ItemStackHelper.getBlockEntityTag(stack);
            if (itemTag != null && level.getBlockEntity(pos) instanceof PanelTile panelTile) {
                panelTile.loadFromItemTag(itemTag);
            }
        }
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
    public VoxelShape getOcclusionShape(BlockState state) {
        // 26.1: getOcclusionShape no longer has world access — return empty since
        // panel shapes are dynamic and depend on block entity data
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter source, BlockPos pos, CollisionContext context) {
        BlockEntity te =  source.getBlockEntity(pos);
        if(te instanceof PanelTile)
        {
            return ((PanelTile) te).getVoxelShape();
        }
        if (state.hasProperty(ModRegistration.HAS_PANEL_BASE) && state.getValue(ModRegistration.HAS_PANEL_BASE))
            return BASE.get(state.getValue(BlockStateProperties.FACING));
        if (context == CollisionContext.empty()) {
            return Shapes.empty();
        }
        return Shapes.empty();
    }

    /**
     * Stashed by {@link #rotate} and consumed by {@link PanelTile#loadAdditional} to
     * pair the FACING rotation with the matching cell-grid rotation. Block#rotate has
     * no BlockEntity access, so we hand the rotation off to the BE's NBT-load on the
     * same thread (the pattern Sable sub-level disassembly uses).
     */
    public static final ThreadLocal<Rotation> PENDING_ROTATION = new ThreadLocal<>();

    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation rotation) {
        return doRotate(state, rotation);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        return doRotate(state, rotation);
    }

    private static BlockState doRotate(BlockState state, Rotation rotation) {
        if (rotation == Rotation.NONE) return state;
        PENDING_ROTATION.set(rotation);
        return state.setValue(BlockStateProperties.FACING,
                rotation.rotate(state.getValue(BlockStateProperties.FACING)));
    }

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

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter world, BlockPos pos, Direction directionFromNeighborToThis) {
        return false;
    }

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

    @Override
    @SuppressWarnings("deprecation")
    public int getDirectSignal(BlockState state, BlockGetter blockReader, BlockPos pos, Direction directionFromNeighborToThis) {
        if (blockReader.getBlockEntity(pos) instanceof PanelTile panelTile) {
            Integer power = panelTile.strongPowerToNeighbors.get(panelTile.getSideFromDirection(directionFromNeighborToThis.getOpposite()));

            // Fix: isSignalSource(BlockState) is protected in 1.21. Use defaultBlockState() instead.
            if ((power==null || power < 15) && Blocks.REDSTONE_WIRE.defaultBlockState().isSignalSource() && PanelTile.getCheckWireSignals()) {
                Integer power2 = panelTile.wirePowerToNeighbors.get(panelTile.getSideFromDirection(directionFromNeighborToThis.getOpposite()));
                power = (power==null||(power2!=null && power2>power))?power2:power;
            }

            return (power == null) ? 0 : power;
        }
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState currentState, Level world, BlockPos pos, Block blockIn, @Nullable Orientation orientation, boolean isMoving) {
        if (world.getBlockEntity(pos) instanceof PanelTile panelTile) {
            try {
                // 26.1: Orientation replaces the old neighbor BlockPos parameter.
                // getFront() gives the direction relevant to this block's update.
                // When orientation or direction is null (common for non-redstone updates),
                // update all sides rather than silently ignoring the notification.
                Direction direction = null;
                if (orientation != null && orientation.getFront() != null) {
                    direction = orientation.getFront();
                }

                boolean change = false;

                if (direction != null) {
                    // Targeted update: only the side facing the changed block
                    Side side = panelTile.getSideFromDirection(direction);
                    if (side != null) {
                        if (panelTile.pingOutwardObservers(direction))
                            change = true;
                        panelTile.updateSide(direction);
                    }
                } else {
                    // Fallback: update all sides when we can't determine direction
                    for (Direction dir : Direction.values()) {
                        Side side = panelTile.getSideFromDirection(dir);
                        if (side != null) {
                            if (panelTile.pingOutwardObservers(dir))
                                change = true;
                            panelTile.updateSide(dir);
                        }
                    }
                }

                if (panelTile.isFlagOutputUpdate()) {
                    panelTile.updateOutputs();
                }
                if (panelTile.updateSideConnections() || change) {
                    panelTile.flagSync();
                }
            } catch (Exception e) {
                panelTile.handleCrash(e);
            }
        }
    }

    /**
     * 26.1 NeoForge extension: called when a neighboring block entity changes.
     * Only used to track side connections (whether a neighbor is a panel for rendering).
     * Redstone state propagation between panels is handled directly through the cell
     * offset system in PanelCellPos, not through block update notifications.
     */
    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        if (level instanceof Level world && world.getBlockEntity(pos) instanceof PanelTile panelTile) {
            if (panelTile.updateSideConnections()) {
                panelTile.flagSync();
            }
        }
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        return true;
    }

    private ItemStack getItemWithNBT(LevelReader worldIn, BlockPos pos, BlockState state) {
        if (worldIn.getBlockEntity(pos) instanceof PanelTile panelTile) {
            ItemStack itemstack = new ItemStack(this);
            CompoundTag compoundNBT = panelTile.saveToNbt(new CompoundTag());
            compoundNBT.putBoolean("hasBase", panelTile.hasBase());
            if (!compoundNBT.isEmpty()) {
                compoundNBT.putString("id", "tinyredstone:redstone_panel");
                ItemStackHelper.setBlockEntityTag(itemstack, compoundNBT);
            }
            return itemstack;
        }
        return null;
    }

    /**
     * Does this panel hold anything worth preserving (components, a cover or a custom color)?
     */
    private boolean panelHasContent(PanelTile panelTile) {
        return panelTile.getCellCount()>0||panelTile.Color!=RenderHelper.getTextureDiffusedColor(DyeColor.GRAY)||panelTile.panelCover!=null;
    }


    @Override
    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        PanelTile panelTile = null;
        if (worldIn.getBlockEntity(pos) instanceof PanelTile pt){
            panelTile=pt;
            panelTile.onBlockDestroy();
        }

        ItemStack itemstack = null;

        if(player.isCreative()) {
            //A stray left click in vanilla could destroy hours of circuit work,
            // so drop the panel (with its contents) instead of voiding it (configurable).
            //Blank panels are still voided in creative to avoid unnecessary item litter.
            if(panelTile != null && Config.CREATIVE_PANEL_DROPS.get() && panelTile.getCellCount()>0)
                itemstack = getItemWithNBT(worldIn, pos, state);
        }
        else if(panelTile==null || panelTile.hasBase() || panelTile.getCellCount()>0) {
            itemstack =
                    (panelTile != null && panelHasContent(panelTile))
                            ? getItemWithNBT(worldIn, pos, state)
                            : new ItemStack(this);
        }

        if(itemstack != null) {
            ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
            itementity.setDefaultPickUpDelay();
            worldIn.addFreshEntity(itementity);
        }
        return super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData, Player player) {
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
                        for (String key : itemTag.keySet()) {
                            ItemStackHelper.addTagElement(itemStack, key, itemTag.get(key));
                        }
                    }
                    return itemStack;
                }
            }
        }
        ItemStack itemStack = getItemWithNBT(world, pos, state);
        if(itemStack == null) return super.getCloneItemStack(world,pos,state,includeData,player);
        return itemStack;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {

        boolean handled = false;
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof PanelTile panelTile && hand==InteractionHand.MAIN_HAND) {
            try {
                blockHitResult = panelTile.getPlayerCollisionHitResult(player);
                PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(panelTile, pos, blockHitResult);
                Item heldItem = player.getItemInHand(hand).getItem();

                if ((posInPanelCell == null || posInPanelCell.getIPanelCell()==null) && heldItem.getDefaultInstance().is(ItemTags.create(Identifier.fromNamespaceAndPath("c", "tools/wrench"))) && !player.isCrouching() && !panelTile.isCovered()) {
                    panelTile.rotate(Rotation.CLOCKWISE_90);
                    handled = true;
                    state.updateNeighbourShapes(world, pos, UPDATE_ALL);
                }
                if (posInPanelCell != null) {
                    if (panelTile.isCrashed() || panelTile.isOverflown()) {
                        if (world.isClientSide())
                            PanelCrashGUI.open(panelTile);
                        handled = true;
                    } else if (heldItem.getDefaultInstance().is(ItemTags.create(Identifier.fromNamespaceAndPath("c", "tools/wrench"))) && player.isCrouching()) {
                        this.playerWillDestroy(world, pos, state, player);
                        if(!world.isClientSide()) world.destroyBlock(pos, true);
                        handled = true;
                    } else if (heldItem == ModRegistration.TINY_COLOR_SELECTOR.get() && posInPanelCell.getIPanelCell() instanceof IColorablePanelCell) {
                        if(world.isClientSide())
                            TinyBlockGUI.open(panelTile, posInPanelCell.getIndex(), (IColorablePanelCell)posInPanelCell.getIPanelCell());
                        handled = true;
                    } else if (heldItem instanceof DyeItem && posInPanelCell.getIPanelCell()==null) {
                        DyeColor dyeColor = heldStack.get(net.minecraft.core.component.DataComponents.DYE);
                        int color = dyeColor != null ? RenderHelper.getTextureDiffusedColor(dyeColor) : panelTile.Color;
                        if (color != panelTile.Color) {
                            panelTile.Color = color;
                            if (!player.isCreative())
                                player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);
                        }
                        handled = true;
                    } else if (heldItem.equals(Items.BARRIER) && player.getScoreboardName().equals("Dev")) {
                        throw new Exception("Test Exception");
                    } else if (itemPanelCoverMap.containsKey(heldItem) && !panelTile.isCovered()) {
                        try {
                            Object panelCoverObject = itemPanelCoverMap.get(heldItem).getConstructors()[0].newInstance();
                            if (panelCoverObject instanceof IPanelCover) {
                                panelTile.panelCover = (IPanelCover) panelCoverObject;
                                ((IPanelCover) panelCoverObject).onPlace(panelTile,player);
                                panelTile.flagLightUpdate = true;
                                panelTile.flagVoxelShapeUpdate();
                                if (!world.isClientSide())
                                    world.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                                if (!player.isCreative())
                                    player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);
                                handled = true;
                            }
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            TinyRedstone.LOGGER.error("Exception thrown while" + e.getMessage());
                        }
                    } else if (posInPanelCell.getIPanelCell() != null && !panelTile.isCovered() && posInPanelCell.getIPanelCell().hasActivation(player) && !player.isCrouching()) {
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
                        PanelCellPos placementPos = posInPanelCell;
                        if(placementPos.getIPanelCell()!=null)
                        {
                            placementPos = posInPanelCell.offset(panelTile.getSideFromDirection(blockHitResult.getDirection()));
                        }

                        if (placementPos!=null && placementPos.getIPanelCell()==null && !panelTile.checkCellForPistonExtension(placementPos)) {
                            try {
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
                                                !cell.canAttachToBaseOnSide(attachingSideRel) || (
                                                        !posInPanelCell.equals(placementPos) && (
                                                                posInPanelCell.getIPanelCell() == null
                                                                        || !posInPanelCell.getIPanelCell().isPushable()
                                                        )
                                                )
                                        ) {
                                            placementOK = false;
                                        }
                                        else {
                                            cell.setBaseSide(attachingSideRel);
                                            if (attachingSideRel==Side.FRONT)
                                                cellFacing=attachingSideDir;
                                        }
                                    }

                                    if (placementOK) {
                                        placementPos.getPanelTile().addCell(
                                                placementPos,
                                                cell,
                                                cellFacing,
                                                player
                                        );
                                        if (!player.isCreative())
                                            player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);
                                        state.updateNeighbourShapes(world,pos,UPDATE_ALL);
                                    }

                                    handled = true;
                                }

                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                TinyRedstone.LOGGER.error(e);
                            }
                        }
                    } else if (panelTile.isOverflown())
                    {
                        if (world.isClientSide())
                            PanelCrashGUI.open(panelTile);
                        handled = true;
                    }

                    panelTile.flagSync();
                    if (!world.isClientSide()) {
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
        return super.useItemOn(heldStack, state, world, pos, player, hand, blockHitResult);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos)
    {
        // Use NeoForge's AuxiliaryLightManager for thread-safe, chunk-sync-aware light emission.
        // The light value is set by PanelTile.updateAuxLight() on both server and client.
        if (world instanceof Level level) {
            var lightManager = level.getAuxLightManager(pos);
            if (lightManager != null) {
                return Math.min(lightManager.getLightAt(pos), 15);
            }
        }
        // Fallback for non-Level contexts (e.g. RenderChunkRegion)
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof PanelTile panelTile) {
            return Math.min(panelTile.getLightOutput(), 15);
        }
        return 0;
    }

    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void attack(BlockState state, Level world, BlockPos pos, Player player) {
        Item heldItem = player.getMainHandItem().getItem();

        if ((heldItem.getDefaultInstance().is(ItemTags.create(Identifier.fromNamespaceAndPath("c", "tools/wrench"))) || PanelBlock.itemPanelCellMap.containsKey(heldItem) || PanelBlock.itemPanelCoverMap.containsKey(heldItem)))
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
                        if(heldItem.getDefaultInstance().is(ItemTags.create(Identifier.fromNamespaceAndPath("c", "tools/wrench"))) && player.isCrouching()) {
                            if(world.isClientSide())
                                ClearPanelGUI.open(panelTile);
                        }
                        else {
                            BlockHitResult result = panelTile.getPlayerCollisionHitResult(player);
                            PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile, state.getValue(BlockStateProperties.FACING), result);

                            if (panelCellPos != null) {
                                if (panelCellPos.getIPanelCell() != null) {
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

            if (player==null || !player.isCreative()) {
                Item item = panelCellItemMap.get(cellPos.getIPanelCell().getClass());
                ItemStack itemStack = new ItemStack(item);
                CompoundTag itemTag = cellPos.getIPanelCell().getItemTag();
                if (itemTag!=null){
                    for (String key : itemTag.keySet()){
                        ItemStackHelper.addTagElement(itemStack, key, itemTag.get(key));
                    }
                }
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY()+.5, pos.getZ(), itemStack);
                world.addFreshEntity(itemEntity);
                if (player!=null)
                    itemEntity.setPos(player.getX(),player.getY(),player.getZ());
            }

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

            if (!player.isCreative()) {
                Item item = panelCoverItemMap.get(panelTile.panelCover.getClass());
                ItemStack itemStack = new ItemStack(item);
                CompoundTag coverTag = panelTile.panelCover.getItemTag();
                if (coverTag!=null) {
                    for (String key : coverTag.keySet()) {
                        ItemStackHelper.addTagElement(itemStack, key, coverTag.get(key));
                    }
                }
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY()+.5, pos.getZ(), itemStack);
                world.addFreshEntity(itemEntity);
                itemEntity.setPos(player.getX(),player.getY(),player.getZ());
            }

            panelTile.panelCover = null;
            panelTile.flagLightUpdate = true;
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
package com.dannyandson.tinyredstone.compat.hwyla;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.compat.CompatHandler;
import mcp.mobius.waila.api.*;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.List;

@WailaPlugin(TinyRedstone.MODID)
public class PanelProvider implements IWailaPlugin, IComponentProvider {
    static final ResourceLocation RENDER_STRING = new ResourceLocation("string");
    static final ResourceLocation RENDER_ITEM_INLINE = new ResourceLocation("item_inline");
    static final ResourceLocation RENDER_INFO_STRING = new ResourceLocation("info_string");

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerTooltipRenderer(RENDER_STRING, new TooltipRendererString());
        registrar.registerTooltipRenderer(RENDER_ITEM_INLINE, new TooltipRendererItemStackInline());
        registrar.registerTooltipRenderer(RENDER_INFO_STRING, new TooltipRendererInfoString());
        registrar.registerComponentProvider(this, TooltipPosition.BODY, PanelBlock.class);
    }

    private boolean show(PlayerEntity playerEntity) {
        switch (com.dannyandson.tinyredstone.Config.DISPLAY_MODE.get()) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return playerEntity.isCrouching();
            case 3:
                return CompatHandler.isMeasuringDevice(playerEntity.getMainHandItem().getItem());
            case 4:
                return CompatHandler.isTinyComponent(playerEntity.getMainHandItem().getItem());
        }
        return true;
    }

    @Override
    public ItemStack getStack(IDataAccessor accessor, IPluginConfig config) {
        if(accessor.getBlock() != null) {
            BlockPos pos = accessor.getPosition();
            World world = accessor.getWorld();
            TileEntity tileEntity = world.getBlockEntity(pos);

            if (tileEntity instanceof PanelTile && show(accessor.getPlayer())) {

                PanelTile panelTile = (PanelTile) tileEntity;
                Block block = accessor.getBlock();

                if(!panelTile.isCovered() && block instanceof PanelBlock) {
                    PanelBlock panelBlock = (PanelBlock) block;
                    BlockRayTraceResult result = new BlockRayTraceResult(accessor.getHitResult().getLocation(), accessor.getSide(), pos, true);
                    PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile, accessor.getBlockState().getValue(BlockStateProperties.FACING), result);


                    if (panelCellPos != null && panelCellPos.getIPanelCell() != null) {
                        IPanelCell panelCell = panelCellPos.getIPanelCell();
                        Item item = panelBlock.getItemByIPanelCell(panelCell.getClass());
                        return item.getDefaultInstance();
                    }
                }
            }
        }
        return IComponentProvider.super.getStack(accessor, config);
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if(accessor.getBlock() != null) {
            BlockPos pos = accessor.getPosition();
            TileEntity tileEntity = accessor.getWorld().getBlockEntity(pos);

            if (tileEntity instanceof PanelTile && show(accessor.getPlayer())) {
                PanelTile panelTile = (PanelTile) tileEntity;

                if (!panelTile.isCovered()) {

                    BlockRayTraceResult rtr = new BlockRayTraceResult(accessor.getHitResult().getLocation(),accessor.getSide(),pos,true);
                    PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(panelTile, pos, rtr);

                    if (posInPanelCell != null) {
                        IPanelCell panelCell = posInPanelCell.getIPanelCell();
                        if (panelCell != null) {
                            boolean handled = false;

                            if (panelCell instanceof IPanelCellInfoProvider) {
                                OverlayBlockInfo overlayBlockInfo = new OverlayBlockInfo(tooltip, accessor.getPlayer().isCrouching());
                                ((IPanelCellInfoProvider) panelCell).addInfo(overlayBlockInfo, panelTile, posInPanelCell);
                                if(overlayBlockInfo.power > -1) {
                                    handled = true;
                                    if(overlayBlockInfo.power > 0) {
                                        tooltip.add(new RenderableTextComponent(
                                                getItemStackRenderable(new ItemStack(Items.REDSTONE)),
                                                getStringRenderable("Power: " + overlayBlockInfo.power)
                                        ));
                                    }
                                }
                            }
                            if (!handled) {
                                Side sideHit = panelTile.getPanelCellSide(posInPanelCell,panelTile.getSideFromDirection(accessor.getSide()));
                                int power = panelCell.getWeakRsOutput(sideHit);
                                if(power > 0) tooltip.add(new RenderableTextComponent(
                                        getItemStackRenderable(new ItemStack(Items.REDSTONE)),
                                        getStringRenderable("Power: " + power)
                                ));
                            }
                        }
                    } else {
                        showBlockRedstonePower(tooltip, accessor.getWorld(), pos, accessor.getSide());
                    }
                } else {
                    showBlockRedstonePower(tooltip, accessor.getWorld(), pos, accessor.getSide());
                }
            }
        }
    }

    private static void showBlockRedstonePower(List<ITextComponent> tooltip, World world, BlockPos pos, Direction sideHit) {
        showRedstonePower(tooltip, world.getSignal(pos, sideHit.getOpposite()));
    }

    private static void showRedstonePower(List<ITextComponent> tooltip, int power) {
        if(power > 0) {
            tooltip.add(new RenderableTextComponent(
                    getItemStackRenderable(new ItemStack(Items.REDSTONE)),
                    getStringRenderable("Power: " + power)
            ));
        }
    }

    public static RenderableTextComponent getItemStackRenderable(ItemStack itemStack) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("id", itemStack.getItem().getRegistryName().toString());
        return new RenderableTextComponent(PanelProvider.RENDER_ITEM_INLINE, tag);
    }

    public static RenderableTextComponent getStringRenderable(String string) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("string", string);
        return new RenderableTextComponent(PanelProvider.RENDER_STRING, tag);
    }

    public static RenderableTextComponent getInfoStringRenderable(String string) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("string", string);
        return new RenderableTextComponent(PanelProvider.RENDER_INFO_STRING, tag);
    }
}
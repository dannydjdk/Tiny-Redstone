package com.dannyandson.tinyredstone.compat.theoneprobe;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.*;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import mcjty.theoneprobe.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.function.Function;

public class PanelProvider implements IBlockDisplayOverride, Function<ITheOneProbe, Void>, IProbeInfoProvider {
    private final ResourceLocation MEASURING_DEVICE = new ResourceLocation(TinyRedstone.MODID, "measuring_device");
    private final ResourceLocation TINY_COMPONENT = new ResourceLocation(TinyRedstone.MODID, "tiny_component");
    private IProbeConfig.ConfigMode redstoneMode;

    @Override
    public String getID() {
        return TinyRedstone.MODID + ":panel";
    }

    @Override
    public Void apply(ITheOneProbe theOneProbe) {
        theOneProbe.registerBlockDisplayOverride(this);
        theOneProbe.registerProvider(this);
        return null;
    }

    private boolean show(ProbeMode probeMode, PlayerEntity playerEntity) {
        switch (com.dannyandson.tinyredstone.Config.DISPLAY_MODE.get()) {
            case 0:
                return probeMode == ProbeMode.DEBUG;
            case 1:
                break;
            case 2:
                if(probeMode != ProbeMode.EXTENDED && probeMode != ProbeMode.DEBUG) return false;
                break;
            case 3:
                if(probeMode != ProbeMode.DEBUG) {
                    ITag<Item> tag = ItemTags.getCollection().get(MEASURING_DEVICE);
                    if(tag == null || !tag.contains(playerEntity.getHeldItem(Hand.MAIN_HAND).getItem())) return false;
                }
            case 4:
                if(probeMode != ProbeMode.DEBUG) {
                    ITagCollection<Item> collection = ItemTags.getCollection();
                    ITag<Item> tag = collection.get(MEASURING_DEVICE);
                    if(tag == null || !tag.contains(playerEntity.getHeldItem(Hand.MAIN_HAND).getItem())) {
                        tag = collection.get(TINY_COMPONENT);
                        if(tag == null || !tag.contains(playerEntity.getHeldItem(Hand.MAIN_HAND).getItem())) {
                            return false;
                        }
                    }
                }
        }
        return true;
    }

    @Override
    public boolean overrideStandardInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData probeHitData) {
        BlockPos pos = probeHitData.getPos();
        TileEntity tileEntity = world.getTileEntity(pos);

        IProbeConfig config = Config.getRealConfig();

        if (tileEntity instanceof PanelTile && show(probeMode, playerEntity)) {
            if (redstoneMode == null) redstoneMode = config.getShowRedstone();
            config.showRedstone(IProbeConfig.ConfigMode.NOT);

            PanelTile panelTile = (PanelTile) tileEntity;
            Block block = blockState.getBlock();

            if(!panelTile.isCovered() && block instanceof PanelBlock) {
                PanelBlock panelBlock = (PanelBlock) block;
                BlockRayTraceResult result = new BlockRayTraceResult(probeHitData.getHitVec(),probeHitData.getSideHit(),pos,true);
                PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile,blockState.get(BlockStateProperties.FACING),result);

                if(panelCellPos!=null && panelCellPos.getIPanelCell() != null) {
                    IPanelCell panelCell = panelCellPos.getIPanelCell();
                    String modName = Tools.getModName(block);

                    Item item = panelBlock.getItemByIPanelCell(panelCell.getClass());
                    ItemStack itemStack = item.getDefaultInstance();

                    if (Tools.show(probeMode, config.getShowModName())) {
                        probeInfo.horizontal()
                                .item(itemStack)
                                .vertical()
                                .itemLabel(itemStack)
                                .text(CompoundText.create().style(TextStyleClass.MODNAME).text(modName));
                    } else {
                        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                                .item(itemStack)
                                .itemLabel(itemStack);
                    }
                    return true;
                }
            }
        } else if (redstoneMode != null) {
            config.showRedstone(redstoneMode);
            redstoneMode = null;
        }

        return false;
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData probeHitData) {
        BlockPos pos = probeHitData.getPos();
        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity instanceof PanelTile && show(probeMode, playerEntity)) {
            PanelTile panelTile = (PanelTile) tileEntity;

            if (!panelTile.isCovered()) {

                BlockRayTraceResult rtr = new BlockRayTraceResult(probeHitData.getHitVec(),probeHitData.getSideHit(),pos,true);
                PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(panelTile, pos, rtr);

                if (posInPanelCell != null) {

                    if (probeMode == ProbeMode.DEBUG) {
                        int cellIndex = posInPanelCell.getIndex();
                        PanelCellSegment segment = posInPanelCell.getSegment();

                        probeInfo.vertical(new LayoutStyle().borderColor(0xff44ff44).spacing(2))
                                .text(CompoundText.createLabelInfo("X: ", posInPanelCell.getX()))
                                .text(CompoundText.createLabelInfo("Z: ", posInPanelCell.getZ()))
                                .text(CompoundText.createLabelInfo("Y: ", posInPanelCell.getY()))
                                .text(CompoundText.createLabelInfo("Row: ", posInPanelCell.getRow()))
                                .text(CompoundText.createLabelInfo("Column: ", posInPanelCell.getColumn()))
                                .text(CompoundText.createLabelInfo("Level: ", posInPanelCell.getLevel()))
                                .text(CompoundText.createLabelInfo("Index: ", cellIndex))
                                .text(CompoundText.createLabelInfo("Segment: ", segment))
                                .text(CompoundText.createLabelInfo("Facing: ", posInPanelCell.getCellFacing()));
                    }

                    IPanelCell panelCell = posInPanelCell.getIPanelCell();
                    if (panelCell != null) {
                        boolean handled = false;

                        if (panelCell instanceof IPanelCellProbeInfoProvider) {
                            handled = ((IPanelCellProbeInfoProvider) panelCell).addProbeInfo(probeMode, probeInfo, panelTile, posInPanelCell);
                        }
                        if (!handled) {
                            Side sideHit = panelTile.getPanelCellSide(posInPanelCell,panelTile.getSideFromDirection(probeHitData.getSideHit()));
                            int power = 0;

                            if(sideHit==Side.BACK) {
                                power = panelCell.getWeakRsOutput(Side.BACK);
                            }
                            else if (sideHit==Side.LEFT) {
                                power = panelCell.getWeakRsOutput(Side.LEFT);
                            }
                            else if (sideHit==Side.FRONT) {
                                power = panelCell.getWeakRsOutput(Side.FRONT);
                            }
                            else if (sideHit==Side.RIGHT) {
                                power = panelCell.getWeakRsOutput(Side.RIGHT);
                            }
                            else if (sideHit==Side.BOTTOM) {
                                power = panelCell.getWeakRsOutput(Side.BOTTOM);
                            }
                            else if (sideHit==Side.TOP) {
                                power = panelCell.getWeakRsOutput(Side.TOP);
                            }
                            ProbeInfoHelper.addPower(probeInfo, power);
                        }
                    }
                } else {
                    showRedstonePower(probeInfo, probeMode, world, pos, probeHitData);
                }
            } else {
                showRedstonePower(probeInfo, probeMode, world, pos, probeHitData);
            }
        }
    }

    private void showRedstonePower(IProbeInfo probeInfo, ProbeMode probeMode, World world, BlockPos pos, IProbeHitData probeHitData) {
        if (Tools.show(probeMode, redstoneMode)) {
            ProbeInfoHelper.addPower(probeInfo, world.getRedstonePower(pos, probeHitData.getSideHit().getOpposite()));
        }
    }
}

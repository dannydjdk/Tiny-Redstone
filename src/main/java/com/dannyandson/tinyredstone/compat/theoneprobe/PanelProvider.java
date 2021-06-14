package com.dannyandson.tinyredstone.compat.theoneprobe;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.compat.CompatHandler;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import mcjty.theoneprobe.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.function.Function;

public class PanelProvider implements IBlockDisplayOverride, Function<ITheOneProbe, Void>, IProbeInfoProvider {
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
                if(probeMode == ProbeMode.NORMAL) return false;
                break;
            case 3:
                if(probeMode != ProbeMode.DEBUG) return CompatHandler.isMeasuringDevice(playerEntity.getMainHandItem().getItem());
                break;
            case 4:
                if(probeMode != ProbeMode.DEBUG) return CompatHandler.isTinyComponent(playerEntity.getMainHandItem().getItem());
                break;
        }
        return true;
    }

    @Override
    public boolean overrideStandardInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData probeHitData) {
        BlockPos pos = probeHitData.getPos();
        TileEntity tileEntity = world.getBlockEntity(pos);

        IProbeConfig config = Config.getRealConfig();

        if (tileEntity instanceof PanelTile && show(probeMode, playerEntity)) {

            if (redstoneMode == null) redstoneMode = config.getShowRedstone();
            config.showRedstone(IProbeConfig.ConfigMode.NOT);

            PanelTile panelTile = (PanelTile) tileEntity;
            Block block = blockState.getBlock();

            if(!panelTile.isCovered() && block instanceof PanelBlock) {
                PanelBlock panelBlock = (PanelBlock) block;
                BlockRayTraceResult result = new BlockRayTraceResult(probeHitData.getHitVec(),probeHitData.getSideHit(),pos,true);
                PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile,blockState.getValue(BlockStateProperties.FACING),result);

                if(panelCellPos!=null) {
                    IPanelCell panelCell = panelCellPos.getIPanelCell();
                    if(panelCell != null) {
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
        TileEntity tileEntity = world.getBlockEntity(pos);

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



                        if (panelCell instanceof IPanelCellInfoProvider) {
                            OverlayBlockInfo overlayBlockInfo = new OverlayBlockInfo(probeInfo, probeMode);
                            ((IPanelCellInfoProvider) panelCell).addInfo(overlayBlockInfo, panelTile, posInPanelCell);
                            if(overlayBlockInfo.power > -1) {
                                handled = true;
                                showRedstonePower(probeInfo, overlayBlockInfo.power);
                            }
                        }
                        if (!handled) {
                            Side sideHit = panelTile.getPanelCellSide(posInPanelCell,panelTile.getSideFromDirection(probeHitData.getSideHit()));
                            showRedstonePower(probeInfo, panelCell.getWeakRsOutput(sideHit));
                        }
                    }
                } else {
                    showBlockRedstonePower(probeInfo, probeMode, redstoneMode, world, pos, probeHitData.getSideHit());
                }
            } else {
                showBlockRedstonePower(probeInfo, probeMode, redstoneMode, world, pos, probeHitData.getSideHit());
            }
        }
    }

    private static void showBlockRedstonePower(IProbeInfo probeInfo, ProbeMode probeMode, IProbeConfig.ConfigMode redstoneMode, World world, BlockPos pos, Direction sideHit) {
        if (Tools.show(probeMode, redstoneMode)) {
            showRedstonePower(probeInfo, world.getSignal(pos, sideHit.getOpposite()));
        }
    }

    private static void showRedstonePower(IProbeInfo probeInfo, int power) {
        if (power > 0) {
            probeInfo.horizontal()
                    .item(new ItemStack(Items.REDSTONE), probeInfo.defaultItemStyle().width(14).height(14))
                    .text(CompoundText.createLabelInfo("Power: ", power));
        }
    }
}

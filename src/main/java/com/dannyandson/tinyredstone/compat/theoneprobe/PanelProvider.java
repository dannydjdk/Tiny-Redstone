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
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Function;

public class PanelProvider implements IBlockDisplayOverride, Function<ITheOneProbe, Void>, IProbeInfoProvider {
    private final ResourceLocation MEASURING_DEVICE = new ResourceLocation(TinyRedstone.MODID, "measuring_device");

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

    @Override
    public boolean overrideStandardInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData probeHitData) {
        BlockPos pos = probeHitData.getPos();
        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity instanceof PanelTile && probeHitData.getSideHit() == Direction.UP) {
            switch (com.dannyandson.tinyredstone.Config.DISPLAY_MODE.get()) {
                case 0:
                    break;
                case 1:
                    if(probeMode != ProbeMode.EXTENDED && probeMode != ProbeMode.DEBUG) return false;
                case 2:
                    ITag<Item> tag = ItemTags.getCollection().get(MEASURING_DEVICE);
                    if(tag == null || !tag.contains(playerEntity.getHeldItem(Hand.MAIN_HAND).getItem())) return false;
            }

            PanelTile panelTile = (PanelTile) tileEntity;
            Block block = blockState.getBlock();

            if(!panelTile.isCovered() && block instanceof PanelBlock) {
                PanelBlock panelBlock = (PanelBlock) block;
                PanelCellPos panelCellPos = PanelCellPos.fromHitVec(pos, probeHitData.getHitVec());
                int cellIndex = panelCellPos.getIndex();

                IPanelCell panelCell = panelTile.cells.get(cellIndex);
                if(panelCell != null) {
                    String modName = Tools.getModName(block);
                    IProbeConfig config = Config.getRealConfig();

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
        return false;
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData probeHitData) {
        BlockPos pos = probeHitData.getPos();
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof PanelTile && probeHitData.getSideHit() == Direction.UP) {
            switch (com.dannyandson.tinyredstone.Config.DISPLAY_MODE.get()) {
                case 0:
                    break;
                case 1:
                    if(probeMode != ProbeMode.EXTENDED && probeMode != ProbeMode.DEBUG) return;
                case 2:
                    ITag<Item> tag = ItemTags.getCollection().get(MEASURING_DEVICE);
                    if(tag == null || !tag.contains(playerEntity.getHeldItem(Hand.MAIN_HAND).getItem())) return;
            }

            PanelTile panelTile = (PanelTile) tileEntity;

            if(!panelTile.isCovered()) {

                PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(pos, probeHitData.getHitVec(), panelTile);
                int cellIndex = posInPanelCell.getIndex();

                PanelCellSegment segment = posInPanelCell.getSegment();

                if (probeMode == ProbeMode.DEBUG) {
                    probeInfo.vertical(new LayoutStyle().borderColor(0xff44ff44).spacing(2))
                            .text(CompoundText.createLabelInfo("X: ", posInPanelCell.getX()))
                            .text(CompoundText.createLabelInfo("Z: ", posInPanelCell.getZ()))
                            .text(CompoundText.createLabelInfo("Row: ", posInPanelCell.getRow()))
                            .text(CompoundText.createLabelInfo("Cell: ", posInPanelCell.getCell()))
                            .text(CompoundText.createLabelInfo("Index: ", cellIndex))
                            .text(CompoundText.createLabelInfo("Segment: ", segment.toString()));
                }

                IPanelCell panelCell = panelTile.cells.get(cellIndex);
                if (panelCell != null) {
                    boolean handled = false;

                    if (panelCell instanceof IPanelCellProbeInfoProvider) {
                        handled = ((IPanelCellProbeInfoProvider) panelCell).addProbeInfo(probeMode, probeInfo, panelTile, posInPanelCell);
                    }
                    if (!handled) {
                        int power = 0;
                        switch (segment) {
                            case BACK:
                                power = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.BACK);
                                break;
                            case LEFT:
                                power = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.LEFT);
                                break;
                            case FRONT:
                                power = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.FRONT);
                                break;
                            case RIGHT:
                                power = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.RIGHT);
                                break;
                            case CENTER: {
                                int back = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.BACK);
                                int left = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.LEFT);
                                int front = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.FRONT);
                                int right = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.RIGHT);
                                if (back == left && left == front && front == right) {
                                    power = back;
                                }
                                break;
                            }
                            case FRONT_RIGHT: {
                                int front = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.FRONT);
                                int right = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.RIGHT);
                                if (front == right) power = front;
                                break;
                            }
                            case FRONT_LEFT: {
                                int front = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.FRONT);
                                int left = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.LEFT);
                                if (front == left) power = front;
                                break;
                            }
                            case BACK_RIGHT: {
                                int back = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.BACK);
                                int right = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.RIGHT);
                                if (back == right) power = back;
                                break;
                            }
                            case BACK_LEFT: {
                                int back = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.BACK);
                                int left = panelCell.getWeakRsOutput(IPanelCell.PanelCellSide.LEFT);
                                if (back == left) power = back;
                                break;
                            }
                        }
                        ProbeInfoHelper.addPower(probeInfo, power);
                    }
                }
            }
        }
    }
}

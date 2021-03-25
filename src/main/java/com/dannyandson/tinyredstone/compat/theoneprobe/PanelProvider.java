package com.dannyandson.tinyredstone.compat.theoneprobe;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.helper.PanelCellHelper;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import mcjty.theoneprobe.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.function.Function;

public class PanelProvider implements IBlockDisplayOverride, Function<ITheOneProbe, Void>, IProbeInfoProvider {
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
        Block block = blockState.getBlock();
        TileEntity tileEntity = world.getTileEntity(pos);
        if (block instanceof  PanelBlock && tileEntity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileEntity;
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
        return false;
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData probeHitData) {
        BlockPos pos = probeHitData.getPos();
        Block block = blockState.getBlock();
        TileEntity tileEntity = world.getTileEntity(pos);
        if (block instanceof  PanelBlock && tileEntity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileEntity;

            Vector3d hitVec = probeHitData.getHitVec();
            double x = hitVec.x - pos.getX();
            double z = hitVec.z - pos.getZ();

            PanelCellPos panelCellPos = PanelCellPos.fromCoordinates(x, z);
            int cellIndex = panelCellPos.getIndex();

            if(probeMode == ProbeMode.DEBUG) {
                probeInfo.vertical(new LayoutStyle().borderColor(0xff44ff44).spacing(2))
                        .text(CompoundText.createLabelInfo("Row: ", panelCellPos.getRow()))
                        .text(CompoundText.createLabelInfo("Cell: ", panelCellPos.getCell()))
                        .text(CompoundText.createLabelInfo("Index: ", cellIndex));
            }

            IPanelCell panelCell = panelTile.cells.get(cellIndex);
            boolean handled = false;
            if (panelCell instanceof IPanelCellProbeInfoProvider) {
                handled = ((IPanelCellProbeInfoProvider) panelCell).addProbeInfo(probeMode, probeInfo, panelTile, panelCellPos, Direction.NORTH, PanelCellHelper.getSegment(panelTile.cellDirections.get(cellIndex), x, z, panelCellPos));
            }
            if(!handled) {

            }
            //probeHitData.getSideHit();
        }
    }
}

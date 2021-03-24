package com.dannyandson.tinyredstone.compat.theoneprobe;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.helper.PanelCellHelper;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.function.Function;

public class PanelProvider implements IProbeInfoProvider, Function<ITheOneProbe, Void> {
    @Override
    public String getID() {
        return TinyRedstone.MODID + ":panel";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData probeHitData) {
        BlockPos pos = probeHitData.getPos();
        Block block = blockState.getBlock();
        TileEntity tileEntity = world.getTileEntity(pos);
        if (block instanceof  PanelBlock && tileEntity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileEntity;
            PanelBlock panelBlock = (PanelBlock) block;

            Vector3d hitVec = probeHitData.getHitVec();
            double x = hitVec.x - pos.getX();
            double z = hitVec.z - pos.getZ();
            int row = Math.round((float) (x * 8f) - 0.5f);
            int cell = Math.round((float) (z * 8f) - 0.5f);
            int cellIndex = (row * 8) + cell;

            IPanelCell panelCell = panelTile.cells.get(cellIndex);
            if(panelCell != null) {
                Item item = panelBlock.getItemByIPanelCell(panelCell.getClass());

                probeInfo.horizontal().item(item.getDefaultInstance()).text(item.getName());

                if (panelCell instanceof IPanelCellProbeInfoProvider) {
                    IPanelCellProbeInfoProvider probeInfoProvider = (IPanelCellProbeInfoProvider) panelCell;
                    probeInfoProvider.addProbeInfo(probeMode, probeInfo, panelTile, cellIndex, PanelCellHelper.getSegment(panelTile.cellDirections.get(cellIndex), x, z, row, cell));
                }
            }


            if(probeMode == ProbeMode.DEBUG) {
                probeInfo.vertical(new LayoutStyle().borderColor(0xff44ff44).spacing(2))
                        .text(CompoundText.createLabelInfo("Row: ", row))
                        .text(CompoundText.createLabelInfo("Cell: ", cell))
                        .text(CompoundText.createLabelInfo("Index: ", cellIndex));
            }
        }
    }

    @Override
    public Void apply(ITheOneProbe theOneProbe) {
        theOneProbe.registerProvider(this);
        return null;
    }
}

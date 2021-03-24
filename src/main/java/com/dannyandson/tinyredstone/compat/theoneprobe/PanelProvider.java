package com.dannyandson.tinyredstone.compat.theoneprobe;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
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
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData iProbeHitData) {
        BlockPos pos = iProbeHitData.getPos();
        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileEntity;
            Vector3d hitVec = iProbeHitData.getHitVec();
            double x = hitVec.x - pos.getX();
            double z = hitVec.z - pos.getZ();
            int row = Math.round((float) (x * 8f) - 0.5f);
            int cell = Math.round((float) (z * 8f) - 0.5f);
            int cellIndex = (row * 8) + cell;

            IPanelCell panelCell = panelTile.cells.get(cellIndex);
            if (panelCell != null) {

            }
        }
    }

    @Override
    public Void apply(ITheOneProbe iTheOneProbe) {
        iTheOneProbe.registerProvider(this);
        return null;
    }
}

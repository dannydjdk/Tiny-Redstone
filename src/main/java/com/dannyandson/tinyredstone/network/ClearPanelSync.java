package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearPanelSync {
    private final BlockPos pos;

    public ClearPanelSync(BlockPos pos)
    {
        this.pos=pos;
    }

    public ClearPanelSync(PacketBuffer buffer)
    {
        this.pos= buffer.readBlockPos();
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            TileEntity te =  ctx.get().getSender().getServerWorld().getTileEntity(this.pos);
            if (te instanceof PanelTile)
            {
                ((PanelTile)te).removeAllCells(null);
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }

}

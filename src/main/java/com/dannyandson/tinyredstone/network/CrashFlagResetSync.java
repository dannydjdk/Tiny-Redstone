package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CrashFlagResetSync {
    private final BlockPos pos;

    public CrashFlagResetSync(BlockPos pos)
    {
        this.pos=pos;
    }

    public CrashFlagResetSync(PacketBuffer buffer)
    {
        this.pos= buffer.readBlockPos();
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            TileEntity te =  ctx.get().getSender().getLevel().getBlockEntity(this.pos);
            if (te instanceof PanelTile)
            {
                ((PanelTile)te).resetCrashFlag();
                ((PanelTile)te).resetOverflownFlag();
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }
}

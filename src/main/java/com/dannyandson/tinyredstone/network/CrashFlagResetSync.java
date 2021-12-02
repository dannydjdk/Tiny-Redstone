package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CrashFlagResetSync {
    private final BlockPos pos;

    public CrashFlagResetSync(BlockPos pos)
    {
        this.pos=pos;
    }

    public CrashFlagResetSync(FriendlyByteBuf buffer)
    {
        this.pos= buffer.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            BlockEntity te =  ctx.get().getSender().getLevel().getBlockEntity(this.pos);
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

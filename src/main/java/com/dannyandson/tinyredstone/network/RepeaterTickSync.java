package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.Repeater;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RepeaterTickSync {
    private final BlockPos pos;
    private final int cellIndex;
    private final int ticks;

    public RepeaterTickSync(BlockPos pos, int cellIndex, int ticks)
    {
        this.pos=pos;
        this.cellIndex=cellIndex;
        this.ticks = ticks;
    }

    public RepeaterTickSync(FriendlyByteBuf buffer)
    {
        this.pos= buffer.readBlockPos();
        this.cellIndex=buffer.readInt();
        this.ticks=buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(cellIndex);
        buf.writeInt(ticks);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            BlockEntity te =  ctx.get().getSender().level().getBlockEntity(this.pos);
            if (te instanceof PanelTile)
            {
                PanelCellPos cellPos = PanelCellPos.fromIndex((PanelTile) te,this.cellIndex);
                IPanelCell cell = cellPos.getIPanelCell();
                if (cell instanceof Repeater)
                {
                    ((Repeater)cell).setTicks(this.ticks);
                    ((PanelTile) te).flagSync();
                }
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }
}

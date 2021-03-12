package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.Repeater;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Map;
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

    public RepeaterTickSync(PacketBuffer buffer)
    {
        this.pos= buffer.readBlockPos();
        this.cellIndex=buffer.readInt();
        this.ticks=buffer.readInt();
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(cellIndex);
        buf.writeInt(ticks);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            TileEntity te =  ctx.get().getSender().getServerWorld().getTileEntity(this.pos);
            if (te instanceof PanelTile)
            {
                IPanelCell cell = ((PanelTile)te).cells.get(this.cellIndex);
                if (cell instanceof Repeater)
                {
                    ((Repeater)cell).setTicks(this.ticks);
                    ((PanelTile) te).sync();
                }
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }
}

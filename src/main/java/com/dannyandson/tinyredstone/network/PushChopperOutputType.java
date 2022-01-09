package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.ChopperBlockEntity;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PushChopperOutputType {
    private String type;
    private BlockPos pos;

    public PushChopperOutputType(String type, BlockPos pos) {
        this.type = type;
        this.pos = pos;
    }

    public PushChopperOutputType(PacketBuffer buffer) {
        this.type = buffer.readUtf();
        this.pos = buffer.readBlockPos();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeUtf(type);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = ctx.get().getSender().getLevel().getBlockEntity(this.pos);
            if (te instanceof ChopperBlockEntity) {
                ((ChopperBlockEntity) te).setItemType(type);
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }

}

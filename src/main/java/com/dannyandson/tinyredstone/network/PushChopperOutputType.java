package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.ChopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PushChopperOutputType {
    private String type;
    private BlockPos pos;

    public PushChopperOutputType(String type, BlockPos pos) {
        this.type = type;
        this.pos = pos;
    }

    public PushChopperOutputType(FriendlyByteBuf buffer) {
        this.type = buffer.readUtf();
        this.pos = buffer.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(type);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            BlockEntity te = ctx.get().getSender().getLevel().getBlockEntity(this.pos);
            if (te instanceof ChopperBlockEntity) {
                ((ChopperBlockEntity) te).setItemType(type);
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }

}

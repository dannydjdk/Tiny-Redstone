package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.RotationLock;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RotationLockRemoveSync {
    public RotationLockRemoveSync() {}

    public RotationLockRemoveSync(PacketBuffer buffer) {}

    public void toBytes(PacketBuffer buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()-> {
            RotationLock.removeServerLock(ctx.get().getSender());
            ctx.get().setPacketHandled(true);
        });
        return true;
    }
}

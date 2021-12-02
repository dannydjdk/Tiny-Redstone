package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.RotationLock;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RotationLockRemoveSync {
    public RotationLockRemoveSync() {}

    public RotationLockRemoveSync(FriendlyByteBuf buffer) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()-> {
            RotationLock.removeServerLock(ctx.get().getSender());
            ctx.get().setPacketHandled(true);
        });
        return true;
    }
}

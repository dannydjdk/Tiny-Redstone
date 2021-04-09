package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.blocks.Side;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RotationLockSync {
    private final Side rotationLock;

    public RotationLockSync(Side rotationLock) {
        this.rotationLock = rotationLock;
    }

    public RotationLockSync(PacketBuffer buffer) {
        this.rotationLock = buffer.readEnumValue(Side.class);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeEnumValue(rotationLock);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()-> {
            RotationLock.lockServerRotation(ctx.get().getSender(), rotationLock);
            ctx.get().setPacketHandled(true);
        });
        return true;
    }
}

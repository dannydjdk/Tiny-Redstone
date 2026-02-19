package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.blocks.Side;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RotationLockSync(Side rotationLock) implements CustomPacketPayload {

    public static final Type<RotationLockSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID, "rotation_lock_sync"));

    public static final StreamCodec<FriendlyByteBuf, RotationLockSync> STREAM_CODEC =
            StreamCodec.of(RotationLockSync::write, RotationLockSync::read);

    public static RotationLockSync read(FriendlyByteBuf buf) {
        return new RotationLockSync(buf.readEnum(Side.class));
    }

    public static void write(FriendlyByteBuf buf, RotationLockSync msg) {
        buf.writeEnum(msg.rotationLock);
    }

    public static void handle(RotationLockSync msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                RotationLock.lockServerRotation(player, msg.rotationLock);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

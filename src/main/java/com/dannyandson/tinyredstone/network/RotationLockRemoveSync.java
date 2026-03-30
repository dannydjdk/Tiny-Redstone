package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.RotationLock;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RotationLockRemoveSync() implements CustomPacketPayload {

    public static final Type<RotationLockRemoveSync> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "rotation_lock_remove_sync"));

    public static final StreamCodec<FriendlyByteBuf, RotationLockRemoveSync> STREAM_CODEC =
            StreamCodec.of(RotationLockRemoveSync::write, RotationLockRemoveSync::read);

    public static RotationLockRemoveSync read(FriendlyByteBuf buf) {
        return new RotationLockRemoveSync();
    }

    public static void write(FriendlyByteBuf buf, RotationLockRemoveSync msg) {
        // no data
    }

    public static void handle(RotationLockRemoveSync msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                RotationLock.removeServerLock(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CrashFlagResetSync(BlockPos pos) implements CustomPacketPayload {

    public static final Type<CrashFlagResetSync> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "crash_flag_reset_sync"));

    public static final StreamCodec<FriendlyByteBuf, CrashFlagResetSync> STREAM_CODEC =
            StreamCodec.of(CrashFlagResetSync::write, CrashFlagResetSync::read);

    public static CrashFlagResetSync read(FriendlyByteBuf buf) {
        return new CrashFlagResetSync(buf.readBlockPos());
    }

    public static void write(FriendlyByteBuf buf, CrashFlagResetSync msg) {
        buf.writeBlockPos(msg.pos);
    }

    public static void handle(CrashFlagResetSync msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = ctx.player().level().getBlockEntity(msg.pos);
            if (te instanceof PanelTile panelTile) {
                panelTile.resetCrashFlag();
                panelTile.resetOverflownFlag();
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

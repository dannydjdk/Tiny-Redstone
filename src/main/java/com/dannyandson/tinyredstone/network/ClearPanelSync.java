package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClearPanelSync(BlockPos pos) implements CustomPacketPayload {

    public static final Type<ClearPanelSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID, "clear_panel_sync"));

    public static final StreamCodec<FriendlyByteBuf, ClearPanelSync> STREAM_CODEC =
            StreamCodec.of(ClearPanelSync::write, ClearPanelSync::read);

    public static ClearPanelSync read(FriendlyByteBuf buf) {
        return new ClearPanelSync(buf.readBlockPos());
    }

    public static void write(FriendlyByteBuf buf, ClearPanelSync msg) {
        buf.writeBlockPos(msg.pos);
    }

    public static void handle(ClearPanelSync msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = ctx.player().level().getBlockEntity(msg.pos);
            if (te instanceof PanelTile panelTile) {
                panelTile.removeAllCells(null);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

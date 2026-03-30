package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.Repeater;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RepeaterTickSync(BlockPos pos, int cellIndex, int ticks) implements CustomPacketPayload {

    public static final Type<RepeaterTickSync> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "repeater_tick_sync"));

    public static final StreamCodec<FriendlyByteBuf, RepeaterTickSync> STREAM_CODEC =
            StreamCodec.of(RepeaterTickSync::write, RepeaterTickSync::read);

    public static RepeaterTickSync read(FriendlyByteBuf buf) {
        return new RepeaterTickSync(buf.readBlockPos(), buf.readInt(), buf.readInt());
    }

    public static void write(FriendlyByteBuf buf, RepeaterTickSync msg) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.cellIndex);
        buf.writeInt(msg.ticks);
    }

    public static void handle(RepeaterTickSync msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = ctx.player().level().getBlockEntity(msg.pos);
            if (te instanceof PanelTile panelTile) {
                PanelCellPos cellPos = PanelCellPos.fromIndex(panelTile, msg.cellIndex);
                IPanelCell cell = cellPos.getIPanelCell();
                if (cell instanceof Repeater repeater) {
                    repeater.setTicks(msg.ticks);
                    panelTile.flagSync();
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

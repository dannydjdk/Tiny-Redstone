package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IColorablePanelCell;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TinyBlockColorSync(BlockPos pos, int cellIndex, int color) implements CustomPacketPayload {

    public static final Type<TinyBlockColorSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID, "tiny_block_color_sync"));

    public static final StreamCodec<FriendlyByteBuf, TinyBlockColorSync> STREAM_CODEC =
            StreamCodec.of(TinyBlockColorSync::write, TinyBlockColorSync::read);

    public static TinyBlockColorSync read(FriendlyByteBuf buf) {
        return new TinyBlockColorSync(buf.readBlockPos(), buf.readInt(), buf.readInt());
    }

    public static void write(FriendlyByteBuf buf, TinyBlockColorSync msg) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.cellIndex);
        buf.writeInt(msg.color);
    }

    public static void handle(TinyBlockColorSync msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = ctx.player().level().getBlockEntity(msg.pos);
            if (te instanceof PanelTile panelTile) {
                PanelCellPos cellPos = PanelCellPos.fromIndex(panelTile, msg.cellIndex);
                IPanelCell cell = cellPos.getIPanelCell();
                if (cell instanceof IColorablePanelCell colorable) {
                    colorable.setColor(msg.color);
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

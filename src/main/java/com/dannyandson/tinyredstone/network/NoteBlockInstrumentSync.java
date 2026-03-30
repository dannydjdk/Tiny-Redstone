package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.NoteBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NoteBlockInstrumentSync(BlockPos pos, int cellIndex, String instrument) implements CustomPacketPayload {

    public static final Type<NoteBlockInstrumentSync> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "note_block_instrument_sync"));

    public static final StreamCodec<FriendlyByteBuf, NoteBlockInstrumentSync> STREAM_CODEC =
            StreamCodec.of(NoteBlockInstrumentSync::write, NoteBlockInstrumentSync::read);

    public static NoteBlockInstrumentSync read(FriendlyByteBuf buf) {
        return new NoteBlockInstrumentSync(buf.readBlockPos(), buf.readInt(), buf.readUtf(32));
    }

    public static void write(FriendlyByteBuf buf, NoteBlockInstrumentSync msg) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.cellIndex);
        buf.writeUtf(msg.instrument);
    }

    public static void handle(NoteBlockInstrumentSync msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = ctx.player().level().getBlockEntity(msg.pos);
            if (te instanceof PanelTile panelTile) {
                PanelCellPos cellPos = PanelCellPos.fromIndex(panelTile, msg.cellIndex);
                IPanelCell cell = cellPos.getIPanelCell();
                if (cell instanceof NoteBlock noteBlock) {
                    noteBlock.setInstrument(msg.instrument);
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

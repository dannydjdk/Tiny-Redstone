package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PanelCellSync(BlockPos pos, int cellIndex, CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<PanelCellSync> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "panel_cell_sync"));

    public static final StreamCodec<FriendlyByteBuf, PanelCellSync> STREAM_CODEC =
            StreamCodec.of(PanelCellSync::write, PanelCellSync::read);

    public static PanelCellSync read(FriendlyByteBuf buf) {
        return new PanelCellSync(buf.readBlockPos(), buf.readInt(), buf.readNbt());
    }

    public static void write(FriendlyByteBuf buf, PanelCellSync msg) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.cellIndex);
        buf.writeNbt(msg.nbt);
    }

    public static void handle(PanelCellSync msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = Minecraft.getInstance().level.getBlockEntity(msg.pos);
            if (te instanceof PanelTile panelTile) {
                PanelCellPos cellPos = PanelCellPos.fromIndex(panelTile, msg.cellIndex);
                IPanelCell cell = cellPos.getIPanelCell();
                if (cell != null) {
                    cell.readNBT(msg.nbt);
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

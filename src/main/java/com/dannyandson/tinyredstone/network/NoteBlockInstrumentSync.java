package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.NoteBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NoteBlockInstrumentSync {
    private final BlockPos pos;
    private final int cellIndex;
    private final String instrument;

    public NoteBlockInstrumentSync(BlockPos pos, int cellIndex, String instrument)
    {
        this.pos=pos;
        this.cellIndex=cellIndex;
        this.instrument = instrument;
    }

    public NoteBlockInstrumentSync(FriendlyByteBuf buffer)
    {
        this.pos= buffer.readBlockPos();
        this.cellIndex=buffer.readInt();
        this.instrument =buffer.readUtf(32);
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(cellIndex);
        buf.writeUtf(instrument);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            BlockEntity te =  ctx.get().getSender().level().getBlockEntity(this.pos);
            if (te instanceof PanelTile)
            {
                PanelCellPos cellPos = PanelCellPos.fromIndex((PanelTile) te,this.cellIndex);
                IPanelCell cell = cellPos.getIPanelCell();
                if (cell instanceof NoteBlock)
                {
                    ((NoteBlock)cell).setInstrument(this.instrument);
                    ((PanelTile) te).flagSync();
                }
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }
}

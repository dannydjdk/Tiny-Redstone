package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.NoteBlock;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

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

    public NoteBlockInstrumentSync(PacketBuffer buffer)
    {
        this.pos= buffer.readBlockPos();
        this.cellIndex=buffer.readInt();
        this.instrument =buffer.readString(32);
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(cellIndex);
        buf.writeString(instrument);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            TileEntity te =  ctx.get().getSender().getServerWorld().getTileEntity(this.pos);
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

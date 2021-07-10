package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.api.IColorablePanelCell;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TinyBlockColorSync {
    private final BlockPos pos;
    private final int cellIndex;
    private final int color;

    public TinyBlockColorSync(BlockPos pos, int cellIndex, int color)
    {
        this.pos=pos;
        this.cellIndex=cellIndex;
        this.color = color;
    }

    public TinyBlockColorSync(PacketBuffer buffer)
    {
        this.pos= buffer.readBlockPos();
        this.cellIndex=buffer.readInt();
        this.color =buffer.readInt();
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(cellIndex);
        buf.writeInt(color);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            TileEntity te =  ctx.get().getSender().getLevel().getBlockEntity(this.pos);
            if (te instanceof PanelTile)
            {
                PanelCellPos cellPos = PanelCellPos.fromIndex((PanelTile) te,this.cellIndex);
                IPanelCell cell = cellPos.getIPanelCell();
                if (cell instanceof IColorablePanelCell)
                {
                    ((IColorablePanelCell)cell).setColor(this.color);
                    ((PanelTile) te).flagSync();
                }
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }
}

package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PanelCellSync {
    private final BlockPos pos;
    private final int cellIndex;
    private final CompoundNBT nbt;

    public PanelCellSync(BlockPos pos, int cellIndex, CompoundNBT nbt)
    {
        this.pos=pos;
        this.cellIndex=cellIndex;
        this.nbt = nbt;
    }

    public PanelCellSync(PacketBuffer buffer)
    {
        this.pos= buffer.readBlockPos();
        this.cellIndex=buffer.readInt();
        this.nbt =buffer.readNbt();
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(cellIndex);
        buf.writeNbt(nbt);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            TileEntity te =  Minecraft.getInstance().level.getBlockEntity(this.pos);
            if (te instanceof PanelTile)
            {
                PanelCellPos cellPos = PanelCellPos.fromIndex((PanelTile) te,this.cellIndex);
                IPanelCell cell = cellPos.getIPanelCell();
                if (cell != null)
                {
                    cell.readNBT(this.nbt);
                }
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }
}

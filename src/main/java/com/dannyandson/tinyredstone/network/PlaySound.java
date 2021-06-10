package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlaySound {
    private final BlockPos pos;
    private final String namespace;
    private final String path;
    private final float volume;
    private final float pitch;

    public PlaySound(BlockPos pos, String namespace, String path, float volume, float pitch)
    {
        this.pos=pos;
        this.namespace=namespace;
        this.path=path;
        this.volume=volume;
        this.pitch=pitch;
    }

    public PlaySound(PacketBuffer buffer)
    {
        this.pos= buffer.readBlockPos();
        this.namespace = buffer.readString();
        this.path= buffer.readString();
        this.volume=buffer.readFloat();
        this.pitch=buffer.readFloat();
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeString(this.namespace);
        buf.writeString(this.path);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            TileEntity te = Minecraft.getInstance().world.getTileEntity(this.pos);
            if (te instanceof PanelTile)
            {
                te.getWorld().playSound(
                        pos.getX(), pos.getY(), pos.getZ(),
                        new SoundEvent(new ResourceLocation(namespace,path)),
                        SoundCategory.BLOCKS, volume, pitch, false
                );
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }
}

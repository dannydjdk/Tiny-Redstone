package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class PlaySound {
    private final BlockPos pos;
    private final String namespace;
    private final String path;
    private final float volume;
    private final float pitch;

    public PlaySound(BlockPos pos, String namespace, String path, float volume, float pitch)
    {
        this.pos = pos;
        this.namespace = namespace;
        this.path = path;
        this.volume = volume;
        this.pitch = pitch;
    }

    public PlaySound(FriendlyByteBuf buffer)
    {
        this.pos = buffer.readBlockPos();
        this.namespace = buffer.readUtf();
        this.path = buffer.readUtf();
        this.volume = buffer.readFloat();
        this.pitch = buffer.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeUtf(this.namespace);
        buf.writeUtf(this.path);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            BlockEntity te = Minecraft.getInstance().level.getBlockEntity(this.pos);
            if (te instanceof PanelTile)
            {
                te.getLevel().playLocalSound(
                        pos.getX(), pos.getY(), pos.getZ(),
                        new SoundEvent(new ResourceLocation(namespace,path)),
                        SoundSource.BLOCKS, volume, pitch, false
                );
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }
}

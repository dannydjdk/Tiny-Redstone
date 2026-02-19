package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlaySound(BlockPos pos, String namespace, String path, float volume, float pitch) implements CustomPacketPayload {

    public static final Type<PlaySound> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID, "play_sound"));

    public static final StreamCodec<FriendlyByteBuf, PlaySound> STREAM_CODEC =
            StreamCodec.of(PlaySound::write, PlaySound::read);

    public static PlaySound read(FriendlyByteBuf buf) {
        return new PlaySound(buf.readBlockPos(), buf.readUtf(), buf.readUtf(), buf.readFloat(), buf.readFloat());
    }

    public static void write(FriendlyByteBuf buf, PlaySound msg) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.namespace);
        buf.writeUtf(msg.path);
        buf.writeFloat(msg.volume);
        buf.writeFloat(msg.pitch);
    }

    public static void handle(PlaySound msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = Minecraft.getInstance().level.getBlockEntity(msg.pos);
            if (te instanceof PanelTile) {
                te.getLevel().playLocalSound(
                        msg.pos.getX(), msg.pos.getY(), msg.pos.getZ(),
                        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(msg.namespace, msg.path)),
                        SoundSource.BLOCKS, msg.volume, msg.pitch, false
                );
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

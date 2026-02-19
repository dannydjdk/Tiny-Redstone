package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.ChopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Fix: Record has a field named "type" which conflicts with the required CustomPacketPayload method type().
// Rename the field to "outputType" so the accessor doesn't clash.
public record PushChopperOutputType(String outputType, BlockPos pos) implements CustomPacketPayload {

    public static final Type<PushChopperOutputType> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID, "push_chopper_output_type"));

    public static final StreamCodec<FriendlyByteBuf, PushChopperOutputType> STREAM_CODEC =
            StreamCodec.of(PushChopperOutputType::write, PushChopperOutputType::read);

    public static PushChopperOutputType read(FriendlyByteBuf buf) {
        return new PushChopperOutputType(buf.readUtf(), buf.readBlockPos());
    }

    public static void write(FriendlyByteBuf buf, PushChopperOutputType msg) {
        buf.writeUtf(msg.outputType);
        buf.writeBlockPos(msg.pos);
    }

    public static void handle(PushChopperOutputType msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = ctx.player().level().getBlockEntity(msg.pos);
            if (te instanceof ChopperBlockEntity chopperBlockEntity) {
                chopperBlockEntity.setItemType(msg.outputType);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

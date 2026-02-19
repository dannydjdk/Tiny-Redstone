package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.ChopperBlockEntity;
import com.dannyandson.tinyredstone.codec.TinyBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

/**
 * Server-bound response confirming a tiny block texture is valid on the client.
 */
public record ValidTinyBlockCacheConfirm(ResourceLocation itemRegistryName, @Nullable BlockPos chopperPos) implements CustomPacketPayload {

    public static final Type<ValidTinyBlockCacheConfirm> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID, "valid_tiny_block_cache_confirm"));

    public static final StreamCodec<FriendlyByteBuf, ValidTinyBlockCacheConfirm> STREAM_CODEC =
            StreamCodec.of(ValidTinyBlockCacheConfirm::write, ValidTinyBlockCacheConfirm::read);

    public static ValidTinyBlockCacheConfirm read(FriendlyByteBuf buf) {
        ResourceLocation itemRegistryName = buf.readResourceLocation();
        BlockPos chopperPos = null;
        try {
            chopperPos = buf.readBlockPos();
        } catch (IndexOutOfBoundsException e) {
            // no chopper pos
        }
        return new ValidTinyBlockCacheConfirm(itemRegistryName, chopperPos);
    }

    public static void write(FriendlyByteBuf buf, ValidTinyBlockCacheConfirm msg) {
        buf.writeResourceLocation(msg.itemRegistryName);
        if (msg.chopperPos != null) {
            buf.writeBlockPos(msg.chopperPos);
        }
    }

    public static void handle(ValidTinyBlockCacheConfirm msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!TinyBlockData.validBlockTextureCache.contains(msg.itemRegistryName.toString()))
                TinyBlockData.validBlockTextureCache.add(msg.itemRegistryName.toString());
            if (msg.chopperPos != null) {
                if (ctx.player().level().getBlockEntity(msg.chopperPos) instanceof ChopperBlockEntity chopperBlockEntity)
                    chopperBlockEntity.setChanged();
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

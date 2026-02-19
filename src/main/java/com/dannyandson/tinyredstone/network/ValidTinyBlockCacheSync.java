package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

public record ValidTinyBlockCacheSync(ResourceLocation itemRegistryName, @Nullable BlockPos chopperPos) implements CustomPacketPayload {

    public static final Type<ValidTinyBlockCacheSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID, "valid_tiny_block_cache_sync"));

    public static final StreamCodec<FriendlyByteBuf, ValidTinyBlockCacheSync> STREAM_CODEC =
            StreamCodec.of(ValidTinyBlockCacheSync::write, ValidTinyBlockCacheSync::read);

    public static ValidTinyBlockCacheSync read(FriendlyByteBuf buf) {
        ResourceLocation itemRegistryName = buf.readResourceLocation();
        BlockPos chopperPos = null;
        try {
            chopperPos = buf.readBlockPos();
        } catch (IndexOutOfBoundsException e) {
            // no chopper pos in buffer
        }
        return new ValidTinyBlockCacheSync(itemRegistryName, chopperPos);
    }

    public static void write(FriendlyByteBuf buf, ValidTinyBlockCacheSync msg) {
        buf.writeResourceLocation(msg.itemRegistryName);
        if (msg.chopperPos != null) {
            buf.writeBlockPos(msg.chopperPos);
        }
    }

    public static void handleClient(ValidTinyBlockCacheSync msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ResourceLocation texture1 = ResourceLocation.fromNamespaceAndPath(msg.itemRegistryName.getNamespace(), "block/" + msg.itemRegistryName.getPath());
            ResourceLocation texture2 = ResourceLocation.fromNamespaceAndPath(msg.itemRegistryName.getNamespace(), "block/" + msg.itemRegistryName.getPath() + "_side");
            TextureAtlasSprite sprite1 = RenderHelper.getSprite(texture1);
            TextureAtlasSprite sprite2 = RenderHelper.getSprite(texture2);
            if (sprite1 != RenderHelper.getSprite(TextureManager.INTENTIONAL_MISSING_TEXTURE) || sprite2 != RenderHelper.getSprite(TextureManager.INTENTIONAL_MISSING_TEXTURE)) {
                // Fix: ValidTinyBlockCacheConfirm constructor order is (ResourceLocation, BlockPos)
                ModNetworkHandler.sendToServer(new ValidTinyBlockCacheConfirm(msg.itemRegistryName, msg.chopperPos));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

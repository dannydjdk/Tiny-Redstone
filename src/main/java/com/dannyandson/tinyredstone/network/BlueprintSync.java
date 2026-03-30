package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import com.dannyandson.tinyredstone.items.Blueprint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BlueprintSync(CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<BlueprintSync> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "blueprint_sync"));

    public static final StreamCodec<FriendlyByteBuf, BlueprintSync> STREAM_CODEC =
            StreamCodec.of(BlueprintSync::write, BlueprintSync::read);

    public static BlueprintSync read(FriendlyByteBuf buf) {
        return new BlueprintSync(buf.readNbt());
    }

    public static void write(FriendlyByteBuf buf, BlueprintSync msg) {
        buf.writeNbt(msg.nbt);
    }

    public static void handle(BlueprintSync msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                ItemStack blueprint = player.getMainHandItem();
                if (blueprint.getItem() instanceof Blueprint) {
                    ItemStackHelper.setCustomTag(blueprint, msg.nbt);
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

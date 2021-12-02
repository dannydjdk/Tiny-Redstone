package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.items.Blueprint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BlueprintSync {

    private final CompoundTag nbt;

    public BlueprintSync(CompoundTag nbt)
    {
        this.nbt=nbt;
    }

    public BlueprintSync(FriendlyByteBuf buffer)
    {
        this.nbt=buffer.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeNbt(nbt);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()-> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack blueprint = ctx.get().getSender().getMainHandItem();
                if (blueprint.getItem() instanceof Blueprint) {
                    blueprint.setTag(this.nbt);
                }
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }

}

package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.items.Blueprint;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class BlueprintSync {

    private final CompoundNBT nbt;

    public BlueprintSync(CompoundNBT nbt)
    {
        this.nbt=nbt;
    }

    public BlueprintSync(PacketBuffer buffer)
    {
        this.nbt=buffer.readCompoundTag();
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeCompoundTag(nbt);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()-> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                ItemStack blueprint = ctx.get().getSender().getHeldItemMainhand();
                if (blueprint.getItem() instanceof Blueprint) {
                    blueprint.setTag(this.nbt);
                }
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }

}

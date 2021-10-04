package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.blocks.ChopperBlockEntity;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.codec.TinyBlockData;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ValidTinyBlockCacheSync {

    ResourceLocation itemRegistryName;
    BlockPos chopperPos;

    public ValidTinyBlockCacheSync(@Nullable BlockPos chopperPos, ResourceLocation itemRegistryName)
    {
        this.itemRegistryName=itemRegistryName;
        this.chopperPos = chopperPos;
    }

    public ValidTinyBlockCacheSync(FriendlyByteBuf buffer){
        this.itemRegistryName=buffer.readResourceLocation();
        try {
            this.chopperPos=buffer.readBlockPos();
        }catch (IndexOutOfBoundsException e){
            this.chopperPos=null;
        }
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(this.itemRegistryName);
        if (this.chopperPos!=null)
            buf.writeBlockPos(this.chopperPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        return (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
                ? clientHandle(ctx) : serverHandle(ctx);
    }

    public boolean clientHandle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(()-> {
            ResourceLocation texture = new ResourceLocation(this.itemRegistryName.getNamespace(), "block/" + this.itemRegistryName.getPath());
            TextureAtlasSprite sprite = RenderHelper.getSprite(texture);
            if (sprite!=RenderHelper.getSprite(TextureManager.INTENTIONAL_MISSING_TEXTURE)) {
                //tell server chopper menu at block pos to update
                ModNetworkHandler.sendToServer(new ValidTinyBlockCacheSync(this.chopperPos, this.itemRegistryName));
            }

            ctx.get().setPacketHandled(true);
        });
        return true;
    }

    public boolean serverHandle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            if (!TinyBlockData.validBlockTextureCache.contains(this.itemRegistryName.toString()))
                TinyBlockData.validBlockTextureCache.add(this.itemRegistryName.toString());
            if (this.chopperPos!=null){
                if(ctx.get().getSender().getLevel().getBlockEntity(this.chopperPos) instanceof ChopperBlockEntity chopperBlockEntity)
                    chopperBlockEntity.setChanged();
            }
            ctx.get().setPacketHandled(true);
        });
        return true;
    }

}

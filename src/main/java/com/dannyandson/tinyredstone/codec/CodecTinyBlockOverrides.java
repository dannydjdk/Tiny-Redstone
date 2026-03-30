package com.dannyandson.tinyredstone.codec;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.mojang.serialization.Codec;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

/**
 * 26.1: SimpleJsonResourceReloadListener now takes a type parameter and
 * uses Codec + FileToIdConverter instead of Gson + folder name string.
 * The apply() method receives already-deserialized objects.
 */
public class CodecTinyBlockOverrides extends SimpleJsonResourceReloadListener<TinyBlockData>
{
    /** The raw data that we parsed from json last time resources were reloaded **/
    protected Map<Identifier, TinyBlockData> data = new HashMap<>();

    private String folderName;

    /**
     * Creates a data manager with a codec-based parser.
     * @param folderName The name of the data folder that we will load from
     * @param codec A codec to deserialize the json into TinyBlockData
     */
    public CodecTinyBlockOverrides(String folderName, Codec<TinyBlockData> codec)
    {
        // 26.1: Constructor takes Codec<T> and FileToIdConverter
        super(codec, net.minecraft.resources.FileToIdConverter.json(folderName));
        this.folderName = folderName;
    }

    /**
     * Gets the resource location of the texture identified for the item with the given resource id
     * @param itemResourceId a resource location of the item whose texture we want
     * @return resource location of the block texture
     */
    public TextureAtlasSprite getSprite(Identifier itemResourceId, Side side) {

        for (Map.Entry<Identifier, TinyBlockData> entry : this.data.entrySet()) {
            Identifier texture = entry.getValue().getTexture(itemResourceId, side);
            if (texture != null)
                return RenderHelper.getSprite(texture);
        }

        TextureAtlasSprite missingTextureSprite = RenderHelper.getSprite(TextureManager.INTENTIONAL_MISSING_TEXTURE);
        TextureAtlasSprite sprite = null;
        if (side == Side.FRONT) {
            sprite = RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString() + "_front"));
            if (!sprite.equals(missingTextureSprite))
                return sprite;
            sprite = RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString() + "_side"));
            if (!sprite.equals(missingTextureSprite))
                return sprite;
        } else if (side == Side.TOP) {
            sprite = RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString() + "_top"));
            if (!sprite.equals(missingTextureSprite))
                return sprite;
        } else if (side == Side.BOTTOM) {
            sprite = RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString() + "_bottom"));
            if (!sprite.equals(missingTextureSprite))
                return sprite;
            sprite = RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString() + "_top"));
            if (!sprite.equals(missingTextureSprite))
                return sprite;
        } else if (side == Side.LEFT) {
            sprite = RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString() + "_left"));
            if (!sprite.equals(missingTextureSprite))
                return sprite;
            sprite = RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString() + "_side"));
            if (!sprite.equals(missingTextureSprite))
                return sprite;
        } else if (side == Side.RIGHT) {
            sprite = RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString() + "_right"));
            if (!sprite.equals(missingTextureSprite))
                return sprite;
            sprite = RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString() + "_side"));
            if (!sprite.equals(missingTextureSprite))
                return sprite;
        } else if (side == Side.BACK) {
            sprite = RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString() + "_back"));
            if (!sprite.equals(missingTextureSprite))
                return sprite;
            sprite = RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString() + "_side"));
            if (!sprite.equals(missingTextureSprite))
                return sprite;
        }

        return RenderHelper.getSprite(TinyBlockData.textureResourceLocationFromResourceId(itemResourceId.toString()));
    }

    /**
     * Check if a block has been explicitly disabled via data pack (type: "disabled").
     * @param itemResourceId a resource location of the block to check
     * @return true if the block has been marked as disabled
     */
    public boolean isDisabled(Identifier itemResourceId) {
        for (Map.Entry<Identifier, TinyBlockData> entry : this.data.entrySet()) {
            String status = entry.getValue().getType(itemResourceId);
            if (status != null && status.equals("disabled"))
                return true;
        }
        return false;
    }

    /**
     * 26.1: apply() now receives already-deserialized Map<Identifier, TinyBlockData>
     * instead of raw JsonElement map.
     */
    @Override
    protected void apply(Map<Identifier, TinyBlockData> deserializedData, ResourceManager resourceManager, ProfilerFiller profiler)
    {
        TinyRedstone.LOGGER.info("Beginning loading of data for data loader: {}", this.folderName);
        this.data = new HashMap<>(deserializedData);
        TinyRedstone.LOGGER.info("Data loader for {} loaded {} jsons", this.folderName, this.data.size());
    }

}
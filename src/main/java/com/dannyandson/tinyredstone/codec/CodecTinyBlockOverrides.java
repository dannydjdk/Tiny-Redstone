package com.dannyandson.tinyredstone.codec;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class CodecTinyBlockOverrides extends JsonReloadListener
{
    // default gson if unspecified
    private static final Gson STANDARD_GSON = new Gson();

    /** The codec we use to convert jsonelements to TinyBlockData **/
    private final Codec<TinyBlockData> codec;

    /** The raw data that we parsed from json last time resources were reloaded **/
    protected Map<ResourceLocation, TinyBlockData> data = new HashMap<>();

    private String folderName;

    /**
     * Creates a data manager with a standard gson parser
     * @param folderName The name of the data folder that we will load from, vanilla folderNames are "recipes", "loot_tables", etc</br>
     * Jsons will be read from data/all_modids/folderName/all_jsons</br>
     * folderName can include subfolders, e.g. "some_mod_that_adds_lots_of_data_loaders/cheeses"
     * @param codec A codec to deserialize the json into your TinyBlockData, see javadocs above class
     */
    public CodecTinyBlockOverrides(String folderName, Codec<TinyBlockData> codec)
    {
        this(folderName, codec, STANDARD_GSON);
    }

    /**
     * As above but with a custom GSON
     * @param folderName The name of the data folder that we will load from, vanilla folderNames are "recipes", "loot_tables", etc</br>
     * Jsons will be read from data/all_modids/folderName/all_jsons</br>
     * folderName can include subfolders, e.g. "some_mod_that_adds_lots_of_data_loaders/cheeses"
     * @param codec A codec to deserialize the json into your TinyBlockData, see javadocs above class
     * @param gson A gson for parsing the raw json data into JsonElements. JsonElement-to-TinyBlockData conversion will be done by the codec,
     * so gson type adapters shouldn't be necessary here
     */
    public CodecTinyBlockOverrides(String folderName, Codec<TinyBlockData> codec, Gson gson)
    {
        super(gson, folderName);
        this.folderName=folderName;
        this.codec = codec;
    }

    /**
     * Gets the resource location of the texture identified for the item with the given resource id
     * @param itemResourceId a resource location of the item whose texture we want
     * @return resource location of the block texture
     */
    public TextureAtlasSprite getSprite(ResourceLocation itemResourceId, Side side) {

        for (Map.Entry<ResourceLocation, TinyBlockData> entry : this.data.entrySet()) {
            ResourceLocation texture = entry.getValue().getTexture(itemResourceId, side);
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
     * Does the item with the given resource id have a texture that can be used by a Tiny Block
     * @param itemResourceId a resource location of the item whose texture we want
     * @return true if the item with the given resource id has a texture that can be used by a Tiny Block
     */
    public boolean hasUsableTexture(ResourceLocation itemResourceId){
        for (Map.Entry<ResourceLocation, TinyBlockData> entry : this.data.entrySet()){
            String status = entry.getValue().getType(itemResourceId);
            if (status !=null && status.equals("disabled"))
                return false;
            ResourceLocation texture = entry.getValue().getTexture(itemResourceId);
            if (texture!=null)
                return true;
        }
        return TinyBlockData.validBlockTextureCache.contains(itemResourceId.toString());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, IResourceManager resourceManager, IProfiler profiler)
    {
        TinyRedstone.LOGGER.info("Beginning loading of data for data loader: {}", this.folderName);
        this.data = this.mapValues(jsons);
        TinyBlockData.validBlockTextureCache.clear();
        TinyRedstone.LOGGER.info("Data loader for {} loaded {} jsons", this.folderName, this.data.size());
    }

    private Map<ResourceLocation, TinyBlockData> mapValues(Map<ResourceLocation, JsonElement> inputs)
    {
        Map<ResourceLocation, TinyBlockData> newMap = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : inputs.entrySet())
        {
            ResourceLocation key = entry.getKey();
            JsonElement element = entry.getValue();
            // if we fail to parse json, log an error and continue
            // if we succeeded, add the resulting TinyBlockData to the map
            this.   codec.decode(JsonOps.INSTANCE, element)
                    .get()
                    .ifLeft(result -> newMap.put(key, result.getFirst()))
                    .ifRight(partial -> TinyRedstone.LOGGER.error("Failed to parse data json for {} due to: {}", key.toString(), partial.message()));
        }

        return newMap;
    }

}

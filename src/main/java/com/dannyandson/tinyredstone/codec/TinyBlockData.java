package com.dannyandson.tinyredstone.codec;

import com.dannyandson.tinyredstone.blocks.Side;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TinyBlockData {

    public static List<String> validBlockTextureCache = new ArrayList<>();
    private List<Pair<String, List<Pair<String, String>>>> data;
    private Map<String, Map<String, String>> tinyBlockDefinitions = new HashMap<>();

    public final static Codec<TinyBlockData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.compoundList(Codec.STRING, Codec.compoundList(Codec.STRING, Codec.STRING))
                            .fieldOf("tiny_block_definitions")
                            .forGetter((TinyBlockData o) -> o.data)

            ).apply(instance, TinyBlockData::new)
    );

    public TinyBlockData(List<Pair<String, List<Pair<String, String>>>> definitions) {
        this.data = definitions;
        for (Pair<String, List<Pair<String, String>>> itemData : this.data) {
            Map<String, String> map = new HashMap<>();
            for (Pair<String, String> itemPair : itemData.getSecond()) {
                map.put(itemPair.getFirst(), itemPair.getSecond());
            }
            this.tinyBlockDefinitions.put(itemData.getFirst(), map);
        }
    }

    @CheckForNull
    public ResourceLocation getTexture(ResourceLocation itemResourceId) {
        return getTexture(itemResourceId,Side.FRONT);
    }
    public ResourceLocation getTexture(ResourceLocation itemResourceId, Side side) {
        if (tinyBlockDefinitions.containsKey(itemResourceId.toString())) {
            Map<String, String> itemData = tinyBlockDefinitions.get(itemResourceId.toString());

            if (side == Side.FRONT) {
                if (itemData.containsKey("texture_front"))
                    return textureResourceLocationFromResourceId(itemData.get("texture_front"));
                else if (itemData.containsKey("texture_side"))
                    return textureResourceLocationFromResourceId(itemData.get("texture_side"));
            } else if (side == Side.TOP) {
                if (itemData.containsKey("texture_top"))
                    return textureResourceLocationFromResourceId(itemData.get("texture_top"));
            } else if (side == Side.BOTTOM) {
                if (itemData.containsKey("texture_bottom"))
                    return textureResourceLocationFromResourceId(itemData.get("texture_bottom"));
            } else if (side == Side.LEFT) {
                if (itemData.containsKey("texture_left"))
                    return textureResourceLocationFromResourceId(itemData.get("texture_left"));
                else if (itemData.containsKey("texture_side"))
                    return textureResourceLocationFromResourceId(itemData.get("texture_side"));
            } else if (side == Side.RIGHT) {
                if (itemData.containsKey("texture_right"))
                    return textureResourceLocationFromResourceId(itemData.get("texture_right"));
                else if (itemData.containsKey("texture_side"))
                    return textureResourceLocationFromResourceId(itemData.get("texture_side"));
            } else if (side == Side.BACK) {
                if (itemData.containsKey("texture_back"))
                    return textureResourceLocationFromResourceId(itemData.get("texture_back"));
                else if (itemData.containsKey("texture_side"))
                    return textureResourceLocationFromResourceId(itemData.get("texture_side"));
            }

            if (itemData.containsKey("texture"))
                return textureResourceLocationFromResourceId(itemData.get("texture"));
        }
        return null;
    }

    @CheckForNull
    public String getType(ResourceLocation itemResourceId) {
        if (tinyBlockDefinitions.containsKey(itemResourceId.toString())) {
            Map<String, String> itemData = tinyBlockDefinitions.get(itemResourceId.toString());
            if (itemData.containsKey("type")) {
                return itemData.get("type");
            }
        }
        return null;
    }

    @CheckForNull
    protected static ResourceLocation textureResourceLocationFromResourceId(String resourceId) {
        String[] resourceIdStrings = resourceId.split(":");
        if (resourceIdStrings.length == 1)
            return new ResourceLocation("minecraft", "block/" + resourceIdStrings[0]);
        else if (resourceIdStrings.length == 2)
            return new ResourceLocation(resourceIdStrings[0], "block/" + resourceIdStrings[1]);
        return null;
    }
}

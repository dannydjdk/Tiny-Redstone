package com.dannyandson.tinyredstone.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
        if (tinyBlockDefinitions.containsKey(itemResourceId.toString())) {
            Map<String, String> itemData = tinyBlockDefinitions.get(itemResourceId.toString());
            if (itemData.containsKey("texture")) {
                return textureResourceLocationFromResourceId(itemData.get("texture"));
            }
        }
        return null;
    }

    @CheckForNull
    public String getStatus(ResourceLocation itemResourceId) {
        if (tinyBlockDefinitions.containsKey(itemResourceId.toString())) {
            Map<String, String> itemData = tinyBlockDefinitions.get(itemResourceId.toString());
            if (itemData.containsKey("status")) {
                return itemData.get("status");
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

package com.dannyandson.tinyredstone.codec;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 26.1: SimpleJsonResourceReloadListener now takes a type parameter and
 * uses Codec + FileToIdConverter instead of Gson + folder name string.
 * The apply() method receives already-deserialized objects.
 */
public class CodecTinyBlockOverrides extends SimpleJsonResourceReloadListener<TinyBlockData>
{
    private static final int SIDE_COUNT = Side.values().length;

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
     * Returns all six face sprites for the block in a single model walk,
     * indexed by {@link Side#ordinal()}. Resolution order per side:
     *   1. Data-pack override (TinyBlockData JSON), if any
     *   2. The block's baked model — first quad facing that direction
     *   3. The block's baked model — first direction-agnostic quad
     *   4. The block's particle sprite
     *   5. Intentional missing-texture sprite
     */
    public TextureAtlasSprite[] getSprites(Identifier itemResourceId) {
        TextureAtlasSprite[] result = new TextureAtlasSprite[SIDE_COUNT];

        applyDataOverrides(itemResourceId, result);
        fillFromBlockModel(itemResourceId, result);
        fillMissingWithDefault(result);

        return result;
    }

    /**
     * Returns the sprite for one face. Convenience wrapper around {@link #getSprites};
     * callers needing more than one face should call {@code getSprites} directly to
     * avoid repeated model walks.
     */
    public TextureAtlasSprite getSprite(Identifier itemResourceId, Side side) {
        return getSprites(itemResourceId)[side.ordinal()];
    }

    private void applyDataOverrides(Identifier itemResourceId, TextureAtlasSprite[] result) {
        if (data.isEmpty()) return;
        for (Side side : Side.values()) {
            for (TinyBlockData entry : data.values()) {
                Identifier texture = entry.getTexture(itemResourceId, side);
                if (texture != null) {
                    result[side.ordinal()] = RenderHelper.getSprite(texture);
                    break;
                }
            }
        }
    }

    /**
     * Fills any null entries in {@code result} from the block's baked model.
     * Single model walk: collectParts runs once, then each unfilled side is
     * resolved by querying the parts for that direction.
     */
    private void fillFromBlockModel(Identifier itemResourceId, TextureAtlasSprite[] result) {
        if (allFilled(result)) return;

        Block block = BuiltInRegistries.BLOCK.getValue(itemResourceId);
        if (block == null) return;
        BlockState state = block.defaultBlockState();
        if (state.isAir()) return;

        BlockStateModel model = Minecraft.getInstance().getModelManager()
                .getBlockStateModelSet().get(state);
        if (model == null) return;

        // Variant selection seed: stable per block position. BlockPos.ZERO —
        // tiny blocks always render the default variant of the source block.
        RandomSource random = RandomSource.create(state.getSeed(BlockPos.ZERO));
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);

        TextureAtlasSprite particleSprite = null;

        for (Side side : Side.values()) {
            if (result[side.ordinal()] != null) continue;

            TextureAtlasSprite sprite = findFaceSprite(parts, directionForSide(side));
            if (sprite == null) {
                // Cross-shaped models etc. have direction-agnostic quads (null face).
                sprite = findFaceSprite(parts, null);
            }
            if (sprite == null) {
                if (particleSprite == null) {
                    particleSprite = model.particleMaterial().sprite();
                }
                sprite = particleSprite;
            }
            result[side.ordinal()] = sprite;
        }
    }

    private static @Nullable TextureAtlasSprite findFaceSprite(List<BlockStateModelPart> parts, @Nullable Direction face) {
        for (BlockStateModelPart part : parts) {
            for (BakedQuad quad : part.getQuads(face)) {
                return quad.materialInfo().sprite();
            }
        }
        return null;
    }

    private void fillMissingWithDefault(TextureAtlasSprite[] result) {
        TextureAtlasSprite missing = null;
        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) {
                if (missing == null) {
                    missing = RenderHelper.getSprite(TextureManager.INTENTIONAL_MISSING_TEXTURE);
                }
                result[i] = missing;
            }
        }
    }

    private static boolean allFilled(TextureAtlasSprite[] arr) {
        for (TextureAtlasSprite s : arr) if (s == null) return false;
        return true;
    }

    /**
     * Maps a panel-relative Side to the corresponding Minecraft Direction on
     * the block's natural orientation. Side.FRONT maps to NORTH (Minecraft's
     * default "front" face for directional blocks).
     */
    private static Direction directionForSide(Side side) {
        return switch (side) {
            case TOP    -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
            case FRONT  -> Direction.NORTH;
            case BACK   -> Direction.SOUTH;
            case LEFT   -> Direction.WEST;
            case RIGHT  -> Direction.EAST;
        };
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
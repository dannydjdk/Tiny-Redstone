package com.dannyandson.tinyredstone.codec;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.dannyandson.tinyredstone.blocks.Side;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
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
import java.util.Arrays;
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

    /** Sentinel: no tint (multiplier of 1.0 in every channel). */
    private static final int NO_TINT = 0xFFFFFFFF;

    /** The raw data that we parsed from json last time resources were reloaded **/
    protected Map<Identifier, TinyBlockData> data = new HashMap<>();

    private String folderName;

    public CodecTinyBlockOverrides(String folderName, Codec<TinyBlockData> codec)
    {
        super(codec, net.minecraft.resources.FileToIdConverter.json(folderName));
        this.folderName = folderName;
    }

    /**
     * Bundle of per-side sprites plus per-side biome-tint colors. Both arrays are
     * indexed by {@link Side#ordinal()}. Untinted faces (most blocks) get {@code 0xFFFFFFFF}
     * in the tint slot, which is a no-op when multiplied with a cell color.
     */
    public record SpritesAndTints(TextureAtlasSprite[] sprites, int[] tints) {}

    /**
     * Returns all six face sprites and their associated biome-tint colors in a single
     * model walk. Tiny blocks render the default tint.
     */
    public SpritesAndTints getSpritesAndTints(Identifier itemResourceId) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[SIDE_COUNT];
        int[] tints = new int[SIDE_COUNT];
        Arrays.fill(tints, NO_TINT);

        applyDataOverrides(itemResourceId, sprites);
        fillFromBlockModel(itemResourceId, sprites, tints);
        fillMissingWithDefault(sprites);

        return new SpritesAndTints(sprites, tints);
    }

    /**
     * Returns all six face sprites for the block, indexed by {@link Side#ordinal()}.
     * Convenience wrapper around {@link #getSpritesAndTints} that discards the tint info.
     */
    public TextureAtlasSprite[] getSprites(Identifier itemResourceId) {
        return getSpritesAndTints(itemResourceId).sprites();
    }

    /**
     * Returns the sprite for one face. Convenience wrapper; callers needing more than
     * one face should call {@link #getSpritesAndTints} (or {@link #getSprites}) directly
     * to avoid repeated model walks.
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
     * Walks the block's baked model once and fills any null entries in {@code result}
     * with per-face sprites, and (when {@code tints} is non-null) any tinted faces with
     * the default tint color from {@code BlockColors}.
     */
    private void fillFromBlockModel(Identifier itemResourceId, TextureAtlasSprite[] result, int @Nullable [] tints) {
        if (allFilled(result)) return;

        Block block = BuiltInRegistries.BLOCK.getValue(itemResourceId);
        if (block == null) return;
        BlockState state = block.defaultBlockState();
        if (state.isAir()) return;

        BlockStateModel model = Minecraft.getInstance().getModelManager()
                .getBlockStateModelSet().get(state);
        if (model == null) return;

        // Variant selection seed: stable per block position. BlockPos.ZERO is fine —
        // tiny blocks always render the default variant of the source block.
        RandomSource random = RandomSource.create(state.getSeed(BlockPos.ZERO));
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);

        TextureAtlasSprite particleSprite = null;

        for (Side side : Side.values()) {
            if (result[side.ordinal()] != null) continue;

            BakedQuad quad = findFaceQuad(parts, directionForSide(side));
            if (quad == null) {
                // Cross-shaped models etc. have direction-agnostic quads (null face).
                quad = findFaceQuad(parts, null);
            }

            if (quad != null) {
                result[side.ordinal()] = quad.materialInfo().sprite();
                if (tints != null) {
                    int tintIndex = quad.materialInfo().tintIndex();
                    if (tintIndex >= 0) {
                        // Tiny Blocks use default tints (no biome specific tints)
                        // BlockTintSource.color(state) is the no-world-context path.
                        BlockTintSource source = Minecraft.getInstance().getBlockColors()
                                .getTintSource(state, tintIndex);
                        if (source != null) {
                            int raw = source.color(state);
                            tints[side.ordinal()] = raw;
                        }
                    }
                }
            } else {
                if (particleSprite == null) {
                    particleSprite = model.particleMaterial().sprite();
                }
                result[side.ordinal()] = particleSprite;
                // Particle fallback: no tint info, leave tints[side] at NO_TINT default.
            }
        }
    }

    private static @Nullable BakedQuad findFaceQuad(List<BlockStateModelPart> parts, @Nullable Direction face) {
        for (BlockStateModelPart part : parts) {
            for (BakedQuad quad : part.getQuads(face)) {
                return quad;
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
package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.util.ItemStackHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * 26.1: ItemTintSource replacement for PanelItemColor.
 * Reads the panel color from the item's BlockEntityTag custom data.
 *
 * Registered via RegisterColorHandlersEvent.ItemTintSources in ClientSetup.
 * Referenced in item definition JSONs via "tints" array.
 */
public record PanelItemTintSource() implements ItemTintSource {

    public static final MapCodec<PanelItemTintSource> MAP_CODEC = MapCodec.unit(PanelItemTintSource::new);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        CompoundTag blockEntityTag = ItemStackHelper.getBlockEntityTag(stack);
        if (blockEntityTag != null && blockEntityTag.contains("color")) {
            return ARGB.opaque(blockEntityTag.getIntOr("color", 0));
        }
        return ARGB.opaque(DyeColor.GRAY.getTextColor());
    }

    @Override
    public MapCodec<PanelItemTintSource> type() {
        return MAP_CODEC;
    }
}
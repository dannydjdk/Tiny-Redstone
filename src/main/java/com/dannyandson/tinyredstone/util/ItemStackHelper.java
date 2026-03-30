package com.dannyandson.tinyredstone.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import org.jspecify.annotations.Nullable;

/**
 * Utility class for working with ItemStack custom data in MC 1.21+.
 * In 1.21, ItemStack.getTag()/setTag()/addTagElement() were removed.
 * Instead, custom data is stored via DataComponents.CUSTOM_DATA.
 */
public class ItemStackHelper {

    /**
     * Get the custom NBT data from an ItemStack (equivalent to old stack.getTag())
     */
    @Nullable
    public static CompoundTag getCustomTag(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag();
        }
        return null;
    }

    /**
     * Set the custom NBT data on an ItemStack (equivalent to old stack.setTag())
     */
    public static void setCustomTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Add a tag element to the ItemStack's custom data (equivalent to old stack.addTagElement())
     */
    public static void addTagElement(ItemStack stack, String key, net.minecraft.nbt.Tag value) {
        CompoundTag tag = getCustomTag(stack);
        if (tag == null) {
            tag = new CompoundTag();
        }
        tag.put(key, value);
        setCustomTag(stack, tag);
    }

    /**
     * Get the BlockEntityTag compound from an ItemStack's custom data.
     * 26.1: DataComponents.BLOCK_ENTITY_DATA now uses TypedEntityData instead of CustomData.
     * As a workaround, we store block entity data inside CUSTOM_DATA under a "BlockEntityTag" key.
     */
    @Nullable
    public static CompoundTag getBlockEntityTag(ItemStack stack) {
        CompoundTag customTag = getCustomTag(stack);
        if (customTag != null && customTag.contains("BlockEntityTag")) {
            return customTag.getCompound("BlockEntityTag").orElseGet(CompoundTag::new);
        }
        return null;
    }

    public static void setBlockEntityTag(ItemStack stack, CompoundTag tag) {
        CompoundTag customTag = getCustomTag(stack);
        if (customTag == null) {
            customTag = new CompoundTag();
        }
        customTag.put("BlockEntityTag", tag);
        setCustomTag(stack, customTag);
    }
}
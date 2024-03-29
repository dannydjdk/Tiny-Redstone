package com.dannyandson.tinyredstone.api;

import com.dannyandson.tinyredstone.compat.OverlayBlockInfoMode;
import net.minecraft.world.item.ItemStack;

/**
 * Interface for classes handling overlay mods (such as The One Probe and HWYLA)
 */
public interface IOverlayBlockInfo {
    OverlayBlockInfoMode getMode();
    void setPowerOutput(int power);
    void addText(String text);
    void addText(ItemStack itemStack, String text);
    void addText(String label, String text);
    void addText(ItemStack itemStack, String label, String text);
    void addInfo(String text);
}

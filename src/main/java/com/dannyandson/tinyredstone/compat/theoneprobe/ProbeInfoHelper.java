package com.dannyandson.tinyredstone.compat.theoneprobe;

import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ProbeInfoHelper {
    public static void addPower(IProbeInfo probeInfo, int power) {
        if (power > 0) {
            probeInfo.horizontal()
                    .item(new ItemStack(Items.REDSTONE), probeInfo.defaultItemStyle().width(14).height(14))
                    .text(CompoundText.createLabelInfo("Power: ", power));
        }
    }
}

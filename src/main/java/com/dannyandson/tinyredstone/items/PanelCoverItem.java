package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.util.ItemStackHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class PanelCoverItem extends PanelCellItem {

    public PanelCoverItem(Item.Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
       return InteractionResult.PASS;
    }

    // initializeClient removed — client extensions now registered via
    // RegisterClientExtensionsEvent in ClientSetup

    @Override
    public Component getName(ItemStack stack) {
        if (ItemStackHelper.getCustomTag(stack) != null) {
            String thisName = super.getName(stack).getString();
            String fromBlockName = null;
            CompoundTag itemNBT = ItemStackHelper.getCustomTag(stack);
            CompoundTag madeFromTag = itemNBT.getCompound("made_from").orElseGet(CompoundTag::new);
            if (madeFromTag.contains("namespace")) {
                fromBlockName = (Component.translatable("block." + madeFromTag.getStringOr("namespace", "") + "." + madeFromTag.getStringOr("path", ""))).getString();
            }
            return Component.nullToEmpty(thisName + " (" + fromBlockName + ")");
        }

        return super.getName(stack);
    }
}

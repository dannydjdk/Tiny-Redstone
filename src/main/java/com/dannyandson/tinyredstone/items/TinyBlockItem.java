package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.setup.ModRegistration;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TinyBlockItem extends PanelCellItem {

    public TinyBlockItem(Item.Properties props) {
        super(props);
    }

    @Override
    public Component getName(ItemStack stack) {
        String thisName = super.getName(stack).getString();
        String fromBlockName = null;
        if (ItemStackHelper.getCustomTag(stack) != null) {
            CompoundTag itemNBT = ItemStackHelper.getCustomTag(stack);
            CompoundTag madeFromTag = itemNBT.getCompound("made_from").orElseGet(CompoundTag::new);
            if (madeFromTag.contains("namespace")) {
                fromBlockName = (Component.translatable("block." + madeFromTag.getStringOr("namespace", "") + "." + madeFromTag.getStringOr("path", ""))).getString();
            }
        }
        if (fromBlockName==null){
            if (stack.getItem()== ModRegistration.TINY_SOLID_BLOCK.get()){
                    fromBlockName = Component.translatable("block.minecraft.white_wool").getString();
            } else if (stack.getItem()== ModRegistration.TINY_TRANSPARENT_BLOCK.get()){
                fromBlockName = Component.translatable("block.minecraft.glass").getString();
            }
        }
        return Component.nullToEmpty(thisName + " (" + fromBlockName + ")");
    }

    // initializeClient removed — client extensions now registered via
    // RegisterClientExtensionsEvent in ClientSetup
}

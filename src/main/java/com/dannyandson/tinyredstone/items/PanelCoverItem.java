package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.setup.ModSetup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class PanelCoverItem extends PanelCellItem{

    public PanelCoverItem()   {
        super(new Item.Properties()
                .tab(ModSetup.ITEM_GROUP)
                .setISTER(()->PanelCoverItemRenderer::new)
        );
    }


    @Override
    public ITextComponent getName(ItemStack stack) {
        if (stack.hasTag()) {
            String thisName = super.getName(stack).getString();
            String fromBlockName = null;
            CompoundNBT itemNBT = stack.getTag();
            CompoundNBT madeFromTag = itemNBT.getCompound("made_from");
            if (madeFromTag.contains("namespace")) {
                fromBlockName = (new TranslationTextComponent("block." + madeFromTag.getString("namespace") + "." + madeFromTag.getString("path"))).getString();
            }
            return ITextComponent.nullToEmpty(thisName + " (" + fromBlockName + ")");
        }

        return super.getName(stack);
    }


}

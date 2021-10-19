package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.setup.ModSetup;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TinyBlockItem extends PanelCellItem {

    public TinyBlockItem()
    {
        super(new Item.Properties()
                .tab(ModSetup.ITEM_GROUP)
                .setISTER(()->TinyBlockItemRenderer::new)
        );
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        String thisName = super.getName(stack).getString();
        String fromBlockName = null;
        if (stack.hasTag()) {
            CompoundNBT itemNBT = stack.getTag();
            CompoundNBT madeFromTag = itemNBT.getCompound("made_from");
            if (madeFromTag.contains("namespace")) {
                fromBlockName = (new TranslationTextComponent("block." + madeFromTag.getString("namespace") + "." + madeFromTag.getString("path"))).getString();
            }
        }
        if (fromBlockName==null){
            if (stack.getItem()== Registration.TINY_SOLID_BLOCK.get()){
                    fromBlockName = new TranslationTextComponent("block.minecraft.white_wool").getString();
            } else if (stack.getItem()== Registration.TINY_TRANSPARENT_BLOCK.get()){
                fromBlockName = new TranslationTextComponent("block.minecraft.glass").getString();
            }
        }
        return ITextComponent.nullToEmpty(thisName + " (" + fromBlockName + ")");
    }

}

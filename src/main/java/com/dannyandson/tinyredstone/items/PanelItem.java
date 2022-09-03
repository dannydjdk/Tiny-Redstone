package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.setup.ModSetup;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class PanelItem extends BlockItem {



    public PanelItem()
    {
        super(Registration.REDSTONE_PANEL_BLOCK.get(),new Item.Properties()
                .tab(ModSetup.ITEM_GROUP)
                .setISTER(()->PanelItemRenderer::new)
        );

    }

    @Override
    public  void  appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flags)
    {
        list.add(new TranslationTextComponent("message.item.redstone_panel"));
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        if (stack.getTag()!=null && stack.getTag().contains("BlockEntityTag") ) {
            CompoundNBT itemTag = stack.getTag().getCompound("BlockEntityTag");
            if(itemTag.contains("hasBase") && !itemTag.getBoolean("hasBase"))
                return new TranslationTextComponent("block.tinyredstone.tiny_container");
        }

        return super.getName(stack);
    }

}

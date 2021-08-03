package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.setup.ModSetup;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class PanelItem extends BlockItem {



    public PanelItem()
    {
        super(Registration.REDSTONE_PANEL_BLOCK.get(),new Item.Properties()
                .tab(ModSetup.ITEM_GROUP)
                //TODO item rendering in 1.17
                //.setISTER(()->PanelItemRenderer::new)
        );

    }

    @Override
    public  void  appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flags)
    {
        list.add(new TranslatableComponent("message.item.redstone_panel"));
    }
}

package com.dannyandson.tinyredstone.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class TinyColorSelector extends RedstoneWrench {

    @Override
    public  void  addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flags)
    {
        list.add(new TranslationTextComponent("message.item.tiny_color_selector"));
    }
}

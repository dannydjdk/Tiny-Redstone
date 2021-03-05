package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.setup.ModSetup;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class PanelCellItem extends Item {
    public PanelCellItem() {
        super(new Item.Properties().group(ModSetup.ITEM_GROUP));
    }

    @Override
    public  void  addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flags)
    {
        list.add(new TranslationTextComponent("message.item.redstone_panel_cell"));
    }

    public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        if(state.getBlock() instanceof PanelBlock)
            return false;
        return true;
    }
}

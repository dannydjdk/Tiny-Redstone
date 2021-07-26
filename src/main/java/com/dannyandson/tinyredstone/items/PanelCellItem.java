package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.setup.ModSetup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class PanelCellItem extends Item {
    public PanelCellItem() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
    }

    @Override
    public  void  appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flags)
    {
        list.add(new TranslatableComponent("message.item.redstone_panel_cell"));
    }

    public boolean canPlayerBreakBlockWhileHolding(BlockState state, Level worldIn, BlockPos pos, Player player) {
        if(state.getBlock() instanceof PanelBlock)
            return false;
        return true;
    }
}

package com.dannyandson.tinyredstone.api;

import com.dannyandson.tinyredstone.blocks.PanelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractPanelCellItem extends Item {
    public AbstractPanelCellItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {

        if(player.level.getBlockState(pos).getBlock() instanceof PanelBlock)
            return true;
        return false;
    }

}

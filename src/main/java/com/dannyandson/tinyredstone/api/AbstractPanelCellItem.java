package com.dannyandson.tinyredstone.api;

import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Helpful abstract class for panel cell items to inherit.
 * Prevents redstone panels from being broken when hit with a panel cell item.
 * Also referenced by left click event to prevent creative players from breaking panels.
 */
public abstract class AbstractPanelCellItem extends Item {
    public AbstractPanelCellItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemStackCopy = context.getItemInHand().copy();
        InteractionResult result = Registration.REDSTONE_PANEL_ITEM.get().useOn(context);
        context.getPlayer().setItemInHand(context.getHand(),itemStackCopy);
        if(context.getLevel().getBlockEntity(context.getClickedPos().offset(context.getClickedFace().getNormal())) instanceof PanelTile panelTile && context.getPlayer()!=null) {
            Registration.REDSTONE_PANEL_BLOCK.get().use(panelTile.getBlockState(),context.getLevel(),panelTile.getBlockPos(), context.getPlayer(),context.getHand(),panelTile.getPlayerCollisionHitResult(context.getPlayer()));
        }
        return result;
    }

    /**
     * Called before a block is broken. Return true to prevent default block
     * harvesting.
     * We return true if the block is being hit is a redstone panel to prevent
     * harvesting the block when attempting to remove components.
     *
     * Note: In SMP, this is called on both client and server sides!
     *
     * @param itemstack The current ItemStack
     * @param pos       Block's position in world
     * @param player    The Player that is wielding the item
     * @return True to prevent harvesting, false to continue as normal
     */
    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        return player.level.getBlockState(pos).getBlock() instanceof PanelBlock;
    }

}

package com.dannyandson.tinyredstone.api;

import com.dannyandson.tinyredstone.Config;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Helpful abstract class for panel cell items to inherit.
 */
public abstract class AbstractPanelCellItem extends Item {
    public AbstractPanelCellItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (Config.ALLOW_WORLD_PLACEMENT.get()) {
            ItemStack itemStackCopy = context.getItemInHand().copy();
            InteractionResult result = ModRegistration.REDSTONE_PANEL_ITEM.get().useOn(context);
            context.getPlayer().setItemInHand(context.getHand(), itemStackCopy);
            if (context.getLevel().getBlockEntity(context.getClickedPos().relative(context.getClickedFace())) instanceof PanelTile panelTile && context.getPlayer() != null) {
                // Use useItemOn (item-in-hand interaction) not useWithoutItem (empty-hand)
                BlockHitResult hitResult = panelTile.getPlayerCollisionHitResult(context.getPlayer());
                ModRegistration.REDSTONE_PANEL_BLOCK.get().useItemOn(
                        context.getItemInHand(),
                        panelTile.getBlockState(),
                        context.getLevel(),
                        panelTile.getBlockPos(),
                        context.getPlayer(),
                        context.getHand(),
                        hitResult
                );
            }
            return result;
        }
        return super.useOn(context);
    }
    /**
     * Called before a block is broken. Return true to prevent default block harvesting.
     */
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        return player.level().getBlockState(pos).getBlock() instanceof PanelBlock;
    }
}
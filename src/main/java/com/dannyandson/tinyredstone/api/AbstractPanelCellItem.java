package com.dannyandson.tinyredstone.api;

import com.dannyandson.tinyredstone.Config;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;

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
    public ActionResultType useOn(ItemUseContext context) {
        if (Config.ALLOW_WORLD_PLACEMENT.get()) {
            ItemStack itemStackCopy = context.getItemInHand().copy();
            ActionResultType result = Registration.REDSTONE_PANEL_ITEM.get().useOn(context);
            context.getPlayer().setItemInHand(context.getHand(), itemStackCopy);

            TileEntity te = context.getLevel().getBlockEntity(context.getClickedPos().offset(context.getClickedFace().getNormal()));
            if (te instanceof PanelTile && context.getPlayer() != null) {
                PanelTile panelTile = (PanelTile) te;
                Registration.REDSTONE_PANEL_BLOCK.get().use(panelTile.getBlockState(), context.getLevel(), panelTile.getBlockPos(), context.getPlayer(), context.getHand(), panelTile.getPlayerCollisionHitResult(context.getPlayer()));
            }
            return result;
        }
        return super.useOn(context);
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
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, PlayerEntity player) {
        return player.level.getBlockState(pos).getBlock() instanceof PanelBlock;
    }

}

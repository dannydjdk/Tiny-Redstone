package com.dannyandson.tinyredstone;

import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.items.PanelCellItem;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.block.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonBinding {
    @SubscribeEvent
    public void onPlayerLogoff(PlayerEvent.PlayerLoggedOutEvent event) {
        RotationLock.removeLock();
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        //allow creative players to remove cells by left clicking with wrench or cell item
        if (
                event.getPlayer().isCreative()
                        && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof PanelBlock
                        && (
                        event.getPlayer().getHeldItemMainhand().getItem()== Registration.REDSTONE_WRENCH.get()
                                || event.getPlayer().getHeldItemMainhand().getItem() instanceof PanelCellItem
                )
        ) {
            BlockState blockState = event.getWorld().getBlockState(event.getPos());
            PanelBlock panelBlock = (PanelBlock)blockState.getBlock();
            panelBlock.onBlockClicked(blockState,event.getWorld(),event.getPos(), event.getPlayer());
            event.setCanceled(true);
        }
    }
}

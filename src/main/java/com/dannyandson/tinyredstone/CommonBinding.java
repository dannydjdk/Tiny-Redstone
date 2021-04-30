package com.dannyandson.tinyredstone;

import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.items.PanelCellItem;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TinyRedstone.MODID)
public class CommonBinding {
    @SubscribeEvent
    public static void onPlayerLogoff(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerEntity player = event.getPlayer();
        if(player.world.isRemote) {
            RotationLock.removeLock(false);
        } else {
            RotationLock.removeServerLock(player);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        //allow creative players to remove cells by left clicking with wrench or cell item
        if (event.getPlayer().isCreative() &&
                event.getWorld().getBlockState(event.getPos()).getBlock() instanceof PanelBlock &&
                (
                        event.getPlayer().getHeldItemMainhand().getItem()==Registration.REDSTONE_WRENCH.get() ||
                                event.getPlayer().getHeldItemMainhand().getItem() instanceof PanelCellItem
                )) {
            BlockState blockState = event.getWorld().getBlockState(event.getPos());
            PanelBlock panelBlock = (PanelBlock)blockState.getBlock();
            panelBlock.onBlockClicked(blockState,event.getWorld(),event.getPos(), event.getPlayer());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getPlayer().isSneaking() && PanelBlock.isPanelCellItem(event.getItemStack().getItem()))
        {
            TileEntity te = event.getWorld().getTileEntity(event.getPos());
            if (te instanceof PanelTile)
            {
                Registration.REDSTONE_PANEL_BLOCK.get().onBlockActivated(te.getBlockState(),event.getWorld(),event.getPos(),event.getPlayer(),event.getHand(),event.getHitVec());
                event.setCanceled(true);
            }
        }
    }
}

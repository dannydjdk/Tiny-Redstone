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
        if(player.level.isClientSide) {
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
                event.getPlayer().level.getBlockState(event.getPos()).getBlock() instanceof PanelBlock &&
                (
                        event.getPlayer().getMainHandItem().getItem()==Registration.REDSTONE_WRENCH.get() ||
                                event.getPlayer().getMainHandItem().getItem() instanceof PanelCellItem
                )) {
            BlockState blockState = event.getPlayer().level.getBlockState(event.getPos());
            PanelBlock panelBlock = (PanelBlock)blockState.getBlock();
            panelBlock.attack(blockState,event.getPlayer().level,event.getPos(), event.getPlayer());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getPlayer().isCrouching() && PanelBlock.isPanelCellItem(event.getItemStack().getItem()))
        {
            TileEntity te = event.getPlayer().level.getBlockEntity(event.getPos());
            if (te instanceof PanelTile)
            {
                Registration.REDSTONE_PANEL_BLOCK.get().use(te.getBlockState(),event.getPlayer().level,event.getPos(),event.getPlayer(),event.getHand(),event.getHitVec());
                event.setCanceled(true);
            }
        }
    }
}

package com.dannyandson.tinyredstone;

import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.items.PanelCellItem;
import com.dannyandson.tinyredstone.items.PanelItem;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ClientBinding {
    boolean key_alt;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        int action = event.getAction();
        if(action == GLFW.GLFW_PRESS) {
            int key = event.getKey();
            if(key == GLFW.GLFW_KEY_LEFT_ALT) key_alt = true;
        } else if(action == GLFW.GLFW_RELEASE) {
            int key = event.getKey();
            if(key == GLFW.GLFW_KEY_LEFT_ALT) key_alt = false;
        }
    }
    @SubscribeEvent
    public void wheelEvent(final InputEvent.MouseScrollEvent mouseScrollEvent) {
        if (mouseScrollEvent.isCanceled()) return;
        final double wheelDelta = mouseScrollEvent.getScrollDelta();
        if (wheelDelta == 0) return;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        final ItemStack mainHand = player.getHeldItemMainhand();
        final Item mainHandItem = mainHand.getItem();
        if (key_alt && mainHandItem instanceof PanelItem) {
            ((PanelItem) mainHandItem).altScroll(player, mainHand, wheelDelta);
            mouseScrollEvent.setCanceled(true);
        }
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

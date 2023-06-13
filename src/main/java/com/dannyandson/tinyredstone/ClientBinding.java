package com.dannyandson.tinyredstone;

import com.dannyandson.tinyredstone.api.AbstractPanelCellItem;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.RotationLock;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;

@Mod.EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT)
public class ClientBinding {

    public static KeyMapping rotationLock;

    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        rotationLock =  new KeyMapping("key." + TinyRedstone.MODID + ".rotation_lock", GLFW.GLFW_KEY_LEFT_ALT, "tinyredstone");
        event.register(rotationLock);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key keyInputEvent) {
        if (keyInputEvent.isCanceled()) return;
        int numberKey = keyInputEvent.getKey() - GLFW.GLFW_KEY_0;
        if(numberKey > 0 && numberKey <= 9) {
            final Player player = Minecraft.getInstance().player;
            if (player == null) return;
            if(player.getInventory().selected + 1 == numberKey) return;
            final ItemStack mainHand = player.getMainHandItem();
            final Item mainHandItem = mainHand.getItem();

            if (mainHandItem instanceof AbstractPanelCellItem) {
                RotationLock.removeLock();
            }
        }
    }

    @SubscribeEvent
    public static void wheelEvent(final InputEvent.MouseScrollingEvent mouseScrollEvent) {
        if (mouseScrollEvent.isCanceled()) return;
        final double scrollDelta = mouseScrollEvent.getScrollDelta();
        if (scrollDelta == 0) return;
        final Player player = Minecraft.getInstance().player;
        if (player == null) return;
        Level world = Minecraft.getInstance().level;
        final ItemStack mainHand = player.getMainHandItem();
        final Item mainHandItem = mainHand.getItem();

        if (mainHandItem instanceof AbstractPanelCellItem) {
            if(rotationLock.isDown()) {
                Vec3 lookVector = Minecraft.getInstance().hitResult.getLocation();
                BlockPos blockPos = BlockPos.containing(lookVector.x,lookVector.y,lookVector.z);
                BlockEntity te = world.getBlockEntity(blockPos);
                if (te instanceof PanelTile) {
                    try {
                        IPanelCell panelCell = (IPanelCell) PanelBlock.getPanelCellClassFromItem(mainHandItem).getConstructors()[0].newInstance();
                        RotationLock.lockRotation((PanelTile) te, player, panelCell.canPlaceVertical(), scrollDelta < 0);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        TinyRedstone.LOGGER.error("Exception thrown when attempting to lock rotation: " + e.getMessage());
                    }
                } else {
                    try {
                        IPanelCell panelCell = (IPanelCell) PanelBlock.getPanelCellClassFromItem(mainHandItem).getConstructors()[0].newInstance();
                        RotationLock.lockRotation(panelCell.canPlaceVertical(), scrollDelta < 0);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        TinyRedstone.LOGGER.error("Exception thrown when attempting to lock rotation: " + e.getMessage());
                    }
                }
                mouseScrollEvent.setCanceled(true);
            } else {
                RotationLock.removeLock();
            }
        }
    }
}

package com.dannyandson.tinyredstone;

import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.RotationLock;
import com.dannyandson.tinyredstone.items.PanelCellItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;

@Mod.EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT)
public class ClientBinding {

    public static KeyBinding rotationLock;

    public static void registerKeyBindings() {
        rotationLock =  new KeyBinding("key." + TinyRedstone.MODID + ".rotation_lock", GLFW.GLFW_KEY_LEFT_ALT, "TinyRedstone");
        ClientRegistry.registerKeyBinding(rotationLock);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent keyInputEvent) {
        if (keyInputEvent.isCanceled()) return;
        int numberKey = keyInputEvent.getKey() - GLFW.GLFW_KEY_0;
        if(numberKey > 0 && numberKey <= 9) {
            final PlayerEntity player = Minecraft.getInstance().player;
            if (player == null) return;
            if(player.inventory.selected + 1 == numberKey) return;
            final ItemStack mainHand = player.getMainHandItem();
            final Item mainHandItem = mainHand.getItem();

            if (mainHandItem instanceof PanelCellItem) {
                RotationLock.removeLock();
            }
        }
    }

    @SubscribeEvent
    public static void wheelEvent(final InputEvent.MouseScrollEvent mouseScrollEvent) {
        if (mouseScrollEvent.isCanceled()) return;
        final double scrollDelta = mouseScrollEvent.getScrollDelta();
        if (scrollDelta == 0) return;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        World world = Minecraft.getInstance().level;
        final ItemStack mainHand = player.getMainHandItem();
        final Item mainHandItem = mainHand.getItem();

        if (mainHandItem instanceof PanelCellItem) {
            if(rotationLock.isDown()) {
                Vector3d lookVector = Minecraft.getInstance().hitResult.getLocation();
                BlockPos blockPos = new BlockPos(lookVector);
                TileEntity te = world.getBlockEntity(blockPos);
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

package com.dannyandson.tinyredstone;

import com.dannyandson.tinyredstone.blocks.IPanelCell;
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
    public static void wheelEvent(final InputEvent.MouseScrollEvent mouseScrollEvent) {
        if (mouseScrollEvent.isCanceled()) return;
        if (mouseScrollEvent.getScrollDelta() == 0) return;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        World world = Minecraft.getInstance().world;
        final ItemStack mainHand = player.getHeldItemMainhand();
        final Item mainHandItem = mainHand.getItem();

        if (mainHandItem instanceof PanelCellItem) {
            if(rotationLock.isKeyDown()) {
                final int wheelDelta = (int) mouseScrollEvent.getScrollDelta();
                Vector3d lookVector = Minecraft.getInstance().objectMouseOver.getHitVec();
                BlockPos blockPos = new BlockPos(lookVector);
                TileEntity te = world.getTileEntity(blockPos);
                if (te instanceof PanelTile) {
                    try {
                        IPanelCell panelCell = (IPanelCell) PanelBlock.getPanelCellClassFromItem(mainHandItem).getConstructors()[0].newInstance();
                        RotationLock.lockRotation((PanelTile) te, player, panelCell.canPlaceVertical(), wheelDelta < 0);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        TinyRedstone.LOGGER.error("Exception thrown when attempting to lock rotation: " + e.getMessage());
                    }
                } else {
                    try {
                        IPanelCell panelCell = (IPanelCell) PanelBlock.getPanelCellClassFromItem(mainHandItem).getConstructors()[0].newInstance();
                        RotationLock.lockRotation(panelCell.canPlaceVertical(), wheelDelta < 0);
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

package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.RotationLockRemoveSync;
import com.dannyandson.tinyredstone.network.RotationLockSync;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.UUID;

public class RotationLock {
    private static final HashMap<UUID, Side> playerRotationLock = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    private static Side rotationLock;

    @OnlyIn(Dist.CLIENT)
    public static void removeLock() {
        removeLock(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void removeLock(boolean sendToServer) {
        if (rotationLock != null) {
            rotationLock = null;
            if(sendToServer) ModNetworkHandler.sendToServer(new RotationLockRemoveSync());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(Side side, boolean allowVertical, boolean invert) {
        lockRotation(side, allowVertical, invert, true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(Side side, boolean allowVertical, boolean invert, boolean sendToServer) {
        switch (side) {
            case BACK:
                side = invert ? Side.RIGHT : (allowVertical ? Side.BOTTOM : Side.LEFT);
                break;
            case LEFT:
                side = invert ? (allowVertical ? Side.TOP : Side.BACK) : Side.FRONT;
                break;
            case FRONT:
                side = invert ? Side.LEFT : Side.RIGHT;
                break;
            case RIGHT:
                side = invert ? Side.FRONT : Side.BACK;
                break;
            case BOTTOM:
                side = invert ? Side.BACK : Side.TOP;
                break;
            case TOP:
                side = invert ? Side.BOTTOM : Side.LEFT;
                break;
        }
        if(side != rotationLock) {
            rotationLock = side;
            if(sendToServer) ModNetworkHandler.sendToServer(new RotationLockSync(side));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(boolean allowVertical, boolean invert) {
        lockRotation(allowVertical, invert, true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(PanelTile panelTile, PlayerEntity playerEntity, boolean allowVertical, boolean invert) {
        lockRotation(panelTile, playerEntity, allowVertical, invert, true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(boolean allowVertical, boolean invert, boolean sendToServer) {
        lockRotation(rotationLock == null ? Side.FRONT : rotationLock, allowVertical, invert, sendToServer);
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(PanelTile panelTile, PlayerEntity playerEntity, boolean allowVertical, boolean invert, boolean sendToServer) {
        lockRotation(rotationLock == null ? panelTile.getSideFromDirection(panelTile.getPlayerDirectionFacing(playerEntity, allowVertical)) : rotationLock, allowVertical, invert, sendToServer);
    }

    @OnlyIn(Dist.CLIENT)
    public static Side getRotationLock() {
        return rotationLock;
    }

    public static void removeServerLock(Player player) {
        playerRotationLock.remove(player.getUUID());
    }

    public static void lockServerRotation(PlayerEntity playerEntity, Side side) {
        playerRotationLock.put(playerEntity.getUUID(), side);
    }

    public static Side getServerRotationLock(Player playerEntity) {
        return playerRotationLock.get(playerEntity.getUUID());
    }
}
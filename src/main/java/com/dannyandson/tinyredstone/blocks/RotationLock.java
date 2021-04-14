package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.RotationLockRemoveSync;
import com.dannyandson.tinyredstone.network.RotationLockSync;
import net.minecraft.entity.player.PlayerEntity;
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
        if(rotationLock != null) {
            rotationLock = null;
            if(sendToServer) ModNetworkHandler.sendToServer(new RotationLockRemoveSync());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(Side side, boolean invert) {
        lockRotation(side, invert, true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(Side side, boolean invert, boolean sendToServer) {
        switch (side) {
            case BACK:
                side = invert ? Side.RIGHT : Side.LEFT;
                break;
            case LEFT:
                side = invert ? Side.BACK : Side.FRONT;
                break;
            case FRONT:
                side = invert ? Side.LEFT : Side.RIGHT;
                break;
            case RIGHT:
                side = invert ? Side.FRONT : Side.BACK;
                break;
        }
        if(side != rotationLock) {
            rotationLock = side;
            if(sendToServer) ModNetworkHandler.sendToServer(new RotationLockSync(side));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(boolean invert) {
        lockRotation(invert, true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(PanelTile panelTile, PlayerEntity playerEntity, boolean invert) {
        lockRotation(panelTile, playerEntity, invert, true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(boolean invert, boolean sendToServer) {
        lockRotation(rotationLock == null
                ? Side.FRONT
                : rotationLock, invert, sendToServer);
    }

    @OnlyIn(Dist.CLIENT)
    public static void lockRotation(PanelTile panelTile, PlayerEntity playerEntity, boolean invert, boolean sendToServer) {
        lockRotation(rotationLock == null
                ? panelTile.getSideFromDirection(panelTile.getPlayerDirectionFacing(playerEntity))
                : rotationLock, invert, sendToServer);
    }

    @OnlyIn(Dist.CLIENT)
    public static Side getRotationLock() {
        return rotationLock;
    }

    public static void removeServerLock(PlayerEntity player) {
        playerRotationLock.remove(player.getUniqueID());
    }

    public static void lockServerRotation(PlayerEntity playerEntity, Side side) {
        playerRotationLock.put(playerEntity.getUniqueID(), side);
    }

    public static Side getServerRotationLock(PlayerEntity playerEntity) {
        return playerRotationLock.get(playerEntity.getUniqueID());
    }
}
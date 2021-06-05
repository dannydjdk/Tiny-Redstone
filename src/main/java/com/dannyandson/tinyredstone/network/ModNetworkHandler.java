package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ModNetworkHandler {
    private static SimpleChannel INSTANCE;
    private static int ID = 0;
    private static final String PROTOCOL_VERSION = "1.2";

    private static int nextID() {
        return ID++;
    }

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(TinyRedstone.MODID, "tinyredstone"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals);

        INSTANCE.messageBuilder(RepeaterTickSync.class,nextID())
                .encoder(RepeaterTickSync::toBytes)
                .decoder(RepeaterTickSync::new)
                .consumer(RepeaterTickSync::handle)
                .add();

        INSTANCE.messageBuilder(TinyBlockColorSync.class,nextID())
                .encoder(TinyBlockColorSync::toBytes)
                .decoder(TinyBlockColorSync::new)
                .consumer(TinyBlockColorSync::handle)
                .add();

        INSTANCE.messageBuilder(NoteBlockInstrumentSync.class,nextID())
                .encoder(NoteBlockInstrumentSync::toBytes)
                .decoder(NoteBlockInstrumentSync::new)
                .consumer(NoteBlockInstrumentSync::handle)
                .add();

        INSTANCE.messageBuilder(BlueprintSync.class,nextID())
                .encoder(BlueprintSync::toBytes)
                .decoder(BlueprintSync::new)
                .consumer(BlueprintSync::handle)
                .add();

        INSTANCE.messageBuilder(RotationLockSync.class,nextID())
                .encoder(RotationLockSync::toBytes)
                .decoder(RotationLockSync::new)
                .consumer(RotationLockSync::handle)
                .add();

        INSTANCE.messageBuilder(RotationLockRemoveSync.class,nextID())
                .encoder(RotationLockRemoveSync::toBytes)
                .decoder(RotationLockRemoveSync::new)
                .consumer(RotationLockRemoveSync::handle)
                .add();

        INSTANCE.messageBuilder(CrashFlagResetSync.class,nextID())
                .encoder(CrashFlagResetSync::toBytes)
                .decoder(CrashFlagResetSync::new)
                .consumer(CrashFlagResetSync::handle)
                .add();

        INSTANCE.messageBuilder(ClearPanelSync.class,nextID())
                .encoder(ClearPanelSync::toBytes)
                .decoder(ClearPanelSync::new)
                .consumer(ClearPanelSync::handle)
                .add();

        INSTANCE.messageBuilder(PlaySound.class,nextID())
                .encoder(PlaySound::toBytes)
                .decoder(PlaySound::new)
                .consumer(PlaySound::handle)
                .add();
    }

    public static void sendToClient(Object packet, ServerPlayerEntity player) {
        INSTANCE.sendTo(packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

}
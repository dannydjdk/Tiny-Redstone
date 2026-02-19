package com.dannyandson.tinyredstone.network;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = TinyRedstone.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworkHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(TinyRedstone.MODID).versioned("2.0");

        // Client → Server packets
        registrar.playToServer(
                RepeaterTickSync.TYPE, RepeaterTickSync.STREAM_CODEC, RepeaterTickSync::handle
        );
        registrar.playToServer(
                TinyBlockColorSync.TYPE, TinyBlockColorSync.STREAM_CODEC, TinyBlockColorSync::handle
        );
        registrar.playToServer(
                NoteBlockInstrumentSync.TYPE, NoteBlockInstrumentSync.STREAM_CODEC, NoteBlockInstrumentSync::handle
        );
        registrar.playToServer(
                BlueprintSync.TYPE, BlueprintSync.STREAM_CODEC, BlueprintSync::handle
        );
        registrar.playToServer(
                RotationLockSync.TYPE, RotationLockSync.STREAM_CODEC, RotationLockSync::handle
        );
        registrar.playToServer(
                RotationLockRemoveSync.TYPE, RotationLockRemoveSync.STREAM_CODEC, RotationLockRemoveSync::handle
        );
        registrar.playToServer(
                CrashFlagResetSync.TYPE, CrashFlagResetSync.STREAM_CODEC, CrashFlagResetSync::handle
        );
        registrar.playToServer(
                ClearPanelSync.TYPE, ClearPanelSync.STREAM_CODEC, ClearPanelSync::handle
        );
        registrar.playToServer(
                PushChopperOutputType.TYPE, PushChopperOutputType.STREAM_CODEC, PushChopperOutputType::handle
        );

        // Server → Client packets
        registrar.playToClient(
                PlaySound.TYPE, PlaySound.STREAM_CODEC, PlaySound::handle
        );
        registrar.playToClient(
                PanelCellSync.TYPE, PanelCellSync.STREAM_CODEC, PanelCellSync::handle
        );

        // Bidirectional packets
        registrar.playToClient(
                ValidTinyBlockCacheSync.TYPE, ValidTinyBlockCacheSync.STREAM_CODEC, ValidTinyBlockCacheSync::handleClient
        );
        // ValidTinyBlockCacheConfirm: client → server (response confirming texture validity)
        registrar.playToServer(
                ValidTinyBlockCacheConfirm.TYPE, ValidTinyBlockCacheConfirm.STREAM_CODEC, ValidTinyBlockCacheConfirm::handle
        );
    }

    public static void sendToClient(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToNearestClient(CustomPacketPayload packet, Level level, BlockPos pos) {
        Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), -1d, false);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, packet);
        }
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

}

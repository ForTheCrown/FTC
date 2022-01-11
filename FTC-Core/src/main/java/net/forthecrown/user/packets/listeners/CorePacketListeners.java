package net.forthecrown.user.packets.listeners;

import net.forthecrown.user.packets.PacketListeners;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;

public final class CorePacketListeners {
    private CorePacketListeners() {}

    public static void init() {
        PacketListeners.register(
                ClientboundPlayerInfoPacket.class,
                new GameModePacketListener()
        );

        /*PacketListeners.register(
                ClientboundBlockEntityDataPacket.class,
                new SignRenderPacketListener()
        );

        PacketListeners.register(
                ClientboundLevelChunkWithLightPacket.class,
                new SignChunkLoadListener()
        );*/
    }
}

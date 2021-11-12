package net.forthecrown.user.packets.listeners;

import net.forthecrown.user.packets.PacketListeners;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;

public final class CorePacketListeners {
    private CorePacketListeners() {}

    public static void init() {
        PacketListeners.register(
                ClientboundPlayerInfoPacket.class,
                new GameModePacketListener()
        );

        PacketListeners.register(
                ClientboundBlockEntityDataPacket.class,
                new SignRenderPacketListener()
        );

        PacketListeners.register(
                ClientboundLevelChunkPacket.class,
                new SignChunkLoadListener()
        );
    }
}

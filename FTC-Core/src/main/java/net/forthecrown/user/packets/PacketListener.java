package net.forthecrown.user.packets;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public interface PacketListener<T extends Packet<ClientGamePacketListener>> {
    void onPacketSend(PacketContext<T> context);

}

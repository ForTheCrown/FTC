package net.forthecrown.user.packets;

import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.Struct;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class PacketContext<T extends Packet<ClientGamePacketListener>> implements Struct {
    public final T packet;
    public final CrownUser target;
    public boolean cancelled;

    public PacketContext(T packet, CrownUser target) {
        this.packet = packet;
        this.target = target;
    }
}

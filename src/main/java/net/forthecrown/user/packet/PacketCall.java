package net.forthecrown.user.packet;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.entity.Player;

/**
 * A packet's read write context
 */
@Getter
public class PacketCall {
    /**
     * The cancellation state of the packet.
     * <p>
     * If the packet ends up being cancelled, it will
     * not be read or written to the network
     */
    @Setter
    private boolean cancelled = false;

    /**
     * The player that's sending/receiving the packet
     */
    private final Player player;

    /**
     * The player's user instance
     */
    private final User user;

    /**
     * Gets the packet listener connection the
     * server-side player uses to recieve packets.
     */
    @Getter
    private final ServerGamePacketListenerImpl packetListener;

    PacketCall(Player player) {
        this.player = player;
        this.user = Users.get(player);

        this.packetListener = VanillaAccess.getPacketListener(player);
    }
}
package net.forthecrown.vanilla.packet;

import java.util.concurrent.Executor;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.packet.PacketCall;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * A packet's read write context
 */
@Getter
public class PacketCallImpl implements PacketCall {

  /**
   * The cancellation state of the packet.
   * <p>
   * If the packet ends up being cancelled, it will not be read or written to the network
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
   * Gets the packet listener connection the server-side player uses to recieve packets.
   */
  private final ServerGamePacketListenerImpl packetListener;

  /**
   * Main thread executor to use for delegating tasks to main thread, as packets are handled over
   * async IO threads
   */
  private final Executor executor;

  /**
   * Packet that will be sent instead of the original packet
   */
  @Setter
  private Packet<?> replacementPacket;

  PacketCallImpl(Player player) {
    this.player = player;
    this.user = Users.get(player);

    this.packetListener = VanillaAccess.getPacketListener(player);
    this.executor = VanillaAccess.getServer();
  }

  @Override
  public World getWorld() {
    return player.getWorld();
  }
}
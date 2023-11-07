package net.forthecrown.packet;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import net.forthecrown.user.User;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface PacketCall {

  World getWorld();

  boolean isCancelled();

  Player getPlayer();

  User getUser();

  ServerGamePacketListenerImpl getPacketListener();

  Executor getExecutor();

  Packet<?> getReplacementPacket();

  void setCancelled(boolean cancelled);

  void setReplacementPacket(Packet<?> replacementPacket);

  void waitForSync(Callable<?> runnable);
}

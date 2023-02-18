package net.forthecrown.user.packet;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnDisable;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.network.protocol.Packet;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketListeners {

  static final Logger LOGGER = Loggers.getLogger();

  private static final Map<Class<?>, PacketHandlerList<?>> handlerLists
      = new Object2ObjectOpenHashMap<>();

  /**
   * The tag of vanilla packet handler that the packet listener is placed before
   */
  public static final String CHANNEL_TAG = "packet_handler";

  /**
   * Adds a given listener to the packet listener list
   *
   * @param listener The listener to register
   */
  public static void register(PacketListener listener) {
    for (Method m : listener.getClass().getDeclaredMethods()) {
      // Skip static methods
      if (Modifier.isStatic(m.getModifiers())) {
        continue;
      }

      // Get PacketHandler annotation if it
      // has one, if it doesn't, skip
      PacketHandler handler = m.getAnnotation(PacketHandler.class);
      if (handler == null) {
        continue;
      }

      m.setAccessible(true);

      // Validate that there's 2 parameters,
      // a packet class and a packet call class
      Validate.isTrue(m.getParameterCount() == 2,
          "Packet listeners must have 2 parameters, the packet and a PacketCall"
      );

      Class<?>[] params = m.getParameterTypes();

      // Validate parameter types
      Validate.isTrue(
          Packet.class.isAssignableFrom(params[0])
              && params[1] == PacketCall.class
              && !Modifier.isAbstract(params[0].getModifiers()),

          "Packet functions must have 2 params, first one is the packet " +
              "(cannot be abstract), second is a PacketCall"
      );

      // Validate that the return type is void
      Validate.isTrue(m.getReturnType() == Void.TYPE, "Packet listener method must return void");

      @SuppressWarnings("unchecked") // This is very much checked
      Class<? extends Packet<?>> packetClass
          = (Class<? extends Packet<?>>) params[0];

      registerListener(packetClass, handler, listener, m);
    }
  }

  private static <T extends Packet<?>> void registerListener(
      Class<T> type,
      PacketHandler handler,
      PacketListener listener,
      Method method
  ) {
    PacketHandlerList<T> list = getOrCreateList(type);

    PacketExecutor<T> executor = new PacketExecutor<>(
        handler.priority(),
        handler.ignoreCancelled(),
        listener,
        method
    );

    // Add executor
    list.addExecutor(executor);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Packet<?>> PacketHandlerList<T> getOrCreateList(
      Class<T> type
  ) {
    var list = handlerLists.computeIfAbsent(
        type,
        aClass -> new PacketHandlerList<>(type)
    );

    return (PacketHandlerList<T>) list;
  }

  /**
   * Unregisters the given listener from all packet listener lists.
   *
   * @param listener The listener to unregister
   */
  public static void unregister(PacketListener listener) {
    for (var v : handlerLists.values()) {
      v.removeAll(listener.getClass());
    }

    handlerLists.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }

  /**
   * Injects a channel handler into the given player to allow us to listen for packets and possibly
   * prevent them being written to the network or read from the network
   *
   * @param player The player to inject.
   */
  public static void inject(Player player) {
    var handler = new PacketListenerChannelHandler(player);
    ChannelPipeline pipeline = getChannel(player).pipeline();
    pipeline.addBefore(CHANNEL_TAG, player.getName(), handler);
  }

  /**
   * Removes the packet listener handler from the player
   *
   * @param player The player to remove the packet listener handle from
   */
  public static void uninject(Player player) {
    Channel channel = getChannel(player);

    channel.eventLoop().submit(() -> {
      channel.pipeline().remove(player.getName());
      return null;
    });
  }

  /**
   * Gets a user's connection channel
   *
   * @param player The player to get the channel of
   * @return The player's channel
   */
  private static Channel getChannel(Player player) {
    return VanillaAccess.getPacketListener(player).connection.channel;
  }

  /**
   * Calls the packet listeners for a given packet type
   *
   * @param packet The packet to call the listeners of
   * @param player The player the packet affects
   * @return True, if the packet was cancelled, false otherwise
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  static PacketCall call(Packet packet, Player player) {
    PacketHandlerList list = handlerLists.get(packet.getClass());

    if (list == null) {
      return null;
    }

    return list.run(packet, player);
  }

  /**
   * Removes packet handlers from every player
   */
  @OnDisable
  public static void removeAll() {
    for (var p : Bukkit.getOnlinePlayers()) {
      uninject(p);
    }
  }
}
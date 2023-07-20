package net.forthecrown.packet;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

class ListenersImpl implements PacketListeners {

  static final Logger LOGGER = Loggers.getLogger();

  static ListenersImpl listeners;

  private final Map<Class<?>, PacketHandlerList<?>> handlerLists
      = new Object2ObjectOpenHashMap<>();

  @Getter
  private final PacketRendererImpl renderingService = new PacketRendererImpl();

  public static final String CHANNEL_TAG = "packet_handler";

  public ListenersImpl() {
    register(new GamemodePacketListener());
    register(new SignPacketListener(renderingService));
  }

  public static ListenersImpl getListeners() {
    return listeners == null
        ? (listeners = new ListenersImpl())
        : listeners;
  }

  public void register(Object listener) {
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
      Preconditions.checkState(m.getParameterCount() == 2,
          "Packet listeners must have 2 parameters, the packet and a PacketCall"
      );

      Class<?>[] params = m.getParameterTypes();

      // Validate parameter types
      Preconditions.checkState(
          Packet.class.isAssignableFrom(params[0])
              && params[1] == PacketCall.class
              && !Modifier.isAbstract(params[0].getModifiers()),

          "Packet functions must have 2 params, first one is the packet " +
              "(cannot be abstract), second is a PacketCall"
      );

      // Validate that the return type is void
      Preconditions.checkState(
          m.getReturnType() == Void.TYPE,
          "Packet listener method must return void"
      );

      @SuppressWarnings("unchecked") // This is very much checked
      Class<? extends Packet<?>> packetClass
          = (Class<? extends Packet<?>>) params[0];

      registerListener(packetClass, handler, listener, m);
    }
  }

  private <T extends Packet<?>> void registerListener(
      Class<T> type,
      PacketHandler handler,
      Object listener,
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
  private <T extends Packet<?>> PacketHandlerList<T> getOrCreateList(
      Class<T> type
  ) {
    var list = handlerLists.computeIfAbsent(
        type,
        aClass -> new PacketHandlerList<>(type)
    );

    return (PacketHandlerList<T>) list;
  }

  public void unregister(Object listener) {
    for (var v : handlerLists.values()) {
      v.removeAll(listener.getClass());
    }

    handlerLists.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }

  public void inject(Player player) {
    var handler = new PacketListenerChannelHandler(player, this);
    ChannelPipeline pipeline = getChannel(player).pipeline();
    pipeline.addBefore(CHANNEL_TAG, player.getName(), handler);
  }

  public void uninject(Player player) {
    Channel channel = getChannel(player);

    channel.eventLoop().submit(() -> {
      channel.pipeline().remove(player.getName());
      return null;
    });
  }

  private Channel getChannel(Player player) {
    return VanillaAccess.getPacketListener(player).connection.channel;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  PacketCall call(Packet packet, Player player) {
    PacketHandlerList list = handlerLists.get(packet.getClass());

    if (list == null) {
      return null;
    }

    return list.run(packet, player);
  }

  public void shutdown() {
    for (var p : Bukkit.getOnlinePlayers()) {
      uninject(p);
    }
  }
}

package net.forthecrown.vanilla.packet;

import static net.forthecrown.vanilla.utils.Accessors.ACCESSOR_CUSTOM_NAME;
import static net.forthecrown.vanilla.utils.Accessors.ACCESSOR_TEXT_DISPLAY_TEXT;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.papermc.paper.adventure.PaperAdventure;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.forthecrown.Loggers;
import net.forthecrown.packet.EntityRenderer;
import net.forthecrown.packet.PacketCall;
import net.forthecrown.packet.PacketHandler;
import net.forthecrown.packet.PacketListeners;
import net.forthecrown.packet.SignRenderer;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.vanilla.utils.Accessors;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.world.entity.Display.TextDisplay;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ListenersImpl implements PacketListeners {

  static final Logger LOGGER = Loggers.getLogger();

  public static final String CHANNEL_TAG = "packet_handler";

  static ListenersImpl listeners;

  private final Map<Class<?>, PacketHandlerList<?>> handlerLists = new Object2ObjectOpenHashMap<>();

  private final Registry<SignRenderer> signRenderers = Registries.newRegistry();
  private final Registry<EntityRenderer> entityRenderers = Registries.newRegistry();

  public ListenersImpl() {
  }

  public void initalize() {
    register(new GamemodePacketListener());

    register(new SignPacketListener(this));
    register(new EntityPacketListener(this));

  }

  @Override
  public void setEntityDisplay(
      @NotNull Entity entity,
      @NotNull Player viewer,
      @Nullable Component text
  ) {
    Objects.requireNonNull(entity, "Null entity");
    Objects.requireNonNull(viewer, "Null viewer");

    net.minecraft.world.entity.Entity nmsEntity = VanillaAccess.getEntity(entity);
    DataValue<?> value;

    if (text == null) {
      nmsEntity.getEntityData().resendPossiblyDesyncedEntity(VanillaAccess.getPlayer(viewer));
      return;
    }

    if (nmsEntity instanceof TextDisplay) {
      EntityDataAccessor<net.minecraft.network.chat.Component> accessor
          = Accessors.find(ACCESSOR_TEXT_DISPLAY_TEXT, TextDisplay.class);

      net.minecraft.network.chat.Component vanillaValue;

      if (text == null) {
        vanillaValue = CommonComponents.EMPTY;
      } else {
        vanillaValue = PaperAdventure.asVanilla(text);
      }

      value = new DataValue<>(accessor.getId(), accessor.getSerializer(), vanillaValue);
    } else {
      EntityDataAccessor<Optional<net.minecraft.network.chat.Component>> accessor
          = Accessors.find(ACCESSOR_CUSTOM_NAME, net.minecraft.world.entity.Entity.class);

      Optional<net.minecraft.network.chat.Component> vanillaValue
          = Optional.ofNullable(PaperAdventure.asVanilla(text));

      value = new DataValue<>(accessor.getId(), accessor.getSerializer(), vanillaValue);
    }

    ClientboundSetEntityDataPacket packet
        = new ClientboundSetEntityDataPacket(entity.getEntityId(), List.of(value));

    var listener = VanillaAccess.getPacketListener(viewer);
    listener.send(packet);
  }

  @Override
  public Registry<SignRenderer> getSignRenderers() {
    return signRenderers;
  }

  @Override
  public Registry<EntityRenderer> getEntityRenderers() {
    return entityRenderers;
  }

  public boolean renderSign(Sign sign, WorldVec3i pos, Player player) {
    if (signRenderers.isEmpty()) {
      return false;
    }

    for (SignRenderer r: signRenderers) {
      if (!r.test(player, pos, sign)) {
        continue;
      }

      r.render(player, pos, sign);
      return true;
    }

    return false;
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

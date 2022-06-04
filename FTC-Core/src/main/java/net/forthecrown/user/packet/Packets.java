package net.forthecrown.user.packet;

import io.netty.channel.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.Crown;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.network.protocol.Packet;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Packets {
    private static final Logger LOGGER = Crown.logger();

    private static final Map<Class, PacketHandlerList> HANDLER_LISTS = new Object2ObjectOpenHashMap<>();

    public static void register(PacketListener listener) {
        for (Method m: listener.getClass().getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers())) continue;

            PacketHandler handler = m.getAnnotation(PacketHandler.class);
            if (handler == null) continue;

            m.setAccessible(true);

            Validate.isTrue(m.getParameterCount() == 2, "Packet listeners must have 2 parameters, the packet and a PacketCall");
            Class[] params = m.getParameterTypes();

            Validate.isTrue(
                    Packet.class.isAssignableFrom(params[0])
                            && params[1] == PacketCall.class
                            && !Modifier.isAbstract(params[0].getModifiers()),
                    "Packet functions must have 2 params, first one is the packet (cannot be abstract), second is a PacketCall"
            );

            Validate.isTrue(m.getReturnType() == Void.TYPE, "Packet listener method must return void");

            PacketHandlerList list = HANDLER_LISTS.computeIfAbsent(params[0], PacketHandlerList::new);
            PacketExecutor executor = new PacketExecutor(listener.getClass(), handler.priority(), handler.ignoreCancelled()) {
                @Override
                public void run(Packet packet, PacketCall call) {
                    try {
                        m.invoke(listener, packet, call);
                    } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                }
            };

            list.addExecutor(executor);
        }
    }

    public static void unregister(PacketListener listener) {
        for (var v: HANDLER_LISTS.values()) {
            v.removeAll(listener.getClass());
        }

        HANDLER_LISTS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public static final String CHANNEL_TAG = "packet_handler";

    public static void inject(Player player) {
        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof Packet<?> packet && call(packet, player)) {
                    return;
                }

                super.write(ctx, msg, promise);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof Packet<?> packet && call(packet, player)) {
                    return;
                }

                super.channelRead(ctx, msg);
            }
        };

        ChannelPipeline pipeline = VanillaAccess.getPlayer(player).connection.connection.channel.pipeline();
        pipeline.addBefore(CHANNEL_TAG, player.getName(), handler);
    }

    public static void uninject(Player player) {
        Channel channel = VanillaAccess.getPlayer(player).connection.connection.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    private static boolean call(Packet packet, Player player) {
        PacketHandlerList list = HANDLER_LISTS.get(packet.getClass());
        if (list == null) return false;

        return list.run(packet, player);
    }

    public static void removeAll() {
        for (var p: Bukkit.getOnlinePlayers()) {
            uninject(p);
        }
    }

    @RequiredArgsConstructor
    public static class PacketHandlerList<T extends Packet> {
        final Class<T> packetClass;
        final List<PacketExecutor<T>> executors = new ObjectArrayList<>();

        public void addExecutor(PacketExecutor<T> executor) {
            executors.add(executor);
            executors.sort(Comparator.comparingInt(o -> o.prio));
        }

        public void removeAll(Class c) {
            executors.removeIf(executor -> c == executor.getExecutorClass());
        }

        public boolean run(T packet, Player player) {
            PacketCall call = new PacketCall(player);

            for (var v: executors) {
                if (call.isCancelled() && v.ignoreCancelled) continue;

                try {
                    v.run(packet, call);
                } catch (Throwable t) {
                    LOGGER.error("Error running packet listener executor " + v.getExecutorClass().getSimpleName(), t);
                }
            }

            return call.isCancelled();
        }

        public boolean isEmpty() {
            return executors.isEmpty();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static abstract class PacketExecutor<T extends Packet> {
        private final Class executorClass;
        private final int prio;
        private final boolean ignoreCancelled;

        public abstract void run(T packet, PacketCall call);
    }
}
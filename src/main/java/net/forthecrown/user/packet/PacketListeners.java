package net.forthecrown.user.packet;

import io.netty.channel.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.FTC;
import net.forthecrown.core.module.OnDisable;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.network.protocol.Packet;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public class PacketListeners {
    static final Logger LOGGER = FTC.getLogger();

    private static final Map<Class, PacketHandlerList> HANDLER_LISTS = new Object2ObjectOpenHashMap<>();

    /**
     * The tag of vanilla packet handler that the
     * packet listener is placed before
     */
    public static final String CHANNEL_TAG = "packet_handler";

    /**
     * Adds a given listener to the packet listener list
     * @param listener The listener to register
     */
    public static void register(PacketListener listener) {
        for (Method m: listener.getClass().getDeclaredMethods()) {
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

            Class[] params = m.getParameterTypes();

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

            // Get the handler list for the packet type
            PacketHandlerList list = HANDLER_LISTS.computeIfAbsent(params[0], PacketHandlerList::new);
            PacketExecutor executor = new PacketExecutor(handler.priority(), handler.ignoreCancelled(), listener, m);

            // Add executor
            list.addExecutor(executor);
        }
    }

    /**
     * Unregisters the given listener from all packet listener
     * lists.
     * @param listener The listener to unregister
     */
    public static void unregister(PacketListener listener) {
        for (var v: HANDLER_LISTS.values()) {
            v.removeAll(listener.getClass());
        }

        HANDLER_LISTS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Injects a channel handler into the given player to
     * allow us to listen for packets and possibly prevent
     * them being written to the network or read from the
     * network
     * @param player The player to inject.
     */
    public static void inject(Player player) {
        var handler = new PacketListenerChannelHandler(player);
        ChannelPipeline pipeline = getChannel(player).pipeline();
        pipeline.addBefore(CHANNEL_TAG, player.getName(), handler);
    }

    /**
     * Removes the packet listener handler from the player
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
     * @param player The player to get the channel of
     * @return The player's channel
     */
    private static Channel getChannel(Player player) {
        return VanillaAccess.getPacketListener(player).connection.channel;
    }

    /**
     * Calls the packet listeners for a given
     * packet type
     *
     * @param packet The packet to call the listeners of
     * @param player The player the packet affects
     * @return True, if the packet was cancelled, false otherwise
     */
    static PacketCall call(Packet packet, Player player) {
        PacketHandlerList list = HANDLER_LISTS.get(packet.getClass());

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
        for (var p: Bukkit.getOnlinePlayers()) {
            uninject(p);
        }
    }

}
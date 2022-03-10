package net.forthecrown.user.packets;

import io.netty.channel.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.ListUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public final class PacketListeners {
    private PacketListeners() {}

    private static final Map<Class, List<PacketListener>> LISTENERS = new Object2ObjectOpenHashMap<>();

    public static void inject(Player player) {
        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                if(packet instanceof Packet && call((Packet) packet, player)) {
                    return;
                }

                super.write(ctx, packet, promise);
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), handler);
    }

    public static void remove(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    public static void removeAll() {
        Bukkit.getOnlinePlayers().forEach(PacketListeners::remove);
    }

    public static <T extends Packet<ClientGamePacketListener>> void register(Class<T> clazz, PacketListener<T> listener) {
        List<PacketListener> listeners = LISTENERS.computeIfAbsent(clazz, aClass -> new ObjectArrayList<>());
        listeners.add(listener);
    }

    public static boolean call(Packet packet, Player player) {
        PacketContext context = new PacketContext(packet, UserManager.getLoadedUser(player.getUniqueId()));

        List<PacketListener> listeners = LISTENERS.get(packet.getClass());
        if(ListUtils.isNullOrEmpty(listeners)) return false;

        listeners.forEach(l -> {
            try {
                l.onPacketSend(context);
            } catch (Exception e) {
                new RuntimeException("Exception while calling packet listener", e).printStackTrace();
            }
        });

        return context.cancelled;
    }
}

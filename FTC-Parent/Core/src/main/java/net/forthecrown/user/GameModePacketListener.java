package net.forthecrown.user;

import io.netty.channel.*;
import net.forthecrown.core.chat.Announcer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ListIterator;

public class GameModePacketListener {
    public static void inject(Player player) {
        new InjectorInstance(player);
    }

    public static void removeAll() {
        Bukkit.getOnlinePlayers().forEach(GameModePacketListener::remove);
    }

    public static void remove(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    private static class InjectorInstance {
        private final Player player;

        public InjectorInstance(Player player) {
            this.player = player;

            inject();
        }

        private void inject() {
            ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                @Override
                public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                    if(packet instanceof ClientboundPlayerInfoPacket) {
                        ClientboundPlayerInfoPacket infoPacket = (ClientboundPlayerInfoPacket) packet;
                        if (infoPacket.getAction() == ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE || infoPacket.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
                            ListIterator<ClientboundPlayerInfoPacket.PlayerUpdate> iterator = infoPacket.getEntries().listIterator();

                            Announcer.debug("Receiver: " + player.getName());
                            while (iterator.hasNext()) {
                                ClientboundPlayerInfoPacket.PlayerUpdate u = iterator.next();
                                if(!u.getProfile().getId().equals(player.getUniqueId())) return;
                            }
                        }
                    }

                    super.write(ctx, packet, promise);
                }
            };

            ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline();
            pipeline.addBefore("packet_handler", player.getName(), handler);
        }
    }
}

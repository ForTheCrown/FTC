package net.forthecrown.user;

import io.netty.channel.*;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.world.level.GameType;
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
        private final Player receiver;

        public InjectorInstance(Player player) {
            this.receiver = player;

            inject();
        }

        private void inject() {
            ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                @Override
                public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                    if(packet instanceof ClientboundPlayerInfoPacket) {
                        ClientboundPlayerInfoPacket infoPacket = (ClientboundPlayerInfoPacket) packet;
                        ListIterator<ClientboundPlayerInfoPacket.PlayerUpdate> iterator = infoPacket.getEntries().listIterator();

                        if (infoPacket.getAction() == ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE) {
                            while (iterator.hasNext()) {
                                ClientboundPlayerInfoPacket.PlayerUpdate u = iterator.next();
                                if(u.getGameMode() != GameType.SPECTATOR) continue;
                                if(!u.getProfile().getId().equals(receiver.getUniqueId())) return;
                            }
                        } else if(infoPacket.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
                            //I swear to god, if a player receives a packet of their own player being added
                            //I will murder someone

                            while (iterator.hasNext()) {
                                ClientboundPlayerInfoPacket.PlayerUpdate u = iterator.next();
                                if(u.getGameMode() != GameType.SPECTATOR) continue;

                                iterator.set(new ClientboundPlayerInfoPacket.PlayerUpdate(u.getProfile(), u.getLatency(), GameType.SURVIVAL, u.getDisplayName()));
                            }
                        }
                    }

                    super.write(ctx, packet, promise);
                }
            };

            ChannelPipeline pipeline = ((CraftPlayer) receiver).getHandle().connection.connection.channel.pipeline();
            pipeline.addBefore("packet_handler", receiver.getName(), handler);
        }
    }
}

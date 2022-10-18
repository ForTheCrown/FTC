package net.forthecrown.events.player;

import net.forthecrown.core.Crown;
import net.forthecrown.user.packet.PacketCall;
import net.forthecrown.user.packet.PacketHandler;
import net.forthecrown.user.packet.PacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.world.level.GameType;
import org.apache.logging.log4j.Logger;

import java.util.ListIterator;
import java.util.UUID;

public class PlayerPacketListener implements PacketListener {
    private static final Logger LOGGER = Crown.logger();

    @PacketHandler(ignoreCancelled = true)
    public void onGameModePacket(ClientboundPlayerInfoPacket packet, PacketCall call) {
        UUID target = call.getPlayer().getUniqueId();
        ListIterator<ClientboundPlayerInfoPacket.PlayerUpdate> iterator = packet.getEntries().listIterator();

        if (packet.getAction() == ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE) {
            while (iterator.hasNext()) {
                ClientboundPlayerInfoPacket.PlayerUpdate u = iterator.next();

                if (u.getGameMode() != GameType.SPECTATOR) {
                    continue;
                }

                if(!u.getProfile().getId().equals(target)) {
                    call.setCancelled(true);
                    return;
                }
            }
        } else if (packet.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
            //I swear to god, if a player receives a packet of their own player being added
            //I will murder someone...
            //Well, I guess I have to :shrug:

            while (iterator.hasNext()) {
                ClientboundPlayerInfoPacket.PlayerUpdate u = iterator.next();

                if (u.getGameMode() != GameType.SPECTATOR
                        || u.getProfile().getId().equals(target)
                ) {
                    continue;
                }

                iterator.set(new ClientboundPlayerInfoPacket.PlayerUpdate(
                        u.getProfile(),
                        u.getLatency(),
                        GameType.SURVIVAL,
                        u.getDisplayName(),
                        u.getProfilePublicKey()
                ));
            }
        }
    }
}
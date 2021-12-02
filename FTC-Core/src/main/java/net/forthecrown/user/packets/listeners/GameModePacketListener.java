package net.forthecrown.user.packets.listeners;

import net.forthecrown.user.packets.PacketContext;
import net.forthecrown.user.packets.PacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.world.level.GameType;

import java.util.ListIterator;

public class GameModePacketListener implements PacketListener<ClientboundPlayerInfoPacket> {
    @Override
    public void onPacketSend(PacketContext<ClientboundPlayerInfoPacket> context) {
        ClientboundPlayerInfoPacket infoPacket = context.packet;
        ListIterator<ClientboundPlayerInfoPacket.PlayerUpdate> iterator = infoPacket.getEntries().listIterator();

        if (infoPacket.getAction() == ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE) {
            while (iterator.hasNext()) {
                ClientboundPlayerInfoPacket.PlayerUpdate u = iterator.next();
                if(u.getGameMode() != GameType.SPECTATOR) continue;

                if(!u.getProfile().getId().equals(context.target.getUniqueId())) {
                    context.cancelled = true;
                    return;
                }
            }
        } else if(infoPacket.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
            //I swear to god, if a player receives a packet of their own player being added
            //I will murder someone...
            //Well, I guess I have to :shrug:

            while (iterator.hasNext()) {
                ClientboundPlayerInfoPacket.PlayerUpdate u = iterator.next();
                if(u.getGameMode() != GameType.SPECTATOR || u.getProfile().getId().equals(context.target.getUniqueId())) continue;

                iterator.set(new ClientboundPlayerInfoPacket.PlayerUpdate(u.getProfile(), u.getLatency(), GameType.SURVIVAL, u.getDisplayName()));
            }
        }
    }
}

package net.forthecrown.user.packets.listeners;

import net.forthecrown.user.packets.PacketContext;
import net.forthecrown.user.packets.PacketListener;
import net.forthecrown.utils.ListUtils;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;

public class SignChunkLoadListener implements PacketListener<ClientboundLevelChunkPacket> {
    @Override
    public void onPacketSend(PacketContext<ClientboundLevelChunkPacket> context) {
        ClientboundLevelChunkPacket packet = context.packet;

        if(ListUtils.isNullOrEmpty(packet.getBlockEntitiesTags())) return;

        packet.getBlockEntitiesTags().parallelStream()
                .filter(SignRenderPacketListener::isShopTag)
                .forEach(t -> SignRenderPacketListener.checkCompoundTag(context.target, t));
    }
}

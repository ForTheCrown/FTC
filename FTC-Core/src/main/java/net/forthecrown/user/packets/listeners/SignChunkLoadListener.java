package net.forthecrown.user.packets.listeners;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.user.packets.PacketContext;
import net.forthecrown.user.packets.PacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;

import java.lang.reflect.Field;
import java.util.List;

public class SignChunkLoadListener implements PacketListener<ClientboundLevelChunkWithLightPacket> {
    public static final String BLOCK_ENTITY_DATA_SPIGOT_FIELD = "blockEntitiesData";
    public static final String DATA_TAG_SPIGOT_FIELD = "tag";

    @Override
    public void onPacketSend(PacketContext<ClientboundLevelChunkWithLightPacket> context) {
        ClientboundLevelChunkWithLightPacket packet = context.packet;

        try {
            getBlockEntityTags(packet.getChunkData()).parallelStream()
                    .filter(SignRenderPacketListener::isShopTag)
                    .forEach(t -> SignRenderPacketListener.transformTag(context.target, t));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static List<CompoundTag> getBlockEntityTags(ClientboundLevelChunkPacketData data) throws IllegalAccessException, NoSuchFieldException {
        List<CompoundTag> info = new ObjectArrayList<>();

        Field f = data.getClass().getDeclaredField(BLOCK_ENTITY_DATA_SPIGOT_FIELD);
        f.setAccessible(true);

        List nmsList = (List) f.get(data);

        for (Object obj: nmsList) {
            Field oField = obj.getClass().getDeclaredField(DATA_TAG_SPIGOT_FIELD);
            oField.setAccessible(true);

            CompoundTag tag = (CompoundTag) oField.get(obj);
        }

        return info;
    }
}

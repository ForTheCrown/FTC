package net.forthecrown.user.packets.listeners;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.user.packets.PacketContext;
import net.forthecrown.user.packets.PacketListener;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;

import java.lang.reflect.Field;
import java.util.List;

public class SignChunkLoadListener implements PacketListener<ClientboundLevelChunkWithLightPacket> {
    public static final String BLOCK_ENTITY_DATA_SPIGOT_FIELD = "d";
    public static final String DATA_TAG_SPIGOT_FIELD = "d";

    public static final Field BLOCK_ENTITY_DATA_FIELD = Util.make(() -> {
        Field f = null;

        try {
            f = ClientboundLevelChunkPacketData.class.getDeclaredField(BLOCK_ENTITY_DATA_SPIGOT_FIELD);
            f.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return f;
    });

    public static final Field DATA_TAG_FIELD = Util.make(() -> {
        Field f = null;

        try {
            Class c = ClientboundLevelChunkPacketData.class.getDeclaredClasses()[0];
            Crown.logger().info(c.getName());
            f = c.getDeclaredField(DATA_TAG_SPIGOT_FIELD);
            f.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return f;
    });

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


    public static List<CompoundTag> getBlockEntityTags(ClientboundLevelChunkPacketData data) throws IllegalAccessException {
        List<CompoundTag> info = new ObjectArrayList<>();
        List nmsList = (List) BLOCK_ENTITY_DATA_FIELD.get(data);

        for (Object obj: nmsList) {
            CompoundTag tag = (CompoundTag) DATA_TAG_FIELD.get(obj);
            info.add(tag);
        }

        return info;
    }
}

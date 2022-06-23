package net.forthecrown.events;

import com.sk89q.worldedit.math.Vector3;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.shops.FtcSignShop;
import net.forthecrown.economy.shops.ShopConstants;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.packet.PacketCall;
import net.forthecrown.user.packet.PacketHandler;
import net.forthecrown.user.packet.PacketListener;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.Logger;
import org.bukkit.GameMode;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class PlayerPacketListener implements PacketListener {
    private static final Logger LOGGER = Crown.logger();

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

    private static final boolean APRIL_FOOLS = Util.make(() -> {
        ZonedDateTime time = ZonedDateTime.now();
        if (time.getMonth() != Month.APRIL) return false;

        return time.getDayOfMonth() == 1;
    });

    // The following commented code was going to be the base for
    // a lil anti-flying thing, but I gave up lol
    /*@PacketHandler(ignoreCancelled = true)
    public void onPlayerMove_pos(ServerboundMovePlayerPacket.Pos packet, PacketCall call) {
        playerMove(packet, call);
    }

    @PacketHandler(ignoreCancelled = true)
    public void onPlayerMove_rot(ServerboundMovePlayerPacket.PosRot packet, PacketCall call) {
        playerMove(packet, call);
    }*/

    void playerMove(ServerboundMovePlayerPacket packet, PacketCall call) {
        if (call.getPlayer().hasPermission(Permissions.GAMEMODES)) {
            return;
        }

        if (call.getPlayer().getGameMode() == GameMode.CREATIVE || call.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Location current = call.getPlayer().getLocation();
        Vector3 cur = Vector3.at(current.getX(), current.getY(), current.getZ());
        Vector3 pos = Vector3.at(packet.x, packet.y, packet.z);
        Vector3 dif = pos.subtract(cur);
        double lengthSq = dif.lengthSq();

        if (lengthSq == 0.0D) {
            return;
        }

        LOGGER.info("cur: {}, pos: {}, dif: {}", cur, pos, dif);
        LOGGER.info("LengthSQ: {}, length: {}", lengthSq, Math.sqrt(lengthSq));
    }

    @PacketHandler(ignoreCancelled = true)
    public void onGameModePacket(ClientboundPlayerInfoPacket packet, PacketCall call) {
        UUID target = call.getPlayer().getUniqueId();
        ListIterator<ClientboundPlayerInfoPacket.PlayerUpdate> iterator = packet.getEntries().listIterator();

        if (packet.getAction() == ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE) {
            while (iterator.hasNext()) {
                ClientboundPlayerInfoPacket.PlayerUpdate u = iterator.next();
                if(u.getGameMode() != GameType.SPECTATOR) continue;

                if(!u.getProfile().getId().equals(target)) {
                    call.setCancelled(true);
                    return;
                }
            }
        } else if(packet.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
            //I swear to god, if a player receives a packet of their own player being added
            //I will murder someone...
            //Well, I guess I have to :shrug:

            while (iterator.hasNext()) {
                ClientboundPlayerInfoPacket.PlayerUpdate u = iterator.next();
                if(u.getGameMode() != GameType.SPECTATOR || u.getProfile().getId().equals(target)) continue;

                iterator.set(new ClientboundPlayerInfoPacket.PlayerUpdate(u.getProfile(), u.getLatency(), GameType.SURVIVAL, u.getDisplayName(), u.getProfilePublicKey()));
            }
        }
    }

    @PacketHandler(ignoreCancelled = true)
    public void onChunkUpdate(ClientboundLevelChunkWithLightPacket packet, PacketCall call) {
        try {
            List<CompoundTag> data = getBlockEntityTags(packet.getChunkData());
            if(data.isEmpty()) return;

            data.stream()
                    .filter(this::isShopTag)
                    .forEach(t -> transformTag(call.getUser(), t));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PacketHandler(ignoreCancelled = true)
    public void onSignPacket(ClientboundBlockEntityDataPacket packet, PacketCall call) {
        if(packet.getType() != BlockEntityType.SIGN) return;
        CompoundTag tag = packet.getTag();

        if(!isShopTag(tag)) return;
        transformTag(call.getUser(), tag);
    }

    List<CompoundTag> getBlockEntityTags(ClientboundLevelChunkPacketData data) throws IllegalAccessException {
        List<CompoundTag> info = new ObjectArrayList<>();
        List nmsList = (List) BLOCK_ENTITY_DATA_FIELD.get(data);

        for (Object obj: nmsList) {
            CompoundTag tag = (CompoundTag) DATA_TAG_FIELD.get(obj);
            info.add(tag);
        }

        return info;
    }

    void transformTag(CrownUser user, CompoundTag tag) {
        WorldVec3i pos2 = new WorldVec3i(user.getWorld(),
                tag.getInt("x"),
                tag.getInt("y"),
                tag.getInt("z")
        );

        FtcSignShop shop = new FtcSignShop(pos2, true);
        try {
            shop.load(tag.getCompound("PublicBukkitValues").getCompound(ShopConstants.SHOP_KEY.asString()));
        } catch (Throwable t) {
            LOGGER.info("Tag: {}", tag);
            LOGGER.error("Cannot transform shop at " + pos2, t);
            return;
        }

        Component priceLine = shop.getPriceLineFor(user);

        if(APRIL_FOOLS) {
            priceLine = FtcFormatter.formatString(":shrug:");
        }

        String priceLineJson = ChatUtils.GSON.serialize(priceLine);

        tag.putString("Text4", priceLineJson);
        if(tag.contains("FilteredText4")) tag.putString("FilteredText4", priceLineJson);
    }

    boolean isShopTag(@Nullable CompoundTag tag) {
        if(tag == null || !tag.contains("PublicBukkitValues", Tag.TAG_COMPOUND) || !tag.contains("GlowingText")) return false;
        CompoundTag publicBukkit = tag.getCompound("PublicBukkitValues");

        return publicBukkit.contains(ShopConstants.SHOP_KEY.asString(), Tag.TAG_COMPOUND);
    }
}
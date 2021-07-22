package net.forthecrown.august;

import net.forthecrown.august.event.PinataEvent;
import net.forthecrown.squire.Squire;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.GlowSquid;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Collection;

public class EventUtil {

    public static NamespacedKey createEventKey(String value) {
        return Squire.createKey(EventConstants.EVENT_NAMESPACE, value);
    }

    public static GlowSquid findPinata() {
        Collection<GlowSquid> entities = EventConstants.PINATA_REGION.getEntitiesByType(GlowSquid.class);

        for (GlowSquid e: entities) {
            if(!e.getPersistentDataContainer().has(EventConstants.PINATA_KEY, PersistentDataType.BYTE)) continue;
            return e;
        }

        return spawnPinata(EventConstants.SPAWN);
    }

    public static void spawnPlusOne(Location loc) {
        ServerLevel level = ((CraftWorld) loc.getWorld()).getHandle();

        //These have to be different variables due to obfuscation mappings
        ArmorStand stand = EntityType.ARMOR_STAND.create(level);
        Entity entity = stand;
        entity.moveTo(loc.getX(), loc.getY(), loc.getZ());
        entity.setInvulnerable(true);
        entity.setCustomNameVisible(true);
        entity.setCustomName(new TextComponent("+1"));

        stand.setMarker(true);
        stand.setInvisible(true);
        stand.setNoBasePlate(true);

        level.addAllEntitiesSafely(stand, CreatureSpawnEvent.SpawnReason.CUSTOM);

        Bukkit.getScheduler().runTaskLater(AugustPlugin.inst, entity::discard, 20);
    }

    public static GlowSquid spawnPinata(Location loc) {
        return loc.getWorld().spawn(loc, GlowSquid.class, squid -> {
            squid.getPersistentDataContainer().set(EventConstants.PINATA_KEY, PersistentDataType.BYTE, (byte) 1);
            squid.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Double.MAX_VALUE);
        });
    }

    public static void dropRandomLoot(Location loc, CrownRandom random) {
        int pickedChance = random.intInRange(0, 100);

        for (PinataDrop d: EventConstants.DROPS) {
            if(!d.getRange().containsInteger(pickedChance)) continue;

            loc.getWorld().dropItemNaturally(loc, d.getItem());
            return;
        }
    }

    public static Vector findRandomDirection(Location loc, CrownRandom random) {
        if(!EventConstants.SAFE_ZONE.contains(loc)) {
            Vector vec = FtcUtils.betweenPoints(loc, EventConstants.MIDDLE);

            return vec
                    .setX(vec.getX() + random.intInRange(-2, 2))
                    .setY(vec.getY() + random.intInRange(-2, 2))
                    .setZ(vec.getZ() + random.intInRange(-2, 2));
        }

        return new Vector(
                random.intInRange(-5, 5),
                random.intInRange(2, 5),
                random.intInRange(-5, 5)
        );
    }

    public static boolean canEnter(Player player) {
        if(PinataEvent.currentEntry != null || PinataEvent.currentStarter != null) {
            player.sendMessage(Component.text("There is already someone in the event").color(NamedTextColor.RED));
            return false;
        }

        if(!player.getInventory().containsAtLeast(EventConstants.ticket(), 1)) {
            player.sendMessage(Component.text("You don't have a ticket to enter").color(NamedTextColor.RED));
            return false;
        }

        return true;
    }
}

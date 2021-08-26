package net.forthecrown.august;

import net.forthecrown.august.event.PinataEvent;
import net.forthecrown.squire.Squire;
import net.forthecrown.utils.CrownRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;

import static net.forthecrown.august.EventConstants.*;

public class EventUtil {

    public static NamespacedKey createEventKey(String value) {
        return Squire.createKey(EVENT_NAMESPACE, value);
    }

    public static Rabbit findPinata() {
        Collection<Rabbit> entities = PINATA_REGION.getEntitiesByType(Rabbit.class);

        for (Rabbit e: entities) {
            if(!e.getPersistentDataContainer().has(PINATA_KEY, PersistentDataType.BYTE)) continue;
            return e;
        }

        return spawnPinata(SPAWN);
    }

    public static void spawnPlusX(Location loc, int x) {
        ServerLevel level = ((CraftWorld) loc.getWorld()).getHandle();

        MutableComponent name = new TextComponent("+" + x);
        name.withStyle(ChatFormatting.YELLOW);

        //These have to be different variables due to obfuscation mappings
        ArmorStand stand = EntityType.ARMOR_STAND.create(level);
        stand.setMarker(true);
        stand.setNoBasePlate(true);

        Entity entity = stand;
        entity.moveTo(loc.getX(), loc.getY(), loc.getZ());
        entity.setInvulnerable(true);
        entity.setCustomNameVisible(true);
        entity.setCustomName(name);
        entity.setInvisible(true);

        level.addAllEntitiesSafely(stand, CreatureSpawnEvent.SpawnReason.CUSTOM);

        Bukkit.getScheduler().runTaskLater(AugustPlugin.inst, entity::discard, 20);
    }

    public static Rabbit spawnPinata(Location loc) {
        return loc.getWorld().spawn(loc, Rabbit.class, pinata -> {
            pinata.getPersistentDataContainer().set(PINATA_KEY, PersistentDataType.BYTE, (byte) 1);
            
            pinata.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(SQUID_HEALTH);
            pinata.setHealth(SQUID_HEALTH);

            pinata.setAdult();

            pinata.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 2, false, false));

            pinata.setCustomNameVisible(true);
            pinata.customName(PINATA_NAME);
        });
    }

    //Kill all the bebes so no one else can use them after you've left the event
    public static void killAllBebes() {
        ARENA_REGION.getEntities()
                .stream().filter(e -> e.getPersistentDataContainer().has(BEBE_KEY, PersistentDataType.BYTE))
                .forEach(org.bukkit.entity.Entity::remove);
    }

    public static void spawnBabies(Location loc, Rabbit.Type type) {
        loc.getWorld().spawnParticle(Particle.TOTEM, loc, 20, 0, 0, 0, 0.2);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.25f, 1.5f);
        loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.5f);

        for(int i = 0; i < BEBE_NAMES.length; i++) {
            int finalI = i;
            loc.getWorld().spawn(loc, Rabbit.class, bebe -> {
                bebe.getPersistentDataContainer().set(BEBE_KEY, PersistentDataType.BYTE, (byte) 1);

                bebe.setBaby();
                bebe.setRabbitType(type);

                //Give em each a name and make it visible
                bebe.customName(BEBE_NAMES[finalI]);
                bebe.setCustomNameVisible(true);

                bebe.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(SQUID_HEALTH);
                bebe.setHealth(SQUID_HEALTH);
                bebe.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
                bebe.setVelocity(new Vector(0, 0.35, 0));
            });
        }
    }

    public static void dropRandomLoot(Location loc, CrownRandom random) {
        int pickedChance = random.intInRange(0, 100);

        for (PinataDrop d: DROPS) {
            if(!d.getRange().containsInteger(pickedChance)) continue;

            loc.getWorld().dropItemNaturally(loc, d.getItem());
            return;
        }
    }

    public static Vector findRandomDirection(CrownRandom random) {
        return new Vector(
                randomDouble(random, VELOCITY_BOUND),
                Math.max(random.nextDouble(), 0.75),
                randomDouble(random, VELOCITY_BOUND)
        );
    }

    private static double randomDouble(CrownRandom random, int bound) {
        return Math.max(random.nextDouble(), 0.08) * ((double) random.intInRange(-bound, bound));
    }

    public static boolean canEnter(Player player) {
        if(PinataEvent.currentEntry != null || PinataEvent.currentStarter != null) {
            player.sendMessage(Component.text("There is already someone in the event").color(NamedTextColor.RED));
            return false;
        }

        if(!player.getInventory().containsAtLeast(ticket(), 1)) {
            player.sendMessage(Component.text("You don't have a ticket to enter").color(NamedTextColor.RED));
            return false;
        }

        return true;
    }
}
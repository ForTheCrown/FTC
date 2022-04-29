package net.forthecrown.dungeons;

import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.squire.Squire;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotConfig;

public final class DungeonUtils {
    private DungeonUtils() {}

    public static final NamespacedKey PUNCHING_BAG_KEY = Squire.createRoyalKey("dummy");
    public static final Component DUNGEON_LORE = Component.text("Dungeon Item");

    public static ItemStack makeDungeonItem(Material material, int amount, @Nullable String name) {
        return makeDungeonItem(material, amount, name == null ? null : Component.text(name).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
    }

    public static ItemStack makeDungeonItem(Material material, int amount, @Nullable Component name){
        return new ItemStackBuilder(material, amount)
                .setName(name)
                .addLore(DUNGEON_LORE)
                .build();
    }

    public static @Nullable Player getNearestVisiblePlayer(LivingEntity origin, WorldBounds3i inBox){
        Location location = origin.getEyeLocation();
        double lastDistance = Double.MAX_VALUE;
        Player result = null;
        for (Player p: inBox.getPlayers()){
            if(p.equals(result)) continue;
            if(p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;

            if(!origin.hasLineOfSight(p)) continue;

            double distance = p.getLocation().distance(location);
            if(distance < lastDistance){
                lastDistance = distance;
                result = p;
            }
        }
        return result;
    }

    public static @Nullable Player getOptimalTarget(LivingEntity e, WorldBounds3i inBox){
        Player result = getNearestVisiblePlayer(e, inBox);

        if(e.getLastDamageCause() == null) return result;
        if(!(e.getLastDamageCause() instanceof EntityDamageByEntityEvent event)) return result;

        if(event.getDamager() instanceof Player) {
            if (inBox.contains(event.getDamager())) result = (Player) event.getDamager();
        }
        return result;
    }

    public static void spawnDummy(Location location){
        location.getWorld().spawn(location, Husk.class, zomzom -> {
            zomzom.getEquipment().setHelmet(new ItemStack(Material.HAY_BLOCK));
            zomzom.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            zomzom.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
            zomzom.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

            zomzom.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(SpigotConfig.maxHealth);
            zomzom.setHealth(SpigotConfig.maxHealth);

            zomzom.setAI(false);
            zomzom.stopSound(SoundStop.all());
            zomzom.setGravity(false);

            zomzom.setCustomNameVisible(true);
            zomzom.customName(Component.text("Hit Me!").color(NamedTextColor.GOLD));

            zomzom.setRemoveWhenFarAway(false);
            zomzom.setPersistent(true);
            zomzom.setCanPickupItems(false);
            zomzom.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
            zomzom.getPersistentDataContainer().set(PUNCHING_BAG_KEY, PersistentDataType.BYTE, (byte) 1);
        });
    }

    public static void giveOrDropItem(Inventory inv, Location loc, ItemStack item) {
        if(inv.firstEmpty() == -1) {
            loc.getWorld().dropItem(loc, item);
        } else {
            inv.addItem(item);
        }
    }

    public static void cannotHarmEffect(World world, Entity entity) {
        world.playSound(entity.getLocation(), org.bukkit.Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.3f, 1.2f);
        world.spawnParticle(Particle.SQUID_INK, entity.getLocation().add(0, entity.getHeight()*0.66, 0), 5, 0.1D, 0.1D, 0.1D, 0.05D);
    }

    // getModifiers().clear() doesn't work because the returned list of
    // getModifiers() is a copied collection
    public static void clearModifiers(AttributeInstance instance) {
        for (AttributeModifier m: instance.getModifiers()) {
            instance.removeModifier(m);
        }
    }
}
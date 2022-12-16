package net.forthecrown.dungeons;

import net.forthecrown.core.registry.Keys;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import static net.forthecrown.core.Messages.DUNGEON_LORE;

public final class DungeonUtils {
    private DungeonUtils() {}

    public static final NamespacedKey PUNCHING_BAG_KEY = Keys.royals("dummy");

    public static ItemStack makeDungeonItem(Material material, int amount, @Nullable String name) {
        return makeDungeonItem(
                material,
                amount,
                name == null ? null : Component.text(name)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        );
    }

    public static ItemStack makeDungeonItem(Material material, int amount, @Nullable Component name) {
        return ItemStacks.builder(material, amount)
                .setNameRaw(name)
                .addLoreRaw(DUNGEON_LORE)
                .build();
    }

    public static @Nullable Player getNearestVisiblePlayer(LivingEntity origin, WorldBounds3i inBox){
        Location location = origin.getEyeLocation();
        double lastDistance = Double.MAX_VALUE;
        Player result = null;

        for (Player p: inBox.getPlayers()) {
            if(p.equals(result)) {
                continue;
            }

            if (p.getGameMode() == GameMode.SPECTATOR
                    || p.getGameMode() == GameMode.CREATIVE
            ) {
                continue;
            }

            if (!origin.hasLineOfSight(p)) {
                continue;
            }

            double distance = p.getLocation().distance(location);
            if (distance < lastDistance) {
                lastDistance = distance;
                result = p;
            }
        }
        return result;
    }

    public static @Nullable Player getOptimalTarget(LivingEntity e, WorldBounds3i inBox){
        Player result = getNearestVisiblePlayer(e, inBox);

        if (!(e.getLastDamageCause() instanceof EntityDamageByEntityEvent event)) {
            return result;
        }

        if (event.getDamager() instanceof Player player && inBox.contains(player)) {
            result = player;
        }
        return result;
    }

    public static void cannotHarmEffect(World world, Entity entity) {
        world.playSound(entity.getLocation(), org.bukkit.Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.3f, 1.2f);
        world.spawnParticle(Particle.SQUID_INK, entity.getLocation().add(0, entity.getHeight()*0.66, 0), 5, 0.1D, 0.1D, 0.1D, 0.05D);
    }

}
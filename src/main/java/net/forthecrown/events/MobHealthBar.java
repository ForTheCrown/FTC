package net.forthecrown.events;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.module.OnDisable;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.spongepowered.math.GenericMath;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class MobHealthBar implements Listener {
    public static final String DEF_HEART = "❤";

    public static final Map<LivingEntity, Component> NAMES = new HashMap<>();
    public static final Map<LivingEntity, BukkitTask> HITMOBS = new HashMap<>();
    public static final Set<ArmorStand> HIT_MARKERS = new ObjectOpenHashSet<>();

    public static final double MAX_DAMAGE_INDICATION = 20;

    private static void delay(LivingEntity damaged) {
        BukkitTask task = HITMOBS.get(damaged);
        Tasks.cancel(task);

        task = Tasks.runLater(() -> {
            damaged.setCustomNameVisible(false);
            damaged.customName(NAMES.get(damaged));

            NAMES.remove(damaged);
            HITMOBS.remove(damaged);
        }, 5 * 20);

        HITMOBS.put(damaged, task); //Put delay in map
    }

    public static void showHealthbar(LivingEntity damaged, double finalDamage, String heartChar, boolean autoRemove) {
        if (damaged.getHealth() - finalDamage <= 0) {
            return;
        }

        String name = damaged.getCustomName();

        // Only affect entities that only show names when player hovers mouse over them:
        // (Note: colored names can get replaced, they return properly anyway)
        if (name != null) {
            if (!name.contains(heartChar)) {
                if (damaged.isCustomNameVisible()) {
                    return; // Don't change names of entities with always visible names (without hearts in them)
                } else {
                    NAMES.put(damaged, damaged.customName()); // Save names of player-named entities
                }
            }
        }

        // Calculate hearts to show:
        int maxHealth = (int) Math.ceil(damaged.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() / 2);
        int remainingHRTS = (int) Math.ceil((damaged.getHealth() - finalDamage) / 2);

        if (remainingHRTS < 0) {
            remainingHRTS = 0;
        }

        if (remainingHRTS > 20) {
            return;
        }

        int heartsToShow = Math.min(maxHealth, 20); // Entities with too many hearts, can at max show 20 hearts, if their health is above that, hearts don't show.

        // Construct name with correct hearts:
        String healthBar = ChatColor.RED + "";
        for (int i = 0; i < remainingHRTS; i++) {
            healthBar += heartChar;
        }

        healthBar += ChatColor.GRAY + "";
        for (int i = remainingHRTS; i < heartsToShow; i++) {
            healthBar += heartChar;
        }

        // Show hearts + set timer to remove hearts
        // By having a Map<LivingEntity, BukkitRunnable>, we can dynamically delay the custom name being
        // turned back into normal
        damaged.setCustomNameVisible(true);
        damaged.setCustomName(healthBar);

        if (autoRemove) {
            delay(damaged);
        }
    }

    public static void spawnDamageNumber(BoundingBox entityBounds, World world, double damage) {
        var random = Util.RANDOM;

        double x = random.nextDouble(entityBounds.getMinX() - 0.5D, entityBounds.getMaxX() + 0.5D);
        double y = random.nextDouble(entityBounds.getMinY() + 0.5D, entityBounds.getMaxY() + 0.5D);
        double z = random.nextDouble(entityBounds.getMinZ() - 0.5D, entityBounds.getMaxZ() + 0.5D);

        var entity = world.spawn(
                new Location(world, x, y, z),
                ArmorStand.class,

                stand -> {
                    stand.setCanTick(true);
                    stand.setMarker(true);
                    stand.setInvisible(true);
                    stand.setCustomNameVisible(true);
                    stand.setCanMove(false);
                    stand.setGravity(false);
                    stand.setAI(false);
                    stand.setArms(false);

                    stand.customName(
                            Component.text(
                                    String.format("%.2f", damage),
                                    damageColor(damage)
                            )
                    );
                }
        );

        HIT_MARKERS.add(entity);

        Tasks.runTimer(new Consumer<>() {
            int ticks = 12;

            @Override
            public void accept(BukkitTask task) {
                if (--ticks < 0) {
                    Tasks.cancel(task);
                    entity.remove();
                    HIT_MARKERS.remove(entity);

                    return;
                }

                entity.teleport(entity.getLocation().add(0, 0.05D, 0));
            }
        }, 1, 1);
    }

    private static TextColor damageColor(double dmg) {
        double progress = dmg / MAX_DAMAGE_INDICATION;
        progress = GenericMath.clamp(progress, 0.0F, 1.0F);

        return TextColor.lerp((float) progress, NamedTextColor.YELLOW, NamedTextColor.RED);
    }

    @OnDisable
    public static void shutdown() {
        NAMES.forEach(Entity::customName);
        HIT_MARKERS.forEach(Entity::remove);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMobDamage(EntityDamageByEntityEvent event) {
        // Check damager is player or arrow shot by player
        if (!(event.getDamager() instanceof Player)) {
            if (!(event.getDamager() instanceof Projectile proj)) {
                return;
            }

            if (!(proj.getShooter() instanceof Player)) {
                return;
            }
        }

        // Not in world_void
        if (event.getEntity().getWorld().equals(Worlds.voidWorld())) {
            return;
        }

        // Must be alive
        if (!(event.getEntity() instanceof LivingEntity damaged)) {
            return;
        }

        // But not another player, armor stand or Boss mob
        if (event.getEntity() instanceof Player
                || event.getEntity() instanceof ArmorStand
                || event.getEntity() instanceof Boss
        ) {
            return;
        }

        var finalDamage = event.getFinalDamage();

        showHealthbar(damaged, finalDamage, DEF_HEART, true);

        if (!PunchingBags.isPunchingBag(damaged)) {
            spawnDamageNumber(damaged.getBoundingBox(), damaged.getWorld(), finalDamage);
        }
    }

    //Death messsage
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!event.getDeathMessage().contains("❤")) {
            return;
        }

        if (!(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent)) {
            return;
        }

        String name = Text.prettyEnumName(damageEvent.getDamager().getType());
        String message = event.getDeathMessage().replaceAll("❤", "") + name;

        event.setDeathMessage(message);
    }
}
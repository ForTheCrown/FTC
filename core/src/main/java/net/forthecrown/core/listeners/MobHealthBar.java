package net.forthecrown.core.listeners;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import net.forthecrown.Worlds;
import net.forthecrown.core.CorePlugin;
import net.forthecrown.text.Text;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.Tasks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.spongepowered.math.GenericMath;

public class MobHealthBar implements Listener {

  public static final String HEART = "❤";

  public static final Map<LivingEntity, Component> NAMES = new HashMap<>();
  public static final Map<LivingEntity, BukkitTask> HITMOBS = new HashMap<>();
  public static final Set<ArmorStand> HIT_MARKERS = new ObjectOpenHashSet<>();

  private static final Random RANDOM = new Random();

  public static final double MAX_DAMAGE_INDICATION = 20;

  private final CorePlugin plugin;

  public MobHealthBar(CorePlugin plugin) {
    this.plugin = plugin;
  }

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

  public static void showHealthBar(LivingEntity damaged, double finalDamage, boolean autoRemove) {
    if (damaged.getHealth() - finalDamage <= 0 || finalDamage <= 0) {
      return;
    }

    String name = damaged.getCustomName();

    // Only affect entities that only show names when player hovers mouse over them:
    // (Note: colored names can get replaced, they return properly anyway)
    if (name != null && !name.contains(HEART)) {

      // Don't change names of entities with always visible names
      // (without hearts in them)
      if (damaged.isCustomNameVisible()) {
        return;
      } else {
        // Save names of player-named entities
        NAMES.put(damaged, damaged.customName());
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

    // Entities with too many hearts, can at max show 20 hearts, if their
    // health is above that, hearts don't show.
    int heartsToShow = Math.min(maxHealth, 20);

    // Construct name with correct hearts:
    String reds = HEART.repeat(remainingHRTS);
    String grays = HEART.repeat(Math.max(0, heartsToShow - remainingHRTS));

    // Show hearts + set timer to remove hearts
    // By having a Map<LivingEntity, BukkitRunnable>, we can dynamically delay the custom name being
    // turned back into normal
    damaged.setCustomNameVisible(true);
    damaged.customName(
        Component.text()
            .append(Component.text(reds, NamedTextColor.RED))
            .append(Component.text(grays, NamedTextColor.GRAY))
            .build()
    );

    if (autoRemove) {
      delay(damaged);
    }
  }

  public static void spawnDamageNumber(BoundingBox entityBounds, World world, double damage) {
    double x = RANDOM.nextDouble(entityBounds.getMinX() - 0.5D, entityBounds.getMaxX() + 0.5D);
    double y = RANDOM.nextDouble(entityBounds.getMinY() + 0.5D, entityBounds.getMaxY() + 0.5D);
    double z = RANDOM.nextDouble(entityBounds.getMinZ() - 0.5D, entityBounds.getMaxZ() + 0.5D);

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

    entity.getScheduler().runAtFixedRate(
        PluginUtil.getPlugin(),
        new Consumer<>() {
          int ticks = 12;

          @Override
          public void accept(ScheduledTask task) {
            if (--ticks < 0) {
              task.cancel();
              entity.remove();
              HIT_MARKERS.remove(entity);

              return;
            }

            entity.teleport(entity.getLocation().add(0, 0.05D, 0));
          }
        },
        entity::remove,
        1, 1
    );
  }

  private static TextColor damageColor(double dmg) {
    double progress = dmg / MAX_DAMAGE_INDICATION;
    progress = GenericMath.clamp(progress, 0.0F, 1.0F);

    return TextColor.lerp((float) progress, NamedTextColor.YELLOW, NamedTextColor.RED);
  }

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
    var config = plugin.getFtcConfig();

    if (config.mobHealthBarsEnabled()) {
      showHealthBar(damaged, finalDamage, true);
    }

    if (config.damageNumbersEnabled()) {
      spawnDamageNumber(damaged.getBoundingBox(), damaged.getWorld(), finalDamage);
    }
  }

  //Death messsage
  @EventHandler(ignoreCancelled = true)
  public void onPlayerDeath(PlayerDeathEvent event) {
    if (!event.getDeathMessage().contains("❤")) {
      return;
    }

    var lastDamage = event.getEntity().getLastDamageCause();
    if (!(lastDamage instanceof EntityDamageByEntityEvent damageEvent)) {
      return;
    }

    String name = Text.prettyEnumName(damageEvent.getDamager().getType());
    String message = event.getDeathMessage().replaceAll("❤", "") + name;

    event.setDeathMessage(message);
  }
}
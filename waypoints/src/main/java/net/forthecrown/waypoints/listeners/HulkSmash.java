package net.forthecrown.waypoints.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import net.forthecrown.cosmetics.CosmeticData;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.events.Events;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import net.forthecrown.waypoints.Waypoints;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class HulkSmash implements Listener {

  /**
   * Determines the amount of game ticks between cosmetic effect tick
   */
  public static final byte GAME_TICKS_PER_COSMETIC_TICK = 1;

  private static final Map<UUID, HulkSmash> listeners = new HashMap<>();

  private final User user;
  private final TravelEffect effect;

  private boolean active = false;

  public static void startHulkSmash(User user, @Nullable TravelEffect effect) {
    HulkSmash listener = listeners.computeIfAbsent(
        user.getUniqueId(),
        uuid -> {
          TravelEffect eff;

          if (effect != null) {
            eff = effect;
          } else {
            eff = user.getComponent(CosmeticData.class).getValue(Cosmetics.TRAVEL_EFFECTS);
          }

          return new HulkSmash(user, eff);
        }
    );

    if (!listener.active) {
      listener.beginListening();
    }
  }

  public static void interrupt(User user) {
    Objects.requireNonNull(user);
    var smash = listeners.get(user.getUniqueId());
    smash.unregister(false);
  }

  public void beginListening() {
    user.set(Waypoints.HULK_SMASHING, true);
    active = true;

    Events.register(this);

    tickTask = Tasks.runTimer(
        this::tick,
        GAME_TICKS_PER_COSMETIC_TICK,
        GAME_TICKS_PER_COSMETIC_TICK
    );
  }

  private short ticks = 30 * (Ticks.TICKS_PER_SECOND / GAME_TICKS_PER_COSMETIC_TICK);
  private short groundTicks = 0;

  private BukkitTask tickTask;

  private void tick() {
    var vel = user.getPlayer().getVelocity();
    double velY = vel.getY();

    // Test if on ground, god-damn floating point errors, use some magic
    // floating point value that a player's Y velocity is always at, when
    // standing on the ground
    if (!user.getPlayer().isGliding() && velY >= -0.07841 && velY <= 0) {
      ++groundTicks;
    }

    // If been on ground for 5 or more ticks, stop
    if (groundTicks >= 5) {
      end();
      return;
    }

    // If below max fall tick, stop
    if (--ticks < 1) {
      unregister(true);
      return;
    }

    try {
      if (effect != null) {
        effect.onHulkTickDown(user, user.getLocation());
      }
    } catch (Exception e) {
      unregister(true);
    }
  }

  public void unregister(boolean unsetProperty) {
    HandlerList.unregisterAll(this);

    if (unsetProperty) {
      user.set(Waypoints.HULK_SMASHING, false);
    }

    listeners.remove(user.getUniqueId());
    active = false;

    tickTask = Tasks.cancel(tickTask);
  }

  private void end() {
    unregister(true);
    user.playSound(Sound.ENTITY_GENERIC_EXPLODE, 0.7F, 1);

    Particle.EXPLOSION_LARGE.builder()
        .location(user.getLocation())
        .allPlayers()
        .count(5)
        .extra(0.0D)
        .offset(1, 1, 1)
        .spawn();

    if (effect != null) {
      effect.onHulkLand(user, user.getLocation());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityDamage(EntityDamageEvent event) {
    if (!user.getUniqueId().equals(event.getEntity().getUniqueId())) {
      return;
    }

    if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
      return;
    }

    event.setCancelled(true);
    end();
  }
}
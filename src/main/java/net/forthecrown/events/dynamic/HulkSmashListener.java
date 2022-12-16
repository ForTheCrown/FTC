package net.forthecrown.events.dynamic;

import lombok.RequiredArgsConstructor;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.events.Events;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class HulkSmashListener implements Listener {
    /**
     * Determines the amount of game ticks between
     * cosmetic effect tick
     */
    public static final byte GAME_TICKS_PER_COSMETIC_TICK = 1; //Nice name, I know

    private final User user;
    private final TravelEffect effect;

    public void beginListening() {
        user.setVisitListener(this);
        user.hulkSmashing = true;

        Events.register(this);
        tickTask = Tasks.runTimer(this::tick, GAME_TICKS_PER_COSMETIC_TICK, GAME_TICKS_PER_COSMETIC_TICK);
    }

    private short ticks = 30 * (Ticks.TICKS_PER_SECOND / GAME_TICKS_PER_COSMETIC_TICK);
    private BukkitTask tickTask;

    private void tick() {
        if (--ticks < 1) {
            unregister();
            return;
        }

        try {
            if (effect != null) {
                effect.onHulkTickDown(user, user.getLocation());
            }
        } catch (Exception e) {
            unregister();
        }
    }

    public void unregister() {
        HandlerList.unregisterAll(this);

        user.setVisitListener(null);
        user.hulkSmashing = false;

        tickTask = Tasks.cancel(tickTask);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if(!user.getUniqueId().equals(event.getEntity().getUniqueId())) {
            return;
        }

        if(event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        event.setCancelled(true);
        unregister();
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
}
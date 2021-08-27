package net.forthecrown.events.dynamic;

import net.forthecrown.core.Crown;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.FtcUser;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.Queue;

import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.Bukkit.getScheduler;

public class RegionVisitListener implements Listener {
    public static final byte TICKS_PER_TICK = 1; //Nice name, I know

    private final FtcUser user;
    private final TravelEffect effect;
    private final Queue<Location> storedLocs = new LinkedList<>();

    public RegionVisitListener(CrownUser user) {
        this.user = (FtcUser) user;
        this.effect = user.getCosmeticData().getActiveTravel();
    }

    public void beginListening() {
        user.visitListener = this;

        getPluginManager().registerEvents(this, Crown.inst());
        tickTask = getScheduler().runTaskTimer(Crown.inst(), this::tick, TICKS_PER_TICK, TICKS_PER_TICK);
    }

    private short ticks = 10 * (20 / TICKS_PER_TICK);
    private BukkitTask tickTask;

    private void tick() {
        ticks--;
        if(ticks < 1) {
            unregister();
            return;
        }

        // Using queue to make locations lag behind player,
        // otherwise the particles spawn in their face lol
        if(effect != null) {
            storedLocs.offer(user.getLocation().clone());
            if (storedLocs.size() == 3) effect.onHulkTickDown(storedLocs.poll());
        }
    }

    public void unregister() {
        HandlerList.unregisterAll(this);

        user.visitListener = null;
        if(!tickTask.isCancelled()) tickTask.cancel();

        TravelEffect effect = user.getCosmeticData().getActiveTravel();
        if(effect != null) effect.onHulkLand(user.getLocation());

        storedLocs.clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if(!user.getUniqueId().equals(event.getEntity().getUniqueId())) return;
        if(event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        event.setCancelled(true);
        unregister();
    }
}

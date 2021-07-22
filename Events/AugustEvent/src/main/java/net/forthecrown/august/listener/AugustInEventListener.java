package net.forthecrown.august.listener;

import net.forthecrown.august.event.AugustEntry;
import net.forthecrown.august.AugustPlugin;
import net.forthecrown.august.EventUtil;
import net.forthecrown.crownevents.InEventListener;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.CrownRandom;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public class AugustInEventListener implements InEventListener, Listener {

    private final AugustEntry entry;
    private final Player player;
    private final Entity pinata;
    private final CrownRandom random;

    public AugustInEventListener(AugustEntry entry, Player player) {
        this.entry = entry;
        this.player = player;

        this.random = new CrownRandom();
        this.pinata = EventUtil.findPinata();
    }

    public boolean checkPlayer(Entity entity) {
        return player.equals(entity);
    }

    public boolean checkPinata(Entity entity) {
        return pinata.equals(entity);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if(!checkPlayer(event.getPlayer())) return;
        AugustPlugin.event.end(entry);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if(!checkPlayer(event.getEntity())) return;
        AugustPlugin.event.end(entry);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if(!checkPlayer(event.getDamager())) return;
        if(!checkPlayer(event.getEntity())) return;

        if(!Cooldown.contains(player)) {
            Cooldown.add(player, 1);
            entry.increment();
        }

        EventUtil.spawnPlusOne(pinata.getLocation().add(0, 1.25, 0));

        if(pinata.isOnGround()) {
            Vector velocity = EventUtil.findRandomDirection(pinata.getLocation(), random);
            pinata.setVelocity(velocity);
        }

        if(!Cooldown.contains(player, "Event_DropCooldown")) {
            Cooldown.add(player, "Event_DropCooldown", random.intInRange(20, 60));

            EventUtil.dropRandomLoot(pinata.getLocation(), random);
        }
    }
}

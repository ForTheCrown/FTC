package net.forthecrown.august.listener;

import io.papermc.paper.event.entity.EntityMoveEvent;
import net.forthecrown.august.AugustPlugin;
import net.forthecrown.august.EventConstants;
import net.forthecrown.august.EventUtil;
import net.forthecrown.august.event.AugustEntry;
import net.forthecrown.core.chat.Announcer;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.crownevents.InEventListener;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.CrownRandom;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import static net.forthecrown.august.EventConstants.*;

public class AugustInEventListener implements InEventListener, Listener {

    private final AugustEntry entry;
    private final Player player;
    private final Rabbit pinata;
    private final CrownRandom random;
    private int combo = 1;

    public AugustInEventListener(AugustEntry entry, Player player) {
        this.entry = entry;
        this.player = player;

        this.random = new CrownRandom();
        this.pinata = EventUtil.findPinata();
    }

    public boolean checkPlayer(Entity entity) { return player.equals(entity); }

    public boolean checkPinata(Entity entity) {
        return pinata.equals(entity);
    }

    public boolean checkBebe(Entity entity) { return entity.getPersistentDataContainer().has(BEBE_KEY, PersistentDataType.BYTE); }

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

    @EventHandler
    public void onPinataHit(EntityDamageByEntityEvent event) {
        if(!checkPlayer(event.getDamager())) return;
        if(!checkPinata(event.getEntity())) return;

        event.setCancelled(true);
        pinata.setHealth(EventConstants.SQUID_HEALTH);

        if(!Cooldown.contains(player)) {
            Cooldown.add(player, 1);
            entry.increment(combo);
            pinata.getWorld().playSound(pinata.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 1 + ((float) (combo * 0.1)));
            pinata.getWorld().spawnParticle(Particle.REDSTONE, pinata.getLocation(), 10, 0, 0, 0, EventConstants.dust);
        }

        EventUtil.spawnPlusX(pinata.getLocation().add(0, 1.25, 0), combo);
        if (combo < 10) combo++;
        else {
            pinataExplodeEffect(pinata.getLocation());
            explodeBabies(pinata.getLocation());
            entry.addSecToTimer(10);
            combo = 1;
        }

        Vector velocity = EventUtil.findRandomDirection(random);
        pinata.setVelocity(velocity);

        if(!Cooldown.contains(player, "Event_DropCooldown")) {
            Cooldown.add(player, "Event_DropCooldown", random.intInRange(20, 60));

            EventUtil.dropRandomLoot(pinata.getLocation(), random);
        }
    }

    @EventHandler
    public void onBebeHit(EntityDamageByEntityEvent event) {
        if(!checkPlayer(event.getDamager())) return;
        if(!checkBebe(event.getEntity())) return;

        Entity bebe = event.getEntity();

        EventUtil.spawnPlusOne(bebe.getLocation().add(0, 0.5, 0));
        bebe.getWorld().playSound(bebe.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 1.5f);
        bebe.getWorld().spawnParticle(Particle.REDSTONE, bebe.getLocation(), 5, 0, 0, 0, EventConstants.dust);
        bebe.remove();
    }

    private void pinataExplodeEffect(Location loc) {
        loc.getWorld().spawnParticle(Particle.TOTEM, loc, 20, 0, 0, 0, 0.2);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.25f, 1.5f);
        loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.5f);
    }

    private void explodeBabies(Location loc) {
        for(int i = 0; i < 7; i++) {
            loc.getWorld().spawn(loc, Rabbit.class, bebe -> {
                bebe.getPersistentDataContainer().set(BEBE_KEY, PersistentDataType.BYTE, (byte) 1);

                bebe.setBaby();
                bebe.setRabbitType(pinata.getRabbitType());

                bebe.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(SQUID_HEALTH);
                bebe.setHealth(SQUID_HEALTH);
                bebe.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
                bebe.setVelocity(new Vector(0, 0.35, 0));
            });
        }
    }


    @EventHandler
    public void onGround(EntityMoveEvent event) {
        if(combo <= 1) return;
        if(!checkPinata(event.getEntity())) return;
        if(pinata.isOnGround()) combo = 1;
    }
}

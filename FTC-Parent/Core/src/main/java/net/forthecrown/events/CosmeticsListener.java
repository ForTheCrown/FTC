package net.forthecrown.events;

import net.forthecrown.core.CrownCore;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CosmeticsListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) return;

        CrownUser user = UserManager.getUser(event.getEntity().getUniqueId());
        CosmeticData data = user.getCosmeticData();
        if(!data.hasActiveDeath()) return;

        Location loc = event.getEntity().getLocation();
        data.getActiveDeath().activate(loc);
    }

    @EventHandler
    public void onPlayerShootsBow(EntityShootBowEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) return;

        CrownUser user = UserManager.getUser(event.getEntity().getUniqueId());
        CosmeticData data = user.getCosmeticData();
        if(!data.hasActiveArrow()) return;

        new ArrowScheduler((Arrow) event.getProjectile(), data.getActiveArrow());
    }

    public static class ArrowScheduler implements Runnable {

        private final int id;
        private final Arrow arrow;
        private final ArrowEffect effect;

        public ArrowScheduler(Arrow arrow, ArrowEffect effect){
            this.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(CrownCore.inst(), this, 1, 1);

            this.arrow = arrow;
            this.effect = effect;
        }

        @Override
        public void run() {
            arrow.getWorld().spawnParticle(effect.getParticle(), arrow.getLocation(), 1, 0, 0, 0, effect.getSlot());

            if(arrow.isDead() || arrow.isOnGround()){
                Bukkit.getScheduler().cancelTask(id);
            }
        }
    }
}

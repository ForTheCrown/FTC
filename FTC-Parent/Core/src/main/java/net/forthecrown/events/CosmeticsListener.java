package net.forthecrown.events;

import com.destroystokyo.paper.ParticleBuilder;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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

        new ArrowScheduler((Arrow) event.getProjectile(), user.getPlayer(), data.getActiveArrow());
    }

    public static class ArrowScheduler implements Runnable {

        private final int id;
        private final Arrow arrow;
        private final ParticleBuilder builder;

        public ArrowScheduler(Arrow arrow, Player player, ArrowEffect effect){
            this.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(ForTheCrown.inst(), this, 1, 1);

            this.arrow = arrow;

            builder = new ParticleBuilder(effect.getParticle())
                    .location(arrow.getLocation())
                    .allPlayers()
                    .count(1).extra(0)
                    .source(player)
                    .spawn();
        }

        @Override
        public void run() {
            builder.location(arrow.getLocation())
                    .spawn();

            if(arrow.isDead() || arrow.isOnGround()){
                Bukkit.getScheduler().cancelTask(id);
            }
        }
    }
}

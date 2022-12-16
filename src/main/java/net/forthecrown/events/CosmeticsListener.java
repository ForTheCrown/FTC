package net.forthecrown.events;

import com.destroystokyo.paper.ParticleBuilder;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.CosmeticData;
import net.forthecrown.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class CosmeticsListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        User user = Users.get(event.getEntity().getUniqueId());
        CosmeticData data = user.getCosmeticData();

        if (data.isUnset(Cosmetics.DEATH)) {
            return;
        }

        Location loc = event.getEntity().getLocation();
        data.get(Cosmetics.DEATH).activate(loc);
    }

    @EventHandler
    public void onPlayerShootsBow(EntityShootBowEvent event) {
        if (event.getProjectile() instanceof Firework) {
            return;
        }

        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        User user = Users.get(event.getEntity().getUniqueId());
        CosmeticData data = user.getCosmeticData();

        if (data.isUnset(Cosmetics.ARROWS)) {
            return;
        }

        Tasks.runTimer(
                new ArrowScheduler(
                        (Arrow) event.getProjectile(),
                        user.getPlayer(),
                        data.get(Cosmetics.ARROWS)
                ),
                1, 1
        );
    }

    public static class ArrowScheduler implements Consumer<BukkitTask> {
        private final Arrow arrow;
        private final ParticleBuilder builder;

        public ArrowScheduler(Arrow arrow, Player player, ArrowEffect effect) {
            this.arrow = arrow;

            builder = new ParticleBuilder(effect.getParticle())
                    .location(arrow.getLocation())
                    .source(player)
                    .extra(0);
        }

        @Override
        public void accept(BukkitTask task) {
            builder.location(arrow.getLocation())
                    .spawn();

            if (arrow.isDead() || arrow.isOnGround()) {
                Tasks.cancel(task);
            }
        }
    }
}
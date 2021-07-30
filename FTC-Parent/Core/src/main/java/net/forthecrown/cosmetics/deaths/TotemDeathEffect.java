package net.forthecrown.cosmetics.deaths;

import net.forthecrown.core.ForTheCrown;
import org.bukkit.*;

public class TotemDeathEffect extends AbstractDeathEffect {
    TotemDeathEffect(){ super(12, "Faulty Totem", "The particles are there, but you still die?"); }

    @Override
    public void activate(Location loc) {
        double x = loc.getX();
        double y = loc.getY()+1;
        double z = loc.getZ();
        for (int i = 0; i < 20; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(ForTheCrown.inst(), () -> {
                for (int i1 = 0; i1 < 2; i1++) {
                    loc.getWorld().spawnParticle(Particle.TOTEM, x, y, z, 5, 0, 0, 0, 0.4);
                }
            }, i);
        }
        loc.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 1, 1);
    }
}

package net.forthecrown.cosmetics.deaths;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;

public class SoulDeathEffect extends DeathEffect {
    SoulDeathEffect(){
        super(11, "Souls",
                "Scary souls escaping your body"
        );
    }

    @Override
    public void activate(Location loc) {
        double x = loc.getX();
        double y = loc.getY()+1;
        double z = loc.getZ();
        loc.getWorld().playEffect(loc, Effect.ZOMBIE_INFECT, 1);

        for (int i = 0; i < 50; i++) {
            loc.getWorld().spawnParticle(Particle.SOUL, x, y+(((float) i)/50), z, 1, 0.5, 0, 0.5, 0.05);
        }
    }
}
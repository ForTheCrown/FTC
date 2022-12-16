package net.forthecrown.cosmetics.deaths;

import org.bukkit.Effect;
import org.bukkit.Location;

public class ExplosionDeathEffect extends DeathEffect {
    ExplosionDeathEffect() {
        super(15, "Creeper",
                "Always wanted to know what that feels like..."
        );
    }

    @Override
    public void activate(Location loc) {
        loc.getWorld().playEffect(loc, Effect.END_GATEWAY_SPAWN, 1);
    }
}
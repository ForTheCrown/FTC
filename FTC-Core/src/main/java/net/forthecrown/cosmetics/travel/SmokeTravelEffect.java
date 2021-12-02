package net.forthecrown.cosmetics.travel;

import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;

import static net.forthecrown.cosmetics.travel.TravelUtil.spawnOnCircle;

public class SmokeTravelEffect extends TravelEffect {
    SmokeTravelEffect() {
        super("Smoke", new InventoryPos(4, 1),
                Component.text("you are a smoker "),
                Component.text("really.")
        );
    }

    @Override
    public void onPoleTeleport(CrownUser user, Location from, Location pole) {
        // Little ball of smoke particles
        ninjaBomb(from);
        ninjaBomb(pole);
    }

    @Override
    public void onHulkStart(CrownUser user, Location loc) {
        // Cone shaped smoke
        spawnOnCircle(loc, 1.3, 0.1, (short)5, Particle.SMOKE_LARGE, 2);  // Inner circle
        spawnOnCircle(loc, 0.8, 0.25, (short)10, Particle.SMOKE_LARGE, 4);
        spawnOnCircle(loc, 0.6, 0.5, (short)10, Particle.SMOKE_LARGE, 4);
        spawnOnCircle(loc, 0.3, 1, (short)20, Particle.SMOKE_LARGE, 2);
        spawnOnCircle(loc, 0.1, 1.5, (short)20, Particle.SMOKE_LARGE, 1); // Outer circle

        // Extra explosion for good measure :D
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, 0.1, 0) , 1, 0, 0, 0, 0, null, true);
    }

    @Override
    public void onHulkTickDown(CrownUser user, Location loc) {
        // Smokes following user
        loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.add(0, 3, 0), 2, 0, 0.5, 0, 0.01, null, true);
    }

    @Override
    public void onHulkTickUp(CrownUser user, Location loc) {
        // Smokes following user
        loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.add(0, -1, 0), 3, 0, 0.5, 0, 0.01, null, true);
    }

    @Override
    public void onHulkLand(CrownUser user, Location landing) {
        // Little ball of smoke particles
        landing.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, landing.add(0, 0.1, 0), 20, 0.2D, 0, 0.2D, 0.01D,null,  true);
    }

    void ninjaBomb(Location loc) {
        loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.add(0, 0.1, 0), 100, 0.5D, 1, 0.5D, 0.01D, null, true);
    }
}

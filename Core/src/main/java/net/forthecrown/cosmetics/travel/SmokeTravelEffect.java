package net.forthecrown.cosmetics.travel;

import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;

public class SmokeTravelEffect extends TravelEffect {
    SmokeTravelEffect() {
        super("Smoke", new InventoryPos(3, 2),
                Component.text("you are a smoker "),
                Component.text("really.")
        );
    }

    @Override
    public void onPoleTeleport(CrownUser user, Location from, Location pole) {
        // Little ball of smoke particles
        from.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, from.add(0, 0.1, 0), 20, 0.2D, 0, 0.2D, 0.01D);
        pole.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pole.add(0, 0.1, 0), 20, 0.2D, 0, 0.2D, 0.01D);
    }

    @Override
    public void onHulkStart(Location loc) {
        // Cone shaped smoke
        spawnOnCircle(loc, 1.3, 0.1, 5, Particle.SMOKE_LARGE, 2);  // Inner circle
        spawnOnCircle(loc, 0.8, 0.25, 10, Particle.SMOKE_LARGE, 4);
        spawnOnCircle(loc, 0.6, 0.5, 10, Particle.SMOKE_LARGE, 4);
        spawnOnCircle(loc, 0.3, 1, 20, Particle.SMOKE_LARGE, 2);
        spawnOnCircle(loc, 0.1, 1.5, 20, Particle.SMOKE_LARGE, 1); // Outer circle

        // Extra explosion for good measure :D
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, 0.1, 0) , 1, 0, 0, 0, 0);
    }

    /**
     * Spawns particles on a circle around a given location.
     * @param loc The location of the centre of the circle.
     * @param extraY Extra y to add to loc (0 means no extra height).
     * @param radius The radius of the circle around loc
     * @param amountPoints The amount of points used on this circle, more means a more accurate circle.
     * @param particle The particle to spawn
     * @param amountParticlesPerPoint Amount of particles to spawn on a point
     */
    private void spawnOnCircle(Location loc, double extraY, double radius, int amountPoints, Particle particle, int amountParticlesPerPoint) {
        for (int i = 0; i < amountPoints; ++i) {
            final double angle = Math.toRadians(((double) i / amountPoints) * 360d);

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location pointLoc = new Location(loc.getWorld(), loc.getX() + x, loc.getY() + extraY, loc.getZ() + z);
            pointLoc.getWorld().spawnParticle(particle, pointLoc, amountParticlesPerPoint, 0, 0, 0, 0);
        }
    }

    @Override
    public void onHulkTick(Location loc) {
        // Smokes following user
        loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.add(0, 1.2, 0), 2, 0, 0, 0, 0);
    }

    @Override
    public void onHulkLand(Location landing) {
        // Little ball of smoke particles
        landing.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, landing.add(0, 0.1, 0), 20, 0.2D, 0, 0.2D, 0.01D);
    }
}

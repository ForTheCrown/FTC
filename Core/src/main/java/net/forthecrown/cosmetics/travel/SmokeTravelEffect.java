package net.forthecrown.cosmetics.travel;

import net.forthecrown.inventory.builder.InventoryPos;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;

public class SmokeTravelEffect extends TravelEffect {
    SmokeTravelEffect() {
        super("Smoke", new InventoryPos(4, 1),
                Component.text("you are a smoker "),
                Component.text("really.")
        );
    }

    @Override
    public void onPoleTeleport(Location from, Location pole) {
        // Little ball of smoke particles
        from.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, from.add(0, 0.1, 0), 20, 0.2D, 0, 0.2D, 0.01D);
        pole.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pole.add(0, 0.1, 0), 20, 0.2D, 0, 0.2D, 0.01D);
    }

    @Override
    public void onHulkStart(Location loc) {
        // Cone shaped smoke
        TravelUtil.spawnOnCircle(loc, 1.3, 0.1, (short)5, Particle.SMOKE_LARGE, 2);  // Inner circle
        TravelUtil.spawnOnCircle(loc, 0.8, 0.25, (short)10, Particle.SMOKE_LARGE, 4);
        TravelUtil.spawnOnCircle(loc, 0.6, 0.5, (short)10, Particle.SMOKE_LARGE, 4);
        TravelUtil.spawnOnCircle(loc, 0.3, 1, (short)20, Particle.SMOKE_LARGE, 2);
        TravelUtil.spawnOnCircle(loc, 0.1, 1.5, (short)20, Particle.SMOKE_LARGE, 1); // Outer circle

        // Extra explosion for good measure :D
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, 0.1, 0) , 1, 0, 0, 0, 0);
    }



    @Override
    public void onHulkTickDown(Location loc) {
        // Smokes following user
        loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.add(0, 2, 0), 2, 0, 0, 0, 0);
    }

    @Override
    public void onHulkTickUp(Location loc) {
        // Smokes following user
        loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 2, 0, 0, 0, 0);
    }

    @Override
    public void onHulkLand(Location landing) {
        // Little ball of smoke particles
        landing.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, landing.add(0, 0.1, 0), 20, 0.2D, 0, 0.2D, 0.01D);
    }
}

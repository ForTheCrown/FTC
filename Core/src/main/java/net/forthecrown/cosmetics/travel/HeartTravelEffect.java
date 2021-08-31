package net.forthecrown.cosmetics.travel;

import net.forthecrown.inventory.builder.InventoryPos;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.List;

public class HeartTravelEffect extends TravelEffect {

    // Amount of points used to spiral around player.
    private final short amountSpiralPoints = 16;

    // Points on a circle that will form a spiral around player.
    private List<Location> circleLocs;

    // Points to a location from circleLocs.
    private int pointer = 0;

    // Moves the pointer to the next location, so the tick methods can get next location.
    private int getNext() {
        pointer = (pointer + 1) % amountSpiralPoints;
        return pointer;
    }



    HeartTravelEffect() {
        super("Hearts", new InventoryPos(3, 2),
                Component.text("you are a lover "),
                Component.text("really.")
        );
    }

    @Override
    public void onPoleTeleport(Location from, Location pole) {
        util.spawnOnHearts(from, 0.2, Particle.END_ROD);
        util.spawnOnHearts(pole, 0.2, Particle.END_ROD);
    }

    @Override
    public void onHulkStart(Location loc) {
        // Prepare tick locations
        this.circleLocs = util.getOnCircle(loc.getWorld(), -2.5, 1.5, amountSpiralPoints);

        util.spawnOnHearts(loc, 0.2, Particle.END_ROD);
    }

    @Override
    public void onHulkTickDown(Location loc) {
        Location spawnLoc = loc.add(this.circleLocs.get(getNext()));
        spawnLoc.getWorld().spawnParticle(Particle.HEART, spawnLoc, 2, 0.1, 0.2, 0.1, 0);
        spawnLoc.getWorld().spawnParticle(Particle.END_ROD, spawnLoc, 1, 0.1, 0.2, 0.1, 0);
    }

    @Override
    public void onHulkTickUp(Location loc) {
        Location spawnLoc = loc.add(this.circleLocs.get(getNext()));
        spawnLoc.getWorld().spawnParticle(Particle.HEART, spawnLoc, 2, 0.1, 0.2, 0.1, 0);
        spawnLoc.getWorld().spawnParticle(Particle.END_ROD, spawnLoc, 1, 0.1, 0.2, 0.1, 0);
    }

    @Override
    public void onHulkLand(Location landing) {
        util.spawnOnHearts(landing, 0.2, Particle.END_ROD);
    }
}

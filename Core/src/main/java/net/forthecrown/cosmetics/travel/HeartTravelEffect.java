package net.forthecrown.cosmetics.travel;

import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.List;

public class HeartTravelEffect extends TravelEffect {

    // Amount of points used to spiral around player.
    private final byte amountSpiralPoints = 16;

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
        super("Hearts", new InventoryPos(2, 1),
                Component.text("you are a lover "),
                Component.text("really.")
        );
    }

    @Override
    public void onPoleTeleport(CrownUser user, Location from, Location pole) {
        TravelUtil.spawnOnHearts(from, 0.2, Particle.END_ROD);
        TravelUtil.spawnOnHearts(pole, -1, Particle.END_ROD);
    }

    @Override
    public void onHulkStart(CrownUser user, Location loc) {
        // Prepare tick locations
        circleLocs = TravelUtil.getOnCircle(loc.getWorld(), -2.5, 1.5, amountSpiralPoints);

        TravelUtil.spawnOnHearts(loc, 0.2, Particle.END_ROD);
    }

    @Override
    public void onHulkTickDown(CrownUser user, Location loc) {
        Location spawnLoc = loc.add(circleLocs.get(getNext()));
        spawnLoc.getWorld().spawnParticle(Particle.HEART, spawnLoc, 2, 0.1, 0.2, 0.1, 0, null, true);
        spawnLoc.getWorld().spawnParticle(Particle.END_ROD, spawnLoc, 1, 0.1, 0.2, 0.1, 0, null, true);
    }

    @Override
    public void onHulkTickUp(CrownUser user, Location loc) {
        Location spawnLoc = loc.add(circleLocs.get(getNext()));
        spawnLoc.getWorld().spawnParticle(Particle.HEART, spawnLoc, 2, 0.1, 0.2, 0.1, 0, null, true);
        spawnLoc.getWorld().spawnParticle(Particle.END_ROD, spawnLoc, 1, 0.1, 0.2, 0.1, 0, null, true);
    }

    @Override
    public void onHulkLand(CrownUser user, Location landing) {
        TravelUtil.spawnOnHearts(landing, 0.2, Particle.END_ROD);
    }
}

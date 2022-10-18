package net.forthecrown.cosmetics.travel;

import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;

public class HeartTravelEffect extends TravelEffect {

    // Amount of points used to spiral around player.
    private final byte amountSpiralPoints = 16;

    // Points on a circle that will form a spiral around player.
    private List<Vector3d> circleLocs;

    // Points to a location from circleLocs.
    private int pointer = 0;

    // Moves the pointer to the next location, so the tick methods can get next location.
    private int getNext() {
        pointer = (pointer + 1) % amountSpiralPoints;
        return pointer;
    }

    HeartTravelEffect() {
        super("Hearts", Slot.of(2, 1),
                Component.text("Fly in a blaze of love ")
        );
    }

    @Override
    public void onPoleTeleport(User user, Location from, Location pole) {
        TravelUtil.spawn4Hearts(from, 0.2, Particle.END_ROD);
        TravelUtil.spawn4Hearts(pole, -1, Particle.END_ROD);
    }

    @Override
    public void onHulkStart(User user, Location loc) {
        // Prepare tick locations
        circleLocs = TravelUtil.getCirclePoints(-2.5, 1.5, amountSpiralPoints);

        TravelUtil.spawn4Hearts(loc, 0.2, Particle.END_ROD);
    }

    @Override
    public void onHulkTickDown(User user, Location loc) {
        var next = circleLocs.get(getNext());
        Location spawnLoc = loc.add(next.x(), next.y(), next.z());

        spawnLoc.getWorld().spawnParticle(Particle.HEART, spawnLoc, 2, 0.1, 0.2, 0.1, 0, null, true);
        spawnLoc.getWorld().spawnParticle(Particle.END_ROD, spawnLoc, 1, 0.1, 0.2, 0.1, 0, null, true);
    }

    @Override
    public void onHulkTickUp(User user, Location loc) {
        var next = circleLocs.get(getNext());
        Location spawnLoc = loc.add(next.x(), next.y(), next.z());

        spawnLoc.getWorld().spawnParticle(Particle.HEART, spawnLoc, 2, 0.1, 0.2, 0.1, 0, null, true);
        spawnLoc.getWorld().spawnParticle(Particle.END_ROD, spawnLoc, 1, 0.1, 0.2, 0.1, 0, null, true);
    }

    @Override
    public void onHulkLand(User user, Location landing) {
        TravelUtil.spawn4Hearts(landing, 0.2, Particle.END_ROD);
    }
}
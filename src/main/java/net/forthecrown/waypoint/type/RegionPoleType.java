package net.forthecrown.waypoint.type;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.WaypointProperties;
import net.forthecrown.waypoint.Waypoints;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

public class RegionPoleType extends PlayerWaypointType {
    public RegionPoleType() {
        super("Region Pole", Waypoints.REGION_POLE_COLUMN);
    }

    @Override
    public void onPreMove(Waypoint waypoint,
                          Vector3i newPosition,
                          World newWorld
    ) {
        clearPole(waypoint);
    }

    @Override
    public void onDelete(Waypoint waypoint) {
        clearPole(waypoint);
    }

    private void clearPole(Waypoint waypoint) {
        if (!waypoint.get(WaypointProperties.INVULNERABLE)) {
            return;
        }

        var oldBounds = waypoint.getBounds()
                .toWorldBounds(waypoint.getWorld());

        for (var b: oldBounds) {
            b.setType(Material.AIR, false);
        }
    }

    @Override
    public @NotNull Bounds3i createBounds() {
        var halfSize = Waypoints.poleSize()
                .div(2, 1, 2);

        return Bounds3i.of(
                halfSize.negate().withY(0),
                halfSize
        );
    }

    @Override
    public void onPostMove(Waypoint waypoint) {
        if (!waypoint.get(WaypointProperties.INVULNERABLE)) {
            return;
        }

        Waypoints.placePole(waypoint);
    }

    @Override
    public Optional<CommandSyntaxException> isValid(Waypoint waypoint) {
        return Waypoints.isValidWaypointArea(
                waypoint.getPosition()
                        .add(0, 1, 0),
                this,
                waypoint.getWorld(),
                false
        );
    }

    @Override
    public Vector3d getVisitPosition(Waypoint waypoint) {
        return super.getVisitPosition(waypoint)
                .add(0, 1, 0);
    }

    @Override
    public boolean isDestroyed(Waypoint waypoint) {
        return isDestroyed(
                waypoint.getPosition().add(0, 1, 0),
                waypoint.getWorld()
        );
    }
}
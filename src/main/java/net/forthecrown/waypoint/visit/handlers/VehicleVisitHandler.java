package net.forthecrown.waypoint.visit.handlers;

import net.forthecrown.utils.Tasks;
import net.forthecrown.waypoint.visit.RidingNode;
import net.forthecrown.waypoint.visit.VisitHandler;
import net.forthecrown.waypoint.visit.WaypointVisit;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Entity;

public class VehicleVisitHandler implements VisitHandler {
    private RidingNode rootNode;

    @Override
    public void onStart(WaypointVisit visit) {
        var root = findRootEntity(visit.getUser().getPlayer());
        rootNode = RidingNode.create(root);

        if (ArrayUtils.isEmpty(rootNode.getPassengers())) {
            rootNode = null;
            return;
        }

        // Add each entity to ignored list of the owned entity handler
        // so it doesn't teleport them separately
        visit.modifyHandler(OwnedEntityHandler.class, handler -> {
            rootNode.forEach(entity -> handler.ignored.add(entity.getUniqueId()));
        });

        visit.setHulkSmashSafe(false);
    }

    @Override
    public void onTeleport(WaypointVisit visit) {
        if (rootNode != null) {
            // Root may be a vehicle or other type of entity
            // not necessarily the player visiting
            rootNode.getEntity().teleport(visit.getTeleportLocation());

            // Wait 2 ticks before forcing remount
            Tasks.runLater(() -> {
                rootNode.remount(null);
            }, 2);
        }
    }

    private Entity findRootEntity(Entity entity) {
        var root = entity;

        while (root.getVehicle() != null) {
            root = root.getVehicle();
        }

        return root;
    }
}
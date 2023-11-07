package net.forthecrown.waypoints.visit;

import static net.forthecrown.waypoints.visit.OwnedEntityHandler.tpDelayed;

import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class VehicleVisitHandler implements VisitHandler {

  private Entity teleportEntity;

  @Override
  public void onStart(WaypointVisit visit) {
    Player player = visit.getUser().getPlayer();
    Entity root = findRootEntity(visit.getUser().getPlayer());

    if (root.equals(player)) {
      return;
    }

    visit.setHulkSmashSafe(false);
    visit.setCancelTeleport(true);

    teleportEntity = root;
  }

  @Override
  public void onTeleport(WaypointVisit visit) {
    if (teleportEntity == null) {
      return;
    }

    tpDelayed(List.of(teleportEntity), visit.getTeleportLocation());
  }

  private Entity findRootEntity(Entity entity) {
    var root = entity;

    while (root.getVehicle() != null) {
      root = root.getVehicle();
    }

    return root;
  }
}
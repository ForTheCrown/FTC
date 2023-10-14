package net.forthecrown.waypoints;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

import java.util.UUID;
import net.forthecrown.packet.SignRenderer;
import net.forthecrown.user.Users;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.waypoints.util.UuidPersistentDataType;
import org.bukkit.DyeColor;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;

public class WaypointSignRenderer implements SignRenderer {

  private final WaypointManager manager;

  public WaypointSignRenderer(WaypointManager manager) {
    this.manager = manager;
  }

  @Override
  public boolean test(Player player, WorldVec3i pos, Sign sign) {
    var pdc = sign.getPersistentDataContainer();
    return pdc.has(Waypoints.EDIT_WAYPOINT_KEY);
  }

  @Override
  public void render(Player player, WorldVec3i pos, Sign sign) {
    PersistentDataContainer pdc = sign.getPersistentDataContainer();
    UUID id = pdc.get(Waypoints.EDIT_WAYPOINT_KEY, UuidPersistentDataType.INSTANCE);
    Waypoint waypoint = manager.get(id);

    if (waypoint == null) {
      return;
    }

    SignSide front = sign.getSide(Side.FRONT);
    front.setGlowingText(true);
    front.setColor(DyeColor.GRAY);

    front.line(0, empty());
    front.line(1, text("Right-Click to"));
    front.line(3, empty());

    if (waypoint.canEdit(Users.get(player))) {
      front.line(2, text("edit waypoint"));
    } else {
      front.line(2, text("view waypoint list"));
    }
  }
}

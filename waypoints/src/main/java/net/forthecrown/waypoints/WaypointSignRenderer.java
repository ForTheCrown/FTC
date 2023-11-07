package net.forthecrown.waypoints;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

import java.util.Optional;
import net.forthecrown.packet.SignRenderer;
import net.forthecrown.user.Users;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.DyeColor;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;

public class WaypointSignRenderer implements SignRenderer {

  private final WaypointManager manager;

  public WaypointSignRenderer(WaypointManager manager) {
    this.manager = manager;
  }

  private Optional<Waypoint> fromEditSign(WorldVec3i pos) {
    var entries = manager.getChunkMap().get(pos);
    if (entries.isEmpty()) {
      return Optional.empty();
    }

    for (Waypoint entry : entries) {
      var signPos = entry.getEditSignPosition();

      if (signPos == null) {
        continue;
      }

      if (signPos.x() == pos.x() && signPos.y() == pos.y() && signPos.z() == pos.z()) {
        return Optional.of(entry);
      }
    }

    return Optional.empty();
  }

  @Override
  public boolean test(Player player, WorldVec3i pos, Sign sign) {
    return fromEditSign(pos).isPresent();
  }

  @Override
  public void render(Player player, WorldVec3i pos, Sign sign) {
    var opt = fromEditSign(pos);

    if (opt.isEmpty()) {
      return;
    }

    var waypoint = opt.get();

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

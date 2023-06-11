package net.forthecrown.dungeons.level;

import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Events;
import net.forthecrown.dungeons.DungeonWorld;
import net.forthecrown.dungeons.level.room.RoomPiece;
import net.forthecrown.utils.math.Bounds;
import net.forthecrown.utils.math.Bounds3i;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@Getter
@RequiredArgsConstructor
public class LevelListener implements Listener {

  private final DungeonLevel level;
  boolean registered;

  boolean inLevel(Location loc) {
    return loc.getWorld().equals(DungeonWorld.get())
        && level.getChunkMap().getTotalArea().contains(loc);
  }

  void register() {
    if (registered) {
      return;
    }

    Events.register(this);
    registered = true;
  }

  void unregister() {
    if (!registered) {
      return;
    }

    Events.unregister(this);
    registered = false;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @EventHandler(ignoreCancelled = true)
  public void onPlayerMove(PlayerMoveEvent event) {
    if (!inLevel(event.getFrom()) || event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
      return;
    }

    Player player = event.getPlayer();

    Bounds3i origin = Bounds.playerBounds(player, event.getFrom());
    Bounds3i destination = Bounds.playerBounds(player, event.getTo());
    Bounds3i totalArea = origin.combine(destination);

    Set allPieces = level.getIntersecting(totalArea);
    allPieces.removeIf(piece -> !(piece instanceof RoomPiece));
    var pieces = (Set<RoomPiece>) allPieces;

    if (pieces.isEmpty()) {
      return;
    }

    var user = event.getPlayer();

    for (var p : pieces) {
      var bounds = p.getBounds();

      boolean originInside = bounds.overlaps(origin);
      boolean destInside = bounds.overlaps(destination);

      // If did not leave or enter the room
      if (originInside == destInside) {
        continue;
      }

      // If exiting
      // Because of the above check, the two booleans
      // must have an opposite state
      if (originInside) {
        p.getPlayers().remove(user);

        // If room is now empty
        if (p.getPlayers().isEmpty()) {
          level.getActivePieces().remove(p);
          level.getInactivePieces().add(p);
        }

        p.onExit(user, level);
      } else {
        level.getActivePieces().add(p);
        level.getInactivePieces().remove(p);

        p.onEnter(user, level);
      }
    }
  }
}
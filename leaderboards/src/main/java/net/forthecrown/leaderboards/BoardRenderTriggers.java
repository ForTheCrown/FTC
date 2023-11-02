package net.forthecrown.leaderboards;

import net.forthecrown.Loggers;
import net.forthecrown.packet.PacketListeners;
import net.forthecrown.utils.collision.CollisionListener;
import net.forthecrown.utils.collision.CollisionSystem;
import net.forthecrown.utils.collision.CollisionSystems;
import net.forthecrown.utils.collision.WorldChunkMap;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.vector.Vector3i;

public class BoardRenderTriggers {

  private static final Logger LOGGER = Loggers.getLogger();

  private final LeaderboardPlugin plugin;

  private final WorldChunkMap<BoardImpl> chunkMap;
  private final CollisionSystem<Player, BoardImpl> system;

  public BoardRenderTriggers(LeaderboardPlugin plugin) {
    this.plugin = plugin;

    this.chunkMap = new WorldChunkMap<>();
    this.system = CollisionSystems.createSystem(chunkMap, new BoardCollisionListener());
  }

  public void activate() {
    system.beginListening();
  }

  public void close() {
    system.stopListening();
  }

  private Bounds3i createBounds() {
    int radius = plugin.getBoardsConfig().renderRadius();
    radius = GenericMath.clamp(radius, 1, 100);
    return Bounds3i.of(Vector3i.ZERO, radius);
  }

  public void onAdded(BoardImpl board) {
    var loc = board.location;
    if (loc == null) {
      return;
    }

    Bounds3i bounds = createBounds().move(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    chunkMap.add(loc.getWorld(), bounds, board);
  }

  public void onRemoved(BoardImpl board) {
    var loc = board.location;

    if (loc == null) {
      return;
    }

    var world = loc.getWorld();
    if (world == null) {
      return;
    }

    chunkMap.remove(world, board);
  }

  public void onLocationSet(BoardImpl board, Location newLocation) {
    Location loc = board.getLocation();
    if (loc != null) {
      chunkMap.remove(loc.getWorld(), board);
    }

    if (newLocation == null) {
      return;
    }

    var bounds = createBounds()
        .move(newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ());

    chunkMap.add(newLocation.getWorld(), bounds, board);
  }

  public void clear() {
    chunkMap.clear();
  }

  public void updateFor(Player player) {
    var overlapping = chunkMap.getOverlapping(
        player.getWorld(), Bounds3i.of(player.getBoundingBox())
    );

    if (overlapping.isEmpty()) {
      return;
    }

    for (BoardImpl board : overlapping) {
      system.getListener().onEnter(player, board);
    }
  }

  public void onUpdate(BoardImpl board) {
    var loc = board.getLocation();
    var opt = board.getDisplay();

    if (loc == null || opt.isEmpty()) {
      return;
    }

    var bounds = createBounds()
        .move(Vectors.intFrom(loc))
        .toWorldBounds(loc.getWorld());

    for (Player player : bounds.getPlayers()) {
      system.getListener().onEnter(player, board);
    }
  }

  private class BoardCollisionListener implements CollisionListener<Player, BoardImpl> {

    PacketListeners packets;

    public BoardCollisionListener() {
      this.packets = PacketListeners.listeners();
    }

    @Override
    public void onEnter(Player source, BoardImpl board) {
      board.getDisplay().ifPresent(display -> {
        Component renderedText = board.renderText(source);
        packets.setEntityDisplay(display, source, renderedText);
      });
    }

    @Override
    public void onExit(Player source, BoardImpl board) {
      board.getDisplay().ifPresent(display -> {
        packets.setEntityDisplay(display, source, null);
      });
    }

    @Override
    public void onMoveInside(Player source, BoardImpl board) {
      // No op
    }
  }
}

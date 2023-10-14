package net.forthecrown.waypoints.listeners;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.forthecrown.Loggers;
import net.forthecrown.command.Exceptions;
import net.forthecrown.text.Text;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.Waypoints;
import net.forthecrown.waypoints.type.WaypointTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;

public class PlayerListener implements Listener {

  private static final Logger LOGGER = Loggers.getLogger();

  public static final Map<UUID, MovingWaypoint> copyingWaypoint = new HashMap<>();

  @EventHandler(ignoreCancelled = true)
  public void onPlayerQuit(PlayerQuitEvent event) {
    copyingWaypoint.remove(event.getPlayer().getUniqueId());
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    var player = event.getPlayer();
    var item = event.getItem();

    if (ItemStacks.isEmpty(item) || !event.getAction().isRightClick()) {
      return;
    }

    if (!ItemStacks.hasTagElement(item.getItemMeta(), "waypoint_tool")) {
      return;
    }

    var block = event.getClickedBlock();
    MovingWaypoint copySource = copyingWaypoint.get(player.getUniqueId());

    WaypointManager manager = WaypointManager.getInstance();
    Waypoint created;
    Waypoint copy;

    event.setCancelled(true);

    if (copySource != null) {
      UUID moveId = copySource.waypointId;
      copy = manager.get(moveId);

      if (copy == null) {
        LOGGER.error("Cannot copy from waypoint {}, it no longer exists", moveId);

        player.sendMessage(
            Component.text("Internal error finding relocation waypoint! (Was it removed?)",
                NamedTextColor.RED
            )
        );

        return;
      }

      var target = WaypointTypes.findTopAndType(block);
      if (target != null) {
        var targetType = target.getSecond();

        if (!Objects.equals(targetType, copy.getType())) {
          player.sendMessage(
              Text.format("Cannot move {0} waypoint to {1} waypoint",
                  NamedTextColor.RED,
                  copy.getType().getDisplayName(),
                  targetType.getDisplayName()
              )
          );
          return;
        }
      }
    } else {
      copy = null;
    }

    try {
      created = Waypoints.tryCreate(player, block, copy != null);
    } catch (CommandSyntaxException exc) {
      Exceptions.handleSyntaxException(player, exc);
      return;
    }

    copyingWaypoint.remove(player.getUniqueId());

    if (copySource != null) {
      copySource.cancel();
    }

    if (copy != null) {
      created.copyFrom(copy);
      manager.removeWaypoint(copy);
    }
  }

  public record MovingWaypoint(UUID waypointId, BukkitTask timeout) {

    void cancel() {
      Tasks.cancel(timeout);
    }
  }
}

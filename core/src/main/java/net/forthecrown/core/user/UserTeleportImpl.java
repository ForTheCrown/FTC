package net.forthecrown.core.user;

import io.papermc.paper.entity.TeleportFlag.EntityState;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.commands.tpa.TpMessages;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.utils.Tasks;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Getter @Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class UserTeleportImpl implements UserTeleport {

  private final UserImpl user;
  private final Supplier<Location> destination;
  private final Type type;

  private Duration delay;

  private boolean async = true;
  private boolean setReturn = true;
  private boolean silent = false;

  private Component startMessage;
  private Component completeMessage;
  private Component interruptMessage;

  private BukkitTask task;

  public void start() {
    if (!isDelayed()) {
      complete();
      return;
    }

    // Send start message if we're not on silent lol
    if (!silent) {
      user.sendMessage(Objects.requireNonNullElse(
          startMessage,
          TpMessages.teleportStart(delay, type)
      ));
    }

    task = Tasks.runLater(this::complete, delay);
  }

  public void stop() {
    task = Tasks.cancel(task);
  }

  public void interrupt() {
    if (!silent) {
      user.sendMessage(Objects.requireNonNullElse(
          interruptMessage,
          TpMessages.TELEPORT_CANCELLED
      ));
    }

    stop();
    user.currentTeleport = null;
  }

  public void complete() {
    if (!silent) {
      user.sendMessage(Objects.requireNonNullElse(
          completeMessage,
          TpMessages.teleportComplete(type)
      ));
    }

    Location location = user.getLocation();
    user.onTpComplete();

    // Get the destination without it throwing errors
    Location dest = getDestinationSafe(destination);

    if (dest != null) {
      Player player = user.getPlayer();

      // If we should use async tp for players and we've selected
      // to use async for this, then TP async, else in sync
      if (async) {
        player.teleportAsync(dest);
      } else {
        // Ignore passengers and dismount the player if they're riding
        // another entity
        player.teleport(dest, EntityState.RETAIN_PASSENGERS);
      }
      user.playSound(Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

      if (setReturn) {
        user.setReturnLocation(location);
      }
    } else {
      // Tell user something went wrong, just in case
      user.sendMessage(TpMessages.TELEPORT_ERROR);
    }
  }

  /**
   * Gets a destination location from the given supplier or null, if it happens to throw a null
   * pointer exception
   *
   * @param supplier The location supplier
   * @return The gotten value, or null
   */
  private static Location getDestinationSafe(Supplier<Location> supplier) {
    try {
      return supplier.get();
    } catch (NullPointerException e) {
      return null;
    }
  }
}

package net.forthecrown.user;

import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

public interface UserTeleport {

  /**
   * Starts the teleportation.
   * <p>
   * If {@link #isDelayed()} == false, It will instantly skip to {@link #complete()}.
   */
  void start();

  /**
   * Stops this teleport
   */
  void stop();

  /**
   * Tells the player their teleport was interrupted, if {@link #isSilent()} == false and stops this
   * teleport by calling {@link #stop()}
   */
  void interrupt();

  /**
   * Completes this teleport by sending the user a teleport message and then teleporting the
   * player.
   */
  void complete();

  User getUser();

  Supplier<Location> getDestination();

  boolean isDelayed();

  Type getType();

  boolean isAsync();

  boolean isSetReturn();

  boolean isSilent();

  Component getStartMessage();

  Component getCompleteMessage();

  Component getInterruptMessage();

  UserTeleport setDelayed(boolean delayed);

  UserTeleport setAsync(boolean async);

  UserTeleport setSetReturn(boolean setReturn);

  UserTeleport setSilent(boolean silent);

  UserTeleport setStartMessage(Component startMessage);

  UserTeleport setCompleteMessage(Component completeMessage);

  UserTeleport setInterruptMessage(Component interruptMessage);

  /**
   * Represents a teleport's type.
   * <p>
   * This is really only used to provide the teleport instance with an action message, if one is not
   * given
   */
  @Getter
  @RequiredArgsConstructor
  enum Type {

    WARP("Warping"),
    TELEPORT("Teleporting"),
    TPA("Teleporting"),
    BACK("Returning"),
    HOME("Teleporting"),
    OTHER("Teleporting");

    /**
     * The action this type performs, eg: "Warping"
     */
    private final String action;
  }
}
package net.forthecrown.user;

import java.time.Duration;
import java.util.function.Supplier;
import javax.annotation.Nullable;
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

  /**
   * Gets the user being teleported
   * @return User being teleported
   */
  User getUser();

  /**
   * Gets the teleport's destination supplier.
   * <p>
   * This is a supplier because often times when someone requests a TPA/Tpa here, they move around
   * and a supplier makes sure that the destination returned is always that of the player, and not
   * where the teleport began.
   *
   * @return Teleport destination supplier
   */
  Supplier<Location> getDestination();

  /**
   * Determines if the teleport's delay should be bypassed or not
   */
  default boolean isDelayed() {
    return getDelay() != null && !getDelay().isZero();
  }

  /**
   * Gets the teleportation delay
   * <p>
   * This is the delay between {@link #start()} being called and the teleport being completed
   *
   * @return Teleport delay
   */
  Duration getDelay();

  /**
   * The teleport's type
   */
  Type getType();

  /**
   * Determines if the teleport should occurr in async, note that a config option in the core plugin
   * is also checked before an async teleport is initiated
   */
  boolean isAsync();

  /**
   * Determines if when the teleport finishes, if the teleporting user should have their last
   * location set to the teleport destination
   */
  boolean isSetReturn();

  /**
   * Determines if this teleport instance is silent, meaning it will not send any messages
   */
  boolean isSilent();

  Component getStartMessage();

  Component getCompleteMessage();

  Component getInterruptMessage();

  UserTeleport setAsync(boolean async);

  UserTeleport setSetReturn(boolean setReturn);

  UserTeleport setDelay(@Nullable Duration delay);

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
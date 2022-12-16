package net.forthecrown.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.core.Messages;
import net.forthecrown.utils.Tasks;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;
import java.util.function.Supplier;

import static net.forthecrown.core.Messages.TELEPORT_CANCELLED;
import static net.forthecrown.core.Messages.TELEPORT_ERROR;

/**
 * A user's teleport from one area to another.
 *
 */
@Accessors(chain = true)
@RequiredArgsConstructor
public class UserTeleport {

    /**
     * The user being teleported
     */
    @Getter
    private final User user;

    /**
     * The teleport's destination supplier.
     * <p>
     * This is a supplier because often times when
     * someone requests a TPA/Tpa here, they move around
     * and a supplier makes sure that the destination
     * returned is always that of the player, and not
     * where the teleport began.
     */
    @Getter
    private final Supplier<Location> destination;

    /**
     * Determines if the teleports 3-second delay
     * should be bypassed or not
     */
    @Getter @Setter
    private boolean delayed;

    /**
     * The teleport's type
     */
    @Getter
    private final Type type;

    /**
     * Determines if the teleport should occurr in async,
     * note that {@link GeneralConfig#useAsyncTpForPlayers} is
     * also checked before an async teleport is initiated
     */
    @Getter @Setter
    private boolean async = true;

    /**
     * Determines if when the teleport finishes,
     * if the teleporting user should have their
     * last location set to the teleport destination
     */
    @Getter @Setter
    private boolean setReturn = true;

    /**
     * Determines if this teleport instance is
     * silent, meaning it will not send any
     * messages
     */
    @Getter @Setter
    private boolean silent = false;

    /**
     * The message to display to the user when
     * the teleportation starts
     */
    @Getter @Setter
    private Component startMessage;

    /**
     * The message to display to the user when
     * the teleportation is completed
     */
    @Getter @Setter
    private Component completeMessage;

    /**
     * The message to display to the user when
     * the teleport is interrupted
     */
    @Getter @Setter
    private Component interruptMessage;

    private BukkitTask task;

    /**
     * Starts the teleportation.
     * <p>
     * If {@link #isDelayed()} == false, It will instantly skip to {@link #complete()}.
     */
    public void start() {
        if (!delayed) {
            complete();
            return;
        }

        // Send start message if we're not on silent lol
        if (!silent) {
            user.sendMessage(Objects.requireNonNullElse(
                    startMessage,
                    Messages.teleportStart(GeneralConfig.tpTickDelay * 50L, type)
            ));
        }

        task = Tasks.runLater(this::complete, GeneralConfig.tpTickDelay);
    }

    /**
     * Stops this teleport
     */
    public void stop() {
        task = Tasks.cancel(task);
        user.lastTeleport = null;
    }

    /**
     * Tells the player their teleport was interrupted, if {@link #isSilent()} == false
     * and stops this teleport by calling {@link #stop()}
     */
    public void interrupt() {
        if (!silent) {
            user.sendMessage(Objects.requireNonNullElse(
                    interruptMessage,
                    TELEPORT_CANCELLED
            ));
        }

        stop();
    }

    /**
     * Completes this teleport by sending the user a teleport
     * message and then teleporting the player.
     */
    public void complete() {
        if (!silent) {
            user.sendMessage(Objects.requireNonNullElse(
                    completeMessage,
                    Messages.teleportComplete(type)
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
            if (GeneralConfig.useAsyncTpForPlayers && async) {
                player.teleportAsync(dest);
            } else {
                player.teleport(dest);
            }
            user.playSound(Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

            if (setReturn) {
                user.setReturnLocation(location);
            }
        } else {
            // Tell user something went wrong, just in case
            user.sendMessage(TELEPORT_ERROR);
        }

        stop();
    }

    /**
     * Gets a destination location from the given supplier or
     * null, if it happens to throw a null pointer exception
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

    /**
     * Represents a teleport's type.
     * <p>
     * This is really only used to provide the
     * teleport instance with an action message,
     * if one is not given
     */
    @Getter
    @RequiredArgsConstructor
    public enum Type {
        WARP ("Warping"),
        TELEPORT ("Teleporting"),
        TPA ("Teleporting"),
        BACK ("Returning"),
        HOME ("Teleporting"),
        OTHER ("Teleporting");

        /** The action this type performs, eg: "Warping" */
        private final String action;
    }
}
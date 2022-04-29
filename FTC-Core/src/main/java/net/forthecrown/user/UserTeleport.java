package net.forthecrown.user;

import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.chat.TimePrinter;
import net.forthecrown.events.dynamic.AsyncTeleportListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A user's teleport from one area to another.
 *
 */
public class UserTeleport {

    private final FtcUser user;
    private final Supplier<Location> destination;
    private final boolean noCooldown;
    private final Type type;

    private AsyncTeleportListener listener;
    private boolean async = true;
    private boolean setReturn = true;

    private Component startMessage;
    private Component completeMessage;
    private Component interruptMessage;

    private BukkitTask task;

    public UserTeleport(FtcUser user, Supplier<Location> destination, boolean noCooldown, Type type) {
        this.user = user;
        this.destination = destination;
        this.noCooldown = noCooldown;
        this.type = type;
    }

    public void start(boolean tell){
        if (noCooldown) {
            complete(tell);
        } else {
            if(tell) {
                user.sendMessage(Objects.requireNonNullElseGet(startMessage, () ->
                        Component.text(type.action + " in ")
                                .color(NamedTextColor.GRAY)
                                .append(new TimePrinter(FtcVars.tpTickDelay.get() * 50)
                                        .print()
                                        .color(NamedTextColor.GOLD)
                                )
                                .append(Component.newline())
                                .append(Component.text("Don't move!"))));
            }

            listener = new AsyncTeleportListener(user, this);
            task = Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> complete(tell), FtcVars.tpTickDelay.get());
        }
    }

    public void stop(){
        if (isScheduled()) {
            task.cancel();
            task = null;
        }

        user.lastTeleport = null;
        if (listener != null) HandlerList.unregisterAll(listener);
    }

    public CrownUser getUser() {
        return user;
    }

    public Supplier<Location> getDestination() {
        return destination;
    }

    public void interrupt(boolean tell){
        if(tell) user.sendMessage(Objects.requireNonNullElseGet(interruptMessage, () ->
                Component.text("Teleport cancelled").color(NamedTextColor.GRAY)
        ));
        stop();
    }

    public void complete(boolean tell) {
        if(tell) user.sendMessage(Objects.requireNonNullElseGet(completeMessage, () ->
                Component.text(type.action + "...").color(NamedTextColor.GRAY)
        ));

        Location location = user.getLocation();
        user.onTpComplete();

        try { //It'll probably throw an exception when the player logs out and the supplier can't get the loc
            Location dest = destination.get();
            Player player = user.getPlayer();

            if(FtcVars.useAsyncTpForPlayers.get() && async) player.teleportAsync(dest);
            else player.teleport(dest);

            if(setReturn) user.setLastLocation(location);
        } catch (NullPointerException ignored){ }

        stop();
    }

    public boolean isScheduled(){
        return task != null && !task.isCancelled();
    }

    public boolean shouldBypassCooldown() {
        return noCooldown;
    }

    public Component getCompleteMessage() {
        return completeMessage;
    }

    public Component getStartMessage() {
        return startMessage;
    }

    public Component getInterruptMessage() {
        return interruptMessage;
    }

    public Type getType() {
        return type;
    }

    public UserTeleport setStartMessage(Component startMessage) {
        this.startMessage = startMessage;
        return this;
    }

    public UserTeleport setCompleteMessage(Component completeMessage) {
        this.completeMessage = completeMessage;
        return this;
    }

    public UserTeleport setInterruptMessage(Component interruptMessage) {
        this.interruptMessage = interruptMessage;
        return this;
    }

    public boolean isAsync() {
        return async;
    }

    public UserTeleport setAsync(boolean async) {
        this.async = async;
        return this;
    }

    public boolean setReturn() {
        return setReturn;
    }

    public UserTeleport setSetReturn(boolean setReturn) {
        this.setReturn = setReturn;
        return this;
    }

    public enum Type {
        WARP ("Warping"),
        TELEPORT ("Teleporting"),
        TPA ("Teleporting"),
        BACK ("Returning"),
        HOME ("Teleporting"),
        OTHER ("Teleporting");

        private final String action;
        Type(String teleporting) {
            this.action = teleporting;
        }
    }
}
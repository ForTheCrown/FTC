package net.forthecrown.user.data;

import net.forthecrown.core.ComVars;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.events.dynamic.AsyncTeleportListener;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.FtcUser;
import net.forthecrown.core.chat.FtcFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.function.Supplier;

public class UserTeleport extends BukkitRunnable {

    private final FtcUser user;
    private final Supplier<Location> destination;
    private final boolean bypassCooldown;
    private final Type type;
    private AsyncTeleportListener listener;

    private Component startMessage;
    private Component completeMessage;
    private Component interruptMessage;

    public UserTeleport(FtcUser user, Supplier<Location> destination, boolean bypassCooldown, Type type) {
        this.user = user;
        this.destination = destination;
        this.bypassCooldown = bypassCooldown;
        this.type = type;
    }

    public void start(boolean tell){
        if(!bypassCooldown){
            if(tell){
                user.sendMessage(Objects.requireNonNullElseGet(startMessage, () ->
                        Component.text(type.action + " in ")
                                .color(NamedTextColor.GRAY)
                                .append(Component.text(FtcFormatter.convertTicksIntoTime(ComVars.getTpTickDelay())).color(NamedTextColor.GOLD))
                                .append(Component.newline())
                                .append(Component.text("Don't move!"))));
            }

            listener = new AsyncTeleportListener(user, this);
            runTaskLater(ForTheCrown.inst(), ComVars.getTpTickDelay());
        } else complete(tell);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        if(isScheduled()) super.cancel();
        if(listener != null) HandlerList.unregisterAll(listener);
    }

    public synchronized void stop(){
        try {
            super.cancel();
        } catch (IllegalStateException ignored) {}

        user.lastTeleport = null;
        if(listener != null) HandlerList.unregisterAll(listener);
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

    public void complete(boolean tell){
        if(isCancelled()){
            stop();
            return;
        }
        if(tell) user.sendMessage(Objects.requireNonNullElseGet(completeMessage, () ->
                Component.text(type.action + "...").color(NamedTextColor.GRAY)
        ));

        Location location = user.getLocation();
        user.onTpComplete();

        try { //It'll probably throw an exception when the player logs out and the supplier can't get the loc
            user.getPlayer().teleportAsync(destination.get());
            user.setLastLocation(location);
        } catch (NullPointerException ignored){ }

        if(listener != null) HandlerList.unregisterAll(listener);
        listener = null;
    }

    @Override
    public void run() {
        complete(true);
    }

    @Override
    public synchronized boolean isCancelled() throws IllegalStateException {
        try {
            return super.isCancelled();
        } catch (IllegalStateException e){
            return false;
        }
    }

    public boolean isScheduled(){
        try {
            return super.isCancelled();
        } catch (IllegalStateException e){
            return false;
        }
    }

    public boolean shouldBypassCooldown() {
        return bypassCooldown;
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

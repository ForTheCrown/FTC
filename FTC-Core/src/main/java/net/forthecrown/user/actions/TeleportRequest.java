package net.forthecrown.user.actions;

import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Crown;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserTeleport;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a user's request to teleport to, or be teleported to, by the target
 * <p></p>
 * That's not english
 */
public class TeleportRequest implements UserAction {

    private final CrownUser sender;
    private final CrownUser target;
    private final boolean tpaHere;
    public final BukkitRunnable expiry;

    public TeleportRequest(CrownUser sender, CrownUser target, boolean tpaHere) {
        this.sender = sender;
        this.target = target;
        this.tpaHere = !tpaHere;

        expiry = new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        };
    }

    public void startCountdown(){
        expiry.runTaskLaterAsynchronously(Crown.inst(), FtcVars.tpaExpiryTime.get());
    }

    public void onAccept(){
        CrownUser tp = tpaHere ? sender : target;
        CrownUser noTp = tpaHere ? target : sender;

        tp.createTeleport(noTp::getLocation, true, UserTeleport.Type.TPA).start(true);
        sender.sendMessage(
                Component.text()
                        .color(NamedTextColor.YELLOW)
                        .append(target.nickDisplayName().color(NamedTextColor.GOLD))
                        .append(Component.text(" accepted your tpa request."))
        );
        target.sendMessage(Component.text("Accepted tpa request").color(NamedTextColor.GRAY));

        stop();
    }

    public void onDeny(boolean remove){
        sender.sendMessage(
                Component.text()
                        .color(NamedTextColor.GRAY)
                        .append(target.nickDisplayName().color(NamedTextColor.GOLD))
                        .append(Component.text(" denied your tpa request"))
        );
        target.sendMessage(Component.text("Tpa request denied").color(NamedTextColor.GRAY));

        if(remove){
            sender.getInteractions().removeOutgoing(target);
            target.getInteractions().removeIncoming(sender);
        }

        expiry.cancel();
    }

    public void cancel(){
        sender.sendMessage(Component.text("Cancelling tpa request").color(NamedTextColor.YELLOW));
        target.sendMessage(
                Component.text()
                        .color(NamedTextColor.GRAY)
                        .append(sender. nickDisplayName().color(NamedTextColor.GOLD))
                        .append(Component.text(" cancelled his tpa request"))
        );

        stop();
    }

    public void stop(){
        sender.getInteractions().removeOutgoing(target);
        target.getInteractions().removeIncoming(sender);
        expiry.cancel();
    }

    public CrownUser getSender() {
        return sender;
    }

    public CrownUser getTarget() {
        return target;
    }

    public boolean isTpaHere() {
        return tpaHere;
    }

    @Override
    public String toString() {
        return "TeleportRequest{" +
                "sender=" + sender.getName() +
                ", receiver=" + target.getName() +
                ", tpaHere=" + tpaHere +
                '}';
    }

    @Override
    public void handle(UserActionHandler handler) {
        handler.handleTeleport(this);
    }
}

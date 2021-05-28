package net.forthecrown.emperor.user.data;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportRequest {
    private final CrownUser sender;
    private final CrownUser receiver;
    private final boolean tpaHere;
    private final BukkitRunnable expiry;

    public TeleportRequest(CrownUser sender, CrownUser receiver, boolean tpaHere) {
        this.sender = sender;
        this.receiver = receiver;
        this.tpaHere = tpaHere;

        expiry = new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        };
    }

    public void startCountdown(){
        expiry.runTaskLaterAsynchronously(CrownCore.inst(), CrownCore.getTpaExpiryTime());
    }

    public void onAccept(){
        CrownUser tp = tpaHere ? sender : receiver;
        CrownUser noTp = tpaHere ? receiver : sender;

        tp.createTeleport(noTp::getLocation, true, UserTeleport.Type.TPA).start(true);
        noTp.sendMessage(
                Component.text()
                        .color(NamedTextColor.YELLOW)
                        .append(tp.displayName().color(NamedTextColor.GOLD))
                        .append(Component.text(" accepted your tpa request."))
        );

        stop();
    }

    public void onDeny(boolean remove){
        sender.sendMessage(
                Component.text()
                        .color(NamedTextColor.GRAY)
                        .append(receiver.displayName().color(NamedTextColor.GOLD))
                        .append(Component.text(" denied your tpa request"))
        );
        receiver.sendMessage(Component.text("Tpa request denied").color(NamedTextColor.GRAY));

        if(remove){
            sender.getInteractions().removeOutgoing(receiver);
            receiver.getInteractions().removeIncoming(sender);
        }

        expiry.cancel();
    }

    public void cancel(){
        sender.sendMessage(Component.text("Cancelling tpa request").color(NamedTextColor.YELLOW));
        receiver.sendMessage(
                Component.text()
                        .color(NamedTextColor.GRAY)
                        .append(sender.displayName().color(NamedTextColor.GOLD))
                        .append(Component.text(" cancelled his tpa request"))
        );

        stop();
    }

    public void stop(){
        sender.getInteractions().removeOutgoing(receiver);
        receiver.getInteractions().removeIncoming(sender);
        expiry.cancel();
    }

    public CrownUser getSender() {
        return sender;
    }

    public CrownUser getReceiver() {
        return receiver;
    }

    public boolean isTpaHere() {
        return tpaHere;
    }

    @Override
    public String toString() {
        return "TeleportRequest{" +
                "sender=" + sender.getName() +
                ", receiver=" + receiver.getName() +
                ", tpaHere=" + tpaHere +
                '}';
    }
}

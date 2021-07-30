package net.forthecrown.user.data;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.user.CrownUser;
import net.forthecrown.core.chat.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class MarriageMessage {
    public static final Component PREFIX = Component.text()
            .color(TextColor.color(255, 158, 208))
            .append(Component.text("["))
            .append(Component.text("Marriage").color(TextColor.color(255, 204, 230)))
            .append(Component.text("] "))
            .build();

    public static final Component POINTER = Component.text(" > ")
            .color(TextColor.color(255, 158, 208))
            .decorate(TextDecoration.BOLD);

    private final CrownUser sender;
    private final CrownUser recipient;
    private final String input;
    private final Component formatted;
    private final MuteStatus status;

    public MarriageMessage(CrownUser sender, CrownUser recipient, String input) {
        this.sender = sender;
        this.recipient = recipient;
        this.input = input;
        this.formatted = ChatFormatter.formatIfAllowed(input, sender.getPlayer());
        status = ForTheCrown.getPunishmentManager().checkMute(sender.getPlayer());
    }

    public void complete(){
        Component formatted = format(sender.nickDisplayName(), this.formatted);

        EavesDropper.reportMarriageDM(this);

        if(sender.getInteractions().isBlockedPlayer(recipient.getUniqueId())){
            if(sender.getInteractions().isOnlyBlocked(recipient.getUniqueId())) sender.sendMessage(Component.translatable("marriage.ignored").color(NamedTextColor.GOLD));
            return;
        }

        if(status == MuteStatus.NONE || status == MuteStatus.SOFT) sender.sendBlockableMessage(recipient.getUniqueId(), formatted);
        if(status == MuteStatus.NONE) recipient.sendBlockableMessage(sender.getUniqueId(), formatted);
    }

    public static Component format(Component displayName, Component message){
        return Component.text()
                .append(PREFIX)
                .append(displayName.color(NamedTextColor.GOLD))
                .append(POINTER)
                .append(message)
                .build();
    }

    public CrownUser getSender() {
        return sender;
    }

    public CrownUser getReceiver() {
        return recipient;
    }

    public String getInput() {
        return input;
    }

    public Component getFormatted() {
        return formatted;
    }

    public MuteStatus getStatus() {
        return status;
    }
}

package net.forthecrown.core.user.data;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.chat.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class MarriageMessage {
    public static final Component PREFIX = Component.text()
            .color(NamedTextColor.GOLD)
            .append(Component.text("["))
            .append(Component.text("Marriage").color(NamedTextColor.YELLOW))
            .append(Component.text("] "))
            .build();

    public static final Component POINTER = Component.text(" > ")
            .color(NamedTextColor.YELLOW)
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
        this.formatted = ChatFormatter.formatStringIfAllowed(input, sender);
        status = CrownCore.getPunishmentManager().checkMute(sender.getPlayer());
    }

    public void complete(){
        Component formatted = format(sender.nickDisplayName(), this.formatted);

        EavesDropper.reportMarriageDM(this);

        if(sender.getInteractions().isBlockedPlayer(recipient.getUniqueId())){
            sender.sendMessage(Component.translatable("marriage.ignored").color(NamedTextColor.GOLD));
            return;
        }

        if(status == MuteStatus.NONE || status == MuteStatus.SOFT) sender.sendMessage(formatted);
        if(status == MuteStatus.NONE) recipient.sendMessage(formatted);
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

    public CrownUser getRecipient() {
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

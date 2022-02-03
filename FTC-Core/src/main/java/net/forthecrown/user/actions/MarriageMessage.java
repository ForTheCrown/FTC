package net.forthecrown.user.actions;

import net.forthecrown.core.Crown;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.Struct;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Struct for a message sent from a user to their spouse
 */
public class MarriageMessage implements UserAction, Struct {

    public static final Component PREFIX = Component.text()
            .color(TextColor.color(255, 158, 208))
            .append(Component.text("["))
            .append(Component.translatable("marriage").color(TextColor.color(255, 204, 230)))
            .append(Component.text("] "))
            .build();

    public static final Component POINTER = Component.text(" > ")
            .color(TextColor.color(255, 158, 208))
            .decorate(TextDecoration.BOLD);

    private final CrownUser sender;
    private final CrownUser target;
    private final String input;
    private final Component formatted;
    private MuteStatus status;

    public MarriageMessage(CrownUser sender, CrownUser target, String input) {
        this.sender = sender;
        this.target = target;
        this.input = input;
        this.formatted = FtcFormatter.formatIfAllowed(input, sender.getPlayer());
        status = Crown.getPunishments().checkMute(sender.getPlayer());
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

    public CrownUser getTarget() {
        return target;
    }

    public String getInput() {
        return input;
    }

    public Component getFormatted() {
        return formatted;
    }

    public MuteStatus getMuteStatus() {
        return status;
    }

    public void setStatus(MuteStatus status) {
        this.status = status;
    }

    @Override
    public void handle(UserActionHandler handler) {
        handler.handleMarriageMessage(this);
    }
}

package net.forthecrown.user.actions;

import net.forthecrown.core.Crown;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * struct for a DM between two command sources.
 */
public class DirectMessage implements UserAction {

    private final CommandSource sender;
    private final CommandSource target;

    private final Component formattedText;
    private final boolean responding;
    private final MuteStatus muteStatus;
    private final String input;

    public DirectMessage(CommandSource sender, CommandSource target, boolean responding, String input) {
        this.sender = sender;
        this.target = target;
        this.responding = responding;
        this.input = input;

        formattedText = FtcFormatter.formatIfAllowed(input, sender.asBukkit());
        muteStatus = Crown.getPunishmentManager().checkMute(sender.asBukkit());
    }

    public CommandSource getSender() {
        return sender;
    }

    public CommandSource getTarget() {
        return target;
    }

    public Component senderDisplayName(){ return FtcFormatter.sourceDisplayName(sender); }
    public Component receiverDisplayName(){ return FtcFormatter.sourceDisplayName(target); }

    public Component getSenderHeader(){
        return getHeader(
                Component.translatable("user.message.me").color(NamedTextColor.YELLOW),
                receiverDisplayName().color(NamedTextColor.YELLOW),
                NamedTextColor.GOLD
        );
    }

    public Component getReceiverHeader(){
        return getHeader(
                senderDisplayName().color(NamedTextColor.YELLOW),
                Component.translatable("user.message.me").color(NamedTextColor.YELLOW),
                NamedTextColor.GOLD
        );
    }

    public static Component getHeader(Component first, Component second, TextColor color){
        return Component.text("[")
                .color(color)
                .append(first)
                .append(Component.text(" -> "))
                .append(second)
                .append(Component.text("]"));
    }

    public MuteStatus getMuteStatus(){
        return muteStatus;
    }

    public Component getFormattedText() {
        return formattedText;
    }

    public boolean isResponding() {
        return responding;
    }

    public String getInput() {
        return input;
    }

    @Override
    public void handle(UserActionHandler handler) {
        handler.handleDirectMessage(this);
    }
}

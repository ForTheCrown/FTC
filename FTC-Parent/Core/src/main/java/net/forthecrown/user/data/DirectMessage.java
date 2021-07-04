package net.forthecrown.user.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class DirectMessage {
    private final CommandSource sender;
    private final CommandSource receiver;

    private final boolean responding;
    private String input;
    private Component formattedText;
    private MuteStatus muteStatus;

    public DirectMessage(CommandSource sender, CommandSource receiver, boolean responding, String input) {
        this.sender = sender;
        this.receiver = receiver;
        this.responding = responding;
        this.input = input;

        format();
    }

    public CommandSource getSender() {
        return sender;
    }

    public CommandSource getReceiver() {
        return receiver;
    }

    public Component senderDisplayName(){ return ChatFormatter.sourceDisplayName(sender); }
    public Component receiverDisplayName(){ return ChatFormatter.sourceDisplayName(receiver); }

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

    public void format() {
        formattedText = ChatFormatter.formatStringIfAllowed(input, sender.asBukkit());
        muteStatus = CrownCore.getPunishmentManager().checkMute(sender.asBukkit());
    }

    public void complete(){
        EavesDropper.reportDM(this);

        if(muteStatus == MuteStatus.NONE){
            Component receiverMessage = Component.text()
                    .append(getReceiverHeader())
                    .append(Component.text(" "))
                    .append(formattedText)
                    .build();
            receiver.sendMessage(receiverMessage);

            if(receiver.isPlayer()){
                try {
                    CrownUser user = UserManager.getUser(receiver.asPlayer());
                    user.setLastMessage(sender);

                    if(user.isAfk()) sender.sendMessage(Component.translatable("user.message.afk").color(NamedTextColor.YELLOW));
                } catch (CommandSyntaxException ignored) {}
            }
        }

        if(muteStatus == MuteStatus.SOFT || muteStatus == MuteStatus.NONE){
            Component senderMessage = Component.text()
                    .append(getSenderHeader())
                    .append(Component.text(" "))
                    .append(formattedText)
                    .build();

            sender.sendMessage(senderMessage);
        }

        receiver.onCommandComplete(null, muteStatus.maySpeak, 0);
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

    public void setInput(String input) {
        this.input = input;
    }
}

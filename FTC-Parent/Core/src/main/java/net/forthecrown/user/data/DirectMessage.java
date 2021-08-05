package net.forthecrown.user.data;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DirectMessage {
    private final CommandSource sender;
    private final CommandSource receiver;
    private final Component formattedText;
    private final boolean responding;
    private final MuteStatus muteStatus;
    private final String input;

    public DirectMessage(CommandSource sender, CommandSource receiver, boolean responding, String input) {
        this.sender = sender;
        this.receiver = receiver;
        this.responding = responding;
        this.input = input;

        formattedText = FtcFormatter.formatIfAllowed(input, sender.asBukkit());
        muteStatus = ForTheCrown.getPunishmentManager().checkMute(sender.asBukkit());
    }

    public CommandSource getSender() {
        return sender;
    }

    public CommandSource getReceiver() {
        return receiver;
    }

    public Component senderDisplayName(){ return FtcFormatter.sourceDisplayName(sender); }
    public Component receiverDisplayName(){ return FtcFormatter.sourceDisplayName(receiver); }

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

    public void complete(){
        EavesDropper.reportDM(this);

        UUID senderID = null;
        UUID receiverID = null;

        if(muteStatus == MuteStatus.NONE){
            if(receiver.isPlayer()){
                CrownUser user = UserManager.getUser(receiver.asOrNull(Player.class));
                user.setLastMessage(sender);

                receiverID = user.getUniqueId();

                if(sender.isPlayer()){
                    CrownUser senderUser = UserManager.getUser(sender.asOrNull(Player.class));
                    senderUser.setLastMessage(receiver);

                    senderID = senderUser.getUniqueId();

                    if(user.getInteractions().isBlockedPlayer(senderUser.getUniqueId())){
                        sender.sendMessage(Component.translatable("user.message.cannot", NamedTextColor.GRAY, user.nickDisplayName().color(NamedTextColor.YELLOW)));
                        return;
                    }
                }

                if(user.isAfk()) sender.sendMessage(Component.translatable("user.message.afk").color(NamedTextColor.YELLOW));
            }

            Component receiverMessage = Component.text()
                    .append(getReceiverHeader())
                    .append(Component.text(" "))
                    .append(formattedText)
                    .build();
            receiver.sendMessage(receiverMessage, senderID);
        }

        if(muteStatus == MuteStatus.SOFT || muteStatus == MuteStatus.NONE){
            Component senderMessage = Component.text()
                    .append(getSenderHeader())
                    .append(Component.text(" "))
                    .append(formattedText)
                    .build();

            sender.sendMessage(senderMessage, receiverID);
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
}

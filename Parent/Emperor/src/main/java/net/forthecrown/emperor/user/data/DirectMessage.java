package net.forthecrown.emperor.user.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.admin.EavesDropper;
import net.forthecrown.emperor.admin.MuteStatus;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.emperor.utils.ChatUtils;
import net.forthecrown.emperor.utils.CrownUtils;
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

    public Component senderDisplayName(){ return CrownUtils.sourceDisplayName(sender); }
    public Component receiverDisplayName(){ return CrownUtils.sourceDisplayName(receiver); }

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
        boolean emojis = sender.hasPermission(Permissions.DONATOR_3);
        boolean color = sender.hasPermission(Permissions.DONATOR_2);

        formattedText = ChatUtils.convertString((emojis ? ChatFormatter.formatEmojis(input) : input), color);
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

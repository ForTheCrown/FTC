package net.forthecrown.emperor.commands.manager;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.economy.Balances;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.kyori.adventure.text.Component;

public interface FtcExceptionProvider {
    static CommandSyntaxException create(String messasge){
        return CrownExceptionProvider.GENERIC.create(ChatFormatter.translateHexCodes(messasge));
    }

    static CommandSyntaxException createWithContext(String message, String input, int cursor){
        StringReader reader = new StringReader(input);
        reader.setCursor(cursor);

        return createWithContext(message, reader);
    }

    static CommandSyntaxException createWithContext(String message, StringReader reader){
        return CrownExceptionProvider.GENERIC.createWithContext(reader, ChatFormatter.translateHexCodes(message));
    }

    static CommandSyntaxException cannotAfford(int amount){
        return CrownExceptionProvider.CANNOT_AFFORD_TRANSACTION.create(Balances.formatted(amount));
    }

    static CommandSyntaxException cannotAfford(){
        return CrownExceptionProvider.CANNOT_AFFORD_INFOLESS.create();
    }

    static CommandSyntaxException senderTpaDisabled(){
        return CrownExceptionProvider.SENDER_TPA_DISABLED.create();
    }

    static CommandSyntaxException targetTpaDisabled(CrownUser user){
        return CrownExceptionProvider.TARGET_TPA_DISABLED.create(user.nickDisplayName());
    }

    static CommandSyntaxException senderEmoteDisabled(){
        return CrownExceptionProvider.SENDER_EMOTE_DISABLED.create();
    }

    static CommandSyntaxException targetEmoteDisabled(CrownUser name){
        return CrownExceptionProvider.TARGET_EMOTE_DISABLED.create(name.nickDisplayName());
    }

    static CommandSyntaxException cannotTeleport(){
        return CrownExceptionProvider.CANNOT_TELEPORT.create();
    }

    static CommandSyntaxException noTpRequest(){
        return CrownExceptionProvider.NO_TP_REQUESTS_INFOLESS.create();
    }

    static CommandSyntaxException noIncomingTP(CrownUser user){
        return CrownExceptionProvider.NO_TP_INCOMING.create(user.nickDisplayName());
    }

    static CommandSyntaxException noOutgoingTP(CrownUser user){
        return CrownExceptionProvider.NO_TP_OUTGOING.create(user.nickDisplayName());
    }

    static CommandSyntaxException cannotTpToSelf(){
        return CrownExceptionProvider.CANNOT_TP_TO_SELF.create();
    }

    static CommandSyntaxException cannotMute(CrownUser user){
        return CrownExceptionProvider.CANNOT_MUTE.create(user);
    }

    static CommandSyntaxException noReplyTargets(){
        return CrownExceptionProvider.NO_REPLY_TARGETS.create();
    }

    static CommandSyntaxException nickTooLong(int length){
        return CrownExceptionProvider.NICK_TOO_LONG.create(Component.text(length), Component.text(CrownCore.getMaxNickLength()));
    }

    static CommandSyntaxException cannotBan(CrownUser user){
        return CrownExceptionProvider.CANNOT_BAN.create(user);
    }

    static CommandSyntaxException cannotKick(CrownUser user){
        return CrownExceptionProvider.CANNOT_KICK.create(user);
    }
    
    static CommandSyntaxException cannotJail(CrownUser user){
        return CrownExceptionProvider.CANNOT_JAIL.create(user);
    }

    static CommandSyntaxException mustHoldItem(){
        return CrownExceptionProvider.MUST_BE_HOLDING_ITEM.create();
    }

    static CommandSyntaxException requestAlreadySent(CrownUser target){
        return CrownExceptionProvider.ALREADY_SENT.create(target.nickDisplayName());
    }

    static CommandSyntaxException noReturnLoc(){
        return CrownExceptionProvider.NO_RETURN.create();
    }

    static CommandSyntaxException alreadyBaron(){
        return CrownExceptionProvider.ALREADY_BARON.create();
    }

    static CommandSyntaxException holdingCoins(){
        return CrownExceptionProvider.HOLDING_COINS.create();
    }

    static CommandSyntaxException blockedPlayer(CrownUser user){
        return CrownExceptionProvider.BLOCKED_PLAYER.create(user.nickDisplayName());
    }

    static CommandSyntaxException senderPayDisabled(){
        return CrownExceptionProvider.SENDER_PAY_DISABLED.create();
    }

    static CommandSyntaxException targetPayDisabled(CrownUser user){
        return CrownExceptionProvider.TARGET_PAY_DISABLED.create(user.nickDisplayName());
    }

    static CommandSyntaxException cannotPaySelf(){
        return CrownExceptionProvider.CANNOT_PAY_SELF.create();
    }

    static CommandSyntaxException inventoryFull(){
        return CrownExceptionProvider.INV_FULL.create();
    }
}

package net.forthecrown.emperor.commands.manager;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.economy.Balances;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.grenadier.exceptions.TranslatableExceptionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import static net.forthecrown.emperor.commands.manager.CrownExceptionProvider.*;

public interface FtcExceptionProvider {
    static CommandSyntaxException create(String messasge){
        return GENERIC.create(ChatFormatter.translateHexCodes(messasge));
    }

    static CommandSyntaxException createWithContext(String message, String input, int cursor){
        StringReader reader = new StringReader(input);
        reader.setCursor(cursor);

        return createWithContext(message, reader);
    }

    static CommandSyntaxException createWithContext(String message, StringReader reader){
        return GENERIC.createWithContext(reader, ChatFormatter.translateHexCodes(message));
    }

    static RoyalCommandException translatable(String key, ComponentLike... args){
        return new TranslatableExceptionType(key).create(args);
    }

    static RoyalCommandException translatableWithContext(String key, ImmutableStringReader context, ComponentLike... args){
        return new TranslatableExceptionType(key).createWithContext(context, args);
    }

    static RoyalCommandException cannotAfford(int amount){
        return CANNOT_AFFORD_TRANSACTION.create(Balances.formatted(amount));
    }

    static RoyalCommandException cannotAfford(){
        return CANNOT_AFFORD_INFOLESS.create();
    }

    static RoyalCommandException senderTpaDisabled(){
        return SENDER_TPA_DISABLED.create();
    }

    static RoyalCommandException targetTpaDisabled(CrownUser user){
        return TARGET_TPA_DISABLED.create(user.nickDisplayName());
    }

    static RoyalCommandException senderEmoteDisabled(){
        return SENDER_EMOTE_DISABLED.create();
    }

    static RoyalCommandException targetEmoteDisabled(CrownUser name){
        return TARGET_EMOTE_DISABLED.create(name.nickDisplayName());
    }

    static RoyalCommandException cannotTeleport(){
        return CANNOT_TELEPORT.create();
    }

    static RoyalCommandException noTpRequest(){
        return NO_TP_REQUESTS_INFOLESS.create();
    }

    static RoyalCommandException noIncomingTP(CrownUser user){
        return NO_TP_INCOMING.create(user.nickDisplayName());
    }

    static RoyalCommandException noOutgoingTP(CrownUser user){
        return NO_TP_OUTGOING.create(user.nickDisplayName());
    }

    static RoyalCommandException cannotTpToSelf(){
        return CANNOT_TP_TO_SELF.create();
    }

    static CommandSyntaxException cannotMute(CrownUser user){
        return CANNOT_MUTE.create(user);
    }

    static RoyalCommandException noReplyTargets(){
        return NO_REPLY_TARGETS.create();
    }

    static RoyalCommandException nickTooLong(int length){
        return NICK_TOO_LONG.create(Component.text(length), Component.text(CrownCore.getMaxNickLength()));
    }

    static CommandSyntaxException cannotBan(CrownUser user){
        return CANNOT_BAN.create(user);
    }

    static CommandSyntaxException cannotKick(CrownUser user){
        return CANNOT_KICK.create(user);
    }
    
    static CommandSyntaxException cannotJail(CrownUser user){
        return CANNOT_JAIL.create(user);
    }

    static RoyalCommandException mustHoldItem(){
        return MUST_BE_HOLDING_ITEM.create();
    }

    static RoyalCommandException requestAlreadySent(CrownUser target){
        return ALREADY_SENT.create(target.nickDisplayName());
    }

    static RoyalCommandException noReturnLoc(){
        return NO_RETURN.create();
    }

    static RoyalCommandException alreadyBaron(){
        return ALREADY_BARON.create();
    }

    static RoyalCommandException holdingCoins(){
        return HOLDING_COINS.create();
    }

    static RoyalCommandException blockedPlayer(CrownUser user){
        return BLOCKED_PLAYER.create(user.nickDisplayName());
    }

    static RoyalCommandException senderPayDisabled(){
        return SENDER_PAY_DISABLED.create();
    }

    static RoyalCommandException targetPayDisabled(CrownUser user){
        return TARGET_PAY_DISABLED.create(user.nickDisplayName());
    }

    static RoyalCommandException cannotPaySelf(){
        return CANNOT_PAY_SELF.create();
    }

    static RoyalCommandException inventoryFull(){
        return INV_FULL.create();
    }
    
    static RoyalCommandException noDefaultHome(){
        return NO_DEF_HOME.create();
    }
}

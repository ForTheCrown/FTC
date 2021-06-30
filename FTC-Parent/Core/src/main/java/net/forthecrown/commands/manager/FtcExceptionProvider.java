package net.forthecrown.commands.manager;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.economy.Balances;
import net.forthecrown.user.CrownUser;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.grenadier.exceptions.TranslatableExceptionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import static net.forthecrown.commands.manager.CrownExceptionProvider.*;

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

    static RoyalCommandException emptyGrave(){
        return EMPTY_GRAVE.create();
    }

    static RoyalCommandException homeNameInUse(){
        return HOME_NAME_IN_USE.create();
    }

    static RoyalCommandException overHomeLimit(CrownUser user){
        return OVER_HOME_LIMIT.create(Component.text(user.getHighestTierRank().tier.maxHomes));
    }

    static RoyalCommandException noNearbyPlayers(){
        return NO_ONE_NEARBY.create();
    }

    static RoyalCommandException cannotIgnoreSelf(){
        return IGNORE_SELF_NO.create();
    }

    static RoyalCommandException noHomesToList(){
        return NO_HOMES.create();
    }

    static RoyalCommandException cannotReturn(){
        return CANNOT_RETURN.create();
    }

    static RoyalCommandException cannotTpaTo(CrownUser user){
        return CANNOT_TPA.create(user.nickDisplayName());
    }

    static RoyalCommandException cannotTpaHere(){
        return CANNOT_TPA_HERE.create();
    }

    static RoyalCommandException badWorldHome(String name){
        return CANNOT_TP_HOME.create(Component.text(name));
    }

    static RoyalCommandException cannotSetHomeHere(){
        return CANNOT_SET_HOME.create();
    }

    static RoyalCommandException cannotChangeMarriageStatus(){
        return MARRIAGE_CANNOT_CHANGE.create();
    }

    static RoyalCommandException cannotChangeMarriageStatusTarget(CrownUser user){
        return MARRIAGE_CANNOT_CHANGE_T.create(user.nickDisplayName());
    }

    static RoyalCommandException senderAlreadyMarried(){
        return MARRIED_SENDER.create();
    }

    static RoyalCommandException targetAlreadyMarried(CrownUser user){
        return MARRIED_TARGET.create(user.nickDisplayName());
    }

    static RoyalCommandException notMarried(){
        return NOT_MARRIED.create();
    }

    static RoyalCommandException notPirate(){
        return NOT_PIRATE.create(Component.text("/visit Questmoor"));
    }
}

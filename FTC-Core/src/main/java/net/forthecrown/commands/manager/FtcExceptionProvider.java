package net.forthecrown.commands.manager;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.inventory.ItemStack;

import static net.forthecrown.commands.manager.CrownExceptionProvider.*;

public interface FtcExceptionProvider {
    static RoyalCommandException create(String messasge){
        return OpenExceptionType.INSTANCE.create(ChatUtils.convertString(messasge, true));
    }

    static RoyalCommandException createWithContext(String message, String input, int cursor){
        StringReader reader = new StringReader(input);
        reader.setCursor(cursor);

        return createWithContext(message, reader);
    }

    static RoyalCommandException createWithContext(String message, StringReader reader){
        return OpenExceptionType.INSTANCE.createWithContext(ChatUtils.convertString(message, true), reader);
    }

    static RoyalCommandException translatable(String key, ComponentLike... args){
        return OpenExceptionType.INSTANCE.create(Component.translatable(key, args));
    }

    static RoyalCommandException translatable(String key, TextColor color, ComponentLike... args){
        return OpenExceptionType.INSTANCE.create(Component.translatable(key, color, args));
    }

    static RoyalCommandException translatableWithContext(String key, ImmutableStringReader context, ComponentLike... args){
        return OpenExceptionType.INSTANCE.createWithContext(Component.translatable(key, args), context);
    }

    static RoyalCommandException translatableWithContext(String key, TextColor color, ImmutableStringReader context, ComponentLike... args){
        return OpenExceptionType.INSTANCE.createWithContext(Component.translatable(key, color, args), context);
    }

    static RoyalCommandException cannotAfford(long amount){
        return CANNOT_AFFORD_TRANSACTION.create(FtcFormatter.rhines(amount));
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

    static RoyalCommandException noReplyTargets(){
        return NO_REPLY_TARGETS.create();
    }

    static RoyalCommandException nickTooLong(int length){
        return NICK_TOO_LONG.create(Component.text(length), Component.text(FtcVars.maxNickLength.get()));
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

    static RoyalCommandException overHomeLimit(CrownUser user){
        return OVER_HOME_LIMIT.create(Component.text(user.getRankTier().maxHomes));
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

    static RoyalCommandException cannotPayBlocked() {
        return CANNOT_PAY_BLOCKED.create();
    }

    static RoyalCommandException shopOutOfStock() {
        return SHOP_OUT_OF_STOCK.create(NamedTextColor.GRAY);
    }

    static RoyalCommandException dontHaveItemForShop(ItemStack item) {
        return SHOP_DONT_HAVE_ITEM.create(FtcFormatter.itemAndAmount(item));
    }

    static RoyalCommandException shopOwnerCannotAfford(int amount) {
        return SHOP_OWNER_CANNOT_AFFORD.create(NamedTextColor.GRAY, FtcFormatter.rhines(amount).color(NamedTextColor.YELLOW));
    }

    static RoyalCommandException noShopSpace() {
        return SHOP_NO_SPACE.create();
    }

    static RoyalCommandException maxShopPriceExceeded() {
        return SHOP_PRICE_EXCEEDED.create(FtcFormatter.rhines(FtcVars.maxSignShopPrice.get()));
    }

    static RoyalCommandException regionsWrongWorld() {
        return REGIONS_WRONG_WORLD.create();
    }

    static void checkNotBlocked(CrownUser user, CrownUser target) throws CommandSyntaxException {
        UserInteractions inter = user.getInteractions();

        if(inter.isOnlyBlocked(target.getUniqueId())) throw translatable("user.blockedOther", target.nickDisplayName());
        if(inter.isSeparatedPlayer(target.getUniqueId())) throw translatable("user.blockedOther.separated", target.nickDisplayName());
    }

    static void checkNotBlockedBy(CrownUser user, CrownUser target) throws RoyalCommandException {
        if(target.getInteractions().isBlockedPlayer(user.getUniqueId())) throw blockedPlayer(target);
    }

    static RoyalCommandException noShopOwned() {
        return translatable("market.noShopOwned");
    }
}

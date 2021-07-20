package net.forthecrown.core;

import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.admin.jails.JailManager;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.kingship.Kingship;
import net.forthecrown.economy.Balances;
import net.forthecrown.economy.ItemPriceMap;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.registry.KitRegistry;
import net.forthecrown.registry.WarpRegistry;
import net.forthecrown.serializer.UserSerializer;
import net.forthecrown.useables.UsablesManager;
import net.forthecrown.user.UserManager;
import net.forthecrown.valhalla.Valhalla;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Namespaced;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class that looks nice and does all the api stuff
 */
public interface CrownCore extends Plugin, Namespaced {
    static CrownCore inst(){ return Main.inst; }

    static PunishmentManager getPunishmentManager() { return Main.punishmentManager; }
    static UsablesManager getUsablesManager() { return Main.usablesManager; }
    static JailManager getJailManager() { return Main.jailManager; }
    static ShopManager getShopManager() { return Main.shopManager; }
    static UserManager getUserManager(){ return Main.userManager; }

    static UserSerializer getUserSerializer() { return Main.userSerializer; }
    static LuckPerms getLuckPerms() { return Main.luckPerms; }
    static Announcer getAnnouncer(){ return Main.announcer; }
    static Balances getBalances(){ return Main.balances; }
    static Kingship getKingship(){ return Main.kingship; }
    static TabList getTabList() { return Main.tabList; }
    static ItemPriceMap getPriceMap() { return Main.prices; }

    static WarpRegistry getWarpRegistry() { return Main.warpRegistry; }
    static KitRegistry getKitRegistry() { return Main.kitRegistry; }

    static CrownMessages getMessages() { return Main.messages; }
    static DayUpdate getDayUpdate() { return Main.dayUpdate; }
    static JoinInfo getJoinInfo() { return Main.joinInfo; }
    static ServerRules getRules() { return Main.rules; }
    static ChatEmotes getEmotes() { return Main.emotes; }

    static Logger logger() { return Main.logger; }
    static File dataFolder() { return inst().getDataFolder(); }
    static FileConfiguration config() { return inst().getConfig(); }
    static InputStream resource(String name) { return inst().getResource(name); }
    static void saveResource(boolean replace, String name) { inst().saveResource(name, replace); }
    static PluginDescriptionFile description() { return inst().getDescription(); }

    static void saveFTC(){
        Main.kingship.save();

        Main.userManager.save();
        Main.userManager.saveUsers();

        Main.balances.save();

        Main.warpRegistry.save();
        Main.kitRegistry.save();

        Main.punishmentManager.save();
        Main.jailManager.save();

        Main.shopManager.save();
        Main.usablesManager.saveAll();

        Main.joinInfo.save();
        Main.prices.save();

        if(inDebugMode()) {
            Valhalla.getInstance().saveAll();
        }

        Main.inst.saveConfig();
        logger().log(Level.INFO, "FTC-Core saved");
    }

    static short getHoppersInOneChunk() {
        return ComVars.hoppersInOneChunk.getValue((short) 256);
    }

    static long getUserResetInterval(){
        return ComVars.userDataResetInterval.getValue(5356800000L);
    }

    static boolean areTaxesEnabled(){
        return ComVars.taxesEnabled.getValue(false);
    }

    static Integer getMaxMoneyAmount(){
        return ComVars.maxMoneyAmount.getValue(50000000);
    }

    static long getBranchSwapCooldown() {
        return ComVars.branchSwapCooldown.getValue(172800000L);
    }

    static boolean logAdminShopUsage(){
        return ComVars.logAdminShop.getValue(true);
    }

    static boolean logNormalShopUsage(){
        return ComVars.logNormalShop.getValue(false);
    }

    static int getTpTickDelay(){
        return ComVars.tpTickDelay.getValue(60);
    }

    static int getTpCooldown(){
        return ComVars.tpCooldown.getValue(60);
    }

    static int getTpaExpiryTime(){
        return ComVars.tpaExpiryTime.getValue(2400);
    }

    static int getStartRhines(){
        return ComVars.startRhines.getValue(100);
    }

    static byte getMaxNickLength(){
        return ComVars.maxNickLength.getValue((byte) 16);
    }

    static boolean allowOtherPlayerNameNicks(){
        return ComVars.allowOtherPlayerNicks.getValue(false);
    }

    static int getBaronPrice(){
        return ComVars.baronPrice.getValue(500000);
    }

    static short getNearRadius(){
        return ComVars.nearRadius.getValue((short) 200);
    }

    static Key onFirstJoinKit(){
        return ComVars.onFirstJoinKit.getValue();
    }

    static long getMarriageCooldown(){
        return ComVars.marriageCooldown.getValue(259200000L);
    }

    static long getAuctionExpirationTime(){
        return ComVars.auctionExpirationTime.getValue(604800000L);
    }

    static long getAuctionPickupTime(){
        return ComVars.auctionPickupTime.getValue(259200000L);
    }

    static World getTreasureWorld(){
        return ComVars.treasureWorld.getValue();
    }

    static int getGhSpecialReward(){
        return ComVars.ghSpecialReward.getValue(25000);
    }

    static int getGhFinalReward(){
        return ComVars.ghFinalReward.getValue(50000);
    }

    static boolean isEventActive(){
        return ComVars.crownEventActive.getValue(false);
    }

    static boolean isEventTimed(){
        return ComVars.crownEventIsTimed.getValue(false);
    }

    static int getTreasureMaxPrize(){
        return ComVars.maxTreasurePrize.getValue(50000);
    }

    static int getTreasureMinPrize(){
        return ComVars.minTreasurePrize.getValue(10000);
    }

    static int getMaxBossDifficulty(){
        return ComVars.maxBossDifficulty.getValue((byte) 5);
    }

    static byte getMaxTreasureItems(){
        return ComVars.maxTreasureItems.getValue((byte) 5);
    }

    static int getMaxShopEarnings() {
        return ComVars.maxShopEarnings.getValue(500000);
    }

    static Location getServerSpawn(){
        return Main.serverSpawn;
    }

    static void setServerSpawn(Location l){
        Main.serverSpawn = l;
    }

    static String getPrefix(){
        return ChatFormatter.translateHexCodes(Main.prefix);
    }

    static boolean inDebugMode(){
        return Main.inDebugMode.getValue(false);
    }

    static String getDiscord(){
        return ChatFormatter.translateHexCodes(Main.discord);
    }

    static Component prefix(){
        return Component.text(ChatColor.stripColor(getPrefix()))
                .color(NamedTextColor.GOLD)
                .hoverEvent(HoverEvent.showText(Component.text("For The Crown").color(NamedTextColor.YELLOW)));
    }

    static Key coreKey(String value){
        return Key.key(inst(), value);
    }
}

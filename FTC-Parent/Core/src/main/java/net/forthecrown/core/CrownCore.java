package net.forthecrown.core;

import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.admin.jails.JailManager;
import net.forthecrown.core.comvars.ComVar;
import net.forthecrown.core.economy.Balances;
import net.forthecrown.core.economy.BlackMarket;
import net.forthecrown.core.economy.shops.ShopManager;
import net.forthecrown.core.registry.ActionRegistry;
import net.forthecrown.core.registry.CheckRegistry;
import net.forthecrown.core.registry.KitRegistry;
import net.forthecrown.core.registry.WarpRegistry;
import net.forthecrown.core.chat.Emotes;
import net.forthecrown.core.useables.UsablesManager;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.utils.MapUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Namespaced;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class that looks nice and does all the api stuff
 */
public interface CrownCore extends Plugin, Namespaced {
    static CrownCore inst(){ return Main.inst; }

    static UserManager getUserManager(){ return Main.userManager; }
    static PunishmentManager getPunishmentManager() { return Main.punishmentManager; }
    static JailManager getJailManager() { return Main.jailManager; }
    static ShopManager getShopManager() { return Main.shopManager; }
    static UsablesManager getUsablesManager() { return Main.usablesManager; }

    static Announcer getAnnouncer(){ return Main.announcer; }
    static Balances getBalances(){ return Main.balances; }
    static BlackMarket getBlackMarket(){ return Main.blackMarket; }
    static Kingship getKingship(){ return Main.kingship; }
    static LuckPerms getLuckPerms() { return Main.luckPerms; }

    static WarpRegistry getWarpRegistry() { return Main.warpRegistry; }
    static KitRegistry getKitRegistry() { return Main.kitRegistry; }
    static ActionRegistry getActionRegistry() { return Main.actionRegistry; }
    static CheckRegistry getCheckRegistry() { return Main.checkRegistry; }

    static CrownMessages getMessages() { return Main.messages; }
    static ServerRules getRules() { return Main.rules; }
    static Emotes getEmotes() { return Main.emotes; }

    static Logger logger() { return Main.logger; }
    static File dataFolder() { return inst().getDataFolder(); }
    static FileConfiguration config() { return inst().getConfig(); }
    static InputStream resource(String name) { return inst().getResource(name); }

    static void saveFTC(){
        Main.kingship.save();

        Main.userManager.save();
        Main.userManager.saveUsers();

        Main.balances.save();
        Main.blackMarket.save();

        Main.warpRegistry.save();
        Main.kitRegistry.save();

        Main.punishmentManager.save();
        Main.jailManager.save();

        Main.shopManager.save();
        Main.usablesManager.saveAll();

        Main.inst.saveConfig();
        logger().log(Level.INFO, "FTC-Core saved");
    }

    static short getHoppersInOneChunk() {
        return Main.hoppersInOneChunk.getValue((short) 256);
    }

    static long getUserDataResetInterval(){
        return Main.userDataResetInterval.getValue(5356800000L);
    }

    static boolean areTaxesEnabled(){
        return Main.taxesEnabled.getValue(false);
    }

    static Integer getMaxMoneyAmount(){
        return Main.maxMoneyAmount.getValue(50000000);
    }

    static long getBranchSwapCooldown() {
        return Main.branchSwapCooldown.getValue(172800000L);
    }

    static boolean logAdminShopUsage(){
        return Main.logAdminShop.getValue(true);
    }

    static boolean logNormalShopUsage(){
        return Main.logNormalShop.getValue(false);
    }

    static int getTpTickDelay(){
        return Main.tpTickDelay.getValue(60);
    }

    static int getTpCooldown(){
        return Main.tpCooldown.getValue(60);
    }

    static int getTpaExpiryTime(){
        return Main.tpaExpiryTime.getValue(2400);
    }

    static int getStartRhines(){
        return Main.startRhines.getValue(100);
    }

    static byte getMaxNickLength(){
        return Main.maxNickLength.getValue((byte) 16);
    }

    static boolean allowOtherPlayerNameNicks(){
        return Main.allowOtherPlayerNicks.getValue(false);
    }

    static int getBaronPrice(){
        return Main.baronPrice.getValue(500000);
    }

    static short getNearRadius(){
        return Main.nearRadius.getValue((short) 200);
    }

    static Key onFirstJoinKit(){
        return Main.onFirstJoinKit.getValue();
    }

    static long getMarriageCooldown(){
        return Main.marriageCooldown.getValue(259200000L);
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

    static Map<Material, Short> getItemPrices(){ //returns the default item Price Map
        return MapUtils.convertValues(Main.defaultItemPrices, ComVar::getValue);
    }
    static Short getItemPrice(Material material){ //Returns the default price for an item
        return Main.defaultItemPrices.get(material).getValue((short) 2);
    }

    static String getDiscord(){
        return ChatFormatter.translateHexCodes(Main.discord);
    }

    static Component prefix(){
        return Component.text(ChatColor.stripColor(getPrefix()))
                .color(NamedTextColor.GOLD)
                .hoverEvent(HoverEvent.showText(Component.text("For The Crown :D, tell Botul you found this text lol").color(NamedTextColor.YELLOW)));
    }
}

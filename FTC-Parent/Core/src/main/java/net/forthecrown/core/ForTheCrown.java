package net.forthecrown.core;

import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.admin.jails.JailManager;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.kingship.Kingship;
import net.forthecrown.economy.Balances;
import net.forthecrown.economy.ItemPriceMap;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.serializer.UserSerializer;
import net.forthecrown.useables.UsablesManager;
import net.forthecrown.useables.kits.KitManager;
import net.forthecrown.useables.warps.WarpManager;
import net.forthecrown.user.UserManager;
import net.forthecrown.valhalla.Valhalla;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Namespaced;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class that looks nice and does all the api stuff
 * <p></p>
 * Implementation: {@link Main}
 */
public interface ForTheCrown extends Plugin, Namespaced {
    static ForTheCrown inst(){ return Main.inst; }

    static PunishmentManager getPunishmentManager() { return Main.punishmentManager; }
    static UsablesManager getUsablesManager() { return Main.usablesManager; }
    static JailManager getJailManager() { return Main.jailManager; }
    static ShopManager getShopManager() { return Main.shopManager; }
    static UserManager getUserManager(){ return Main.userManager; }
    static WarpManager getWarpManager() { return Main.warpRegistry; }
    static KitManager getKitManager() { return Main.kitRegistry; }

    static UserSerializer getUserSerializer() { return Main.userSerializer; }
    static LuckPerms getLuckPerms() { return Main.luckPerms; }
    static Announcer getAnnouncer(){ return Main.announcer; }
    static Balances getBalances(){ return Main.balances; }
    static Kingship getKingship(){ return Main.kingship; }
    static TabList getTabList() { return Main.tabList; }
    static ItemPriceMap getPriceMap() { return Main.prices; }

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
        Main.tabList.save();

        if(inDebugMode()) {
            Valhalla.getInstance().saveAll();
        }

        Main.inst.saveConfig();
        logger().log(Level.INFO, "FTC-Core saved");
    }

    static Location getServerSpawn(){
        return Main.serverSpawn;
    }

    static void setServerSpawn(Location l){
        Main.serverSpawn = l;
    }

    static String getPrefix(){
        return FtcFormatter.translateHexCodes(Main.prefix);
    }

    static boolean inDebugMode(){
        return Main.inDebugMode.getValue(false);
    }

    static String getDiscord(){
        return FtcFormatter.translateHexCodes(Main.discord);
    }

    static Component prefix(){
        return Component.text(ChatColor.stripColor(getPrefix()))
                .color(NamedTextColor.GOLD)
                .hoverEvent(Component.text("For The Crown").color(NamedTextColor.YELLOW));
    }

    static Key coreKey(String value){
        return Key.key(inst(), value);
    }
}

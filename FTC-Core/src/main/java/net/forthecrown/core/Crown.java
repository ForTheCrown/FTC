package net.forthecrown.core;

import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.admin.jails.JailManager;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.kingship.Kingship;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.ItemPriceMap;
import net.forthecrown.economy.guild.TradersGuild;
import net.forthecrown.economy.houses.HouseSerializer;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.useables.UsablesManager;
import net.forthecrown.useables.kits.KitManager;
import net.forthecrown.useables.warps.WarpManager;
import net.forthecrown.user.manager.UserManager;
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
public interface Crown extends Plugin, Namespaced {
    static Crown inst(){ return Main.inst; }

    // Feels like I'm violating syntax by aligning the methods like this
    static PunishmentManager    getPunishmentManager()  { return Main.punishmentManager; }
    static UsablesManager       getUsables()            { return Main.usablesManager; }
    static RegionManager        getRegionManager()      { return Main.regionManager; }
    static JailManager          getJailManager()        { return Main.jailManager; }
    static ShopManager          getShopManager()        { return Main.shopManager; }
    static UserManager          getUserManager()        { return Main.userManager; }
    static WarpManager          getWarpManager()        { return Main.warpRegistry; }
    static KitManager           getKitManager()         { return Main.kitRegistry; }

    static TradersGuild         getTradersGuild()       { return Main.tradersGuild; }
    static Markets              getMarkets()            { return Main.markets; }
    static LuckPerms            getLuckPerms()          { return Main.luckPerms; }
    static Announcer            getAnnouncer()          { return Main.announcer; }
    static ItemPriceMap         getPriceMap()           { return Main.prices; }
    static Economy              getEconomy()            { return Main.economy; }
    static Kingship             getKingship()           { return Main.kingship; }
    static TabList              getTabList()            { return Main.tabList; }

    static FtcMessages          getMessages()           { return Main.messages; }
    static DayUpdate            getDayUpdate()          { return Main.dayUpdate; }
    static JoinInfo             getJoinInfo()           { return Main.joinInfo; }
    static ChatEmotes           getEmotes()             { return Main.emotes; }
    static ServerRules          getRules()              { return Main.rules; }

    static Logger logger() { return Main.logger; }
    static File dataFolder() { return inst().getDataFolder(); }
    static FileConfiguration config() { return inst().getConfig(); }
    static InputStream resource(String name) { return inst().getResource(name); }
    static PluginDescriptionFile description() { return inst().getDescription(); }
    static void saveResource(boolean replace, String name) { inst().saveResource(name, replace); }

    static void saveFTC(){
        Main.kingship.save();

        Main.userManager.save();
        Main.userManager.saveUsers();

        Main.economy.save();

        Main.warpRegistry.save();
        Main.kitRegistry.save();

        Main.punishmentManager.save();
        Main.jailManager.save();

        Main.shopManager.save();
        Main.usablesManager.saveAll();

        Main.joinInfo.save();
        Main.prices.save();
        Main.tabList.save();

        Main.regionManager.save();

        Main.markets.save();
        Main.tradersGuild.save();

        HouseSerializer.serialize();
        ComVars.save();
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
        return FtcFormatter.formatColorCodes(Main.prefix);
    }

    static boolean inDebugMode(){
        return Main.inDebugMode.getValue(false);
    }

    static String getDiscord(){
        return FtcFormatter.formatColorCodes(Main.discord);
    }

    static Component prefix(){
        return Component.text(ChatColor.stripColor(getPrefix()))
                .color(NamedTextColor.GOLD)
                .hoverEvent(Component.text("For The Crown").color(NamedTextColor.YELLOW));
    }

}

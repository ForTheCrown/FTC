package net.forthecrown.core;

import net.forthecrown.core.admin.Punisher;
import net.forthecrown.core.battlepass.BattlePass;
import net.forthecrown.core.chat.*;
import net.forthecrown.dungeons.level.LevelSerializer;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.ItemPriceMap;
import net.forthecrown.economy.guilds.TradeGuild;
import net.forthecrown.economy.houses.HouseSerializer;
import net.forthecrown.economy.houses.Houses;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.structure.FtcStructSerializer;
import net.forthecrown.useables.UsablesManager;
import net.forthecrown.useables.kits.KitManager;
import net.forthecrown.useables.warps.WarpManager;
import net.forthecrown.user.UserManager;
import net.forthecrown.vars.VarRegistry;
import net.kyori.adventure.key.Namespaced;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.InputStream;

/**
 * Main class that looks nice and does all the api stuff
 * <p></p>
 * Implementation: {@link Main}
 */
public interface Crown extends Plugin, Namespaced {
    static Crown                inst()                  { return Main.inst; }

    // Feels like I'm violating syntax by aligning the methods like this

    // Managers
    static UsablesManager       getUsables()            { return Main.usablesManager; }
    static RegionManager        getRegionManager()      { return Main.regionManager; }
    static ShopManager          getShopManager()        { return Main.shopManager; }
    static UserManager          getUserManager()        { return Main.userManager; }
    static WarpManager          getWarpManager()        { return Main.warpRegistry; }
    static KitManager           getKitManager()         { return Main.kitRegistry; }

    // Classes that are interfaces for some reason
    static Punisher             getPunisher()           { return Main.punisher; }
    static Markets              getMarkets()            { return Main.markets; }
    static LuckPerms            getLuckPerms()          { return Main.luckPerms; }
    static Announcer            getAnnouncer()          { return Main.announcer; }
    static ItemPriceMap         getPriceMap()           { return Main.prices; }
    static Economy              getEconomy()            { return Main.economy; }
    static Kingship             getKingship()           { return Main.kingship; }
    static TabList              getTabList()            { return Main.tabList; }
    static BattlePass           getBattlePass()         { return Main.battlePass; }

    // Classes that aren't interfaces
    static FtcMessages          getMessages()           { return Main.messages; }
    static DayChange            getDayChange()          { return Main.dayChange; }
    static JoinInfo             getJoinInfo()           { return Main.joinInfo; }
    static ChatEmotes           getEmotes()             { return Main.emotes; }
    static TradeGuild           getGuild()              { return Main.guild; }
    static ServerRules          getRules()              { return Main.rules; }
    static EndOpener            getEndOpener()          { return Main.endOpener; }
    static ResourceWorld        getResourceWorld()      { return Main.resourceWorld; }
    static ServerHolidays       getHolidays()           { return Main.holidays; }
    static FtcStructSerializer  getStructureManager()   { return Main.structSerializer; }

    // Bukkit stuff
    static FtcConfig            config()                { return Main.config; }
    static Logger               logger()                { return Main.logger; }
    static File                 dataFolder()            { return inst().getDataFolder(); }
    static InputStream          resource(String name)   { return inst().getResource(name); }

    static PluginDescriptionFile description()          { return inst().getDescription(); }
    static void saveResource(boolean replace, String name) { inst().saveResource(name, replace); }

    static void saveFTC() {
        Main.userManager.save();
        Main.userManager.saveUsers();

        Main.economy.save();

        Main.warpRegistry.save();
        Main.kitRegistry.save();

        Main.punisher.save();

        Main.shopManager.save();
        Main.usablesManager.saveAll();

        Main.prices.save();

        Main.regionManager.save();
        Main.structSerializer.save();

        Main.markets.save();
        Main.guild.save();
        Main.config.save();
        Main.battlePass.save();

        Main.userManager.saveCache();

        Main.holidays.save();

        if(Houses.ENABLED) {
            HouseSerializer.serialize();
        }

        VarRegistry.save();
        LevelSerializer.save();

        logger().info("FTC-Core saved");
    }

    static boolean inDebugMode() {
        return FtcVars.inDebugMode.getValue(false);
    }

    static Component prefix() {
        return config().prefix();
    }
}
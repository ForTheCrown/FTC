package net.forthecrown.core;

import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.BlackMarket;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.commands.brigadier.RoyalBrigadier;
import net.forthecrown.core.comvars.ComVar;
import net.forthecrown.core.comvars.ComVars;
import net.forthecrown.core.comvars.types.ComVarType;
import net.forthecrown.core.crownevents.ArmorStandLeaderboard;
import net.forthecrown.core.events.*;
import net.forthecrown.core.files.*;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

public final class FtcCore extends JavaPlugin {

    private static FtcCore instance;

    private static String prefix = "&6[FTC]&r  ";
    private static String king;
    private static String discord;
    private static final Map<Material, ComVar<Short>> defaultItemPrices = new HashMap<>();
    private static int saverID;
    private Integer maxMoneyAmount;

    private static ComVar<Long>     userDataResetInterval;// = 5356800000L; //2 months by default
    private static ComVar<Long>     branchSwapCooldown;// = 172800000; //2 days by default
    private static ComVar<Boolean>  taxesEnabled;// = false;
    private static ComVar<Boolean>  logAdminShop;
    private static ComVar<Boolean>  logNormalShop;
    private static ComVar<Byte>     hoppersInOneChunk;

    private static CrownAnnouncer   announcer;
    private static CrownBalances    balFile;
    private static CrownBlackMarket bm;
    private static RoyalBrigadier   brigadier;
    private static CrownWorldGuard  crownWorldGuard;
    private static CrownUserManager userManager;

    public static final Set<ArmorStandLeaderboard> LEADERBOARDS = new HashSet<>();
    public static LuckPerms LUCK_PERMS;

    public static NamespacedKey SHOP_KEY;

    @Override
    public void onEnable() {
        instance = this;
        LUCK_PERMS = LuckPermsProvider.get();
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        announcer = new CrownAnnouncer();
        balFile = new CrownBalances(this);
        bm = new CrownBlackMarket(this);
        brigadier = new RoyalBrigadier(this);
        userManager = new CrownUserManager();

        SHOP_KEY = new NamespacedKey(this, "signshop");

        registerEvents();
        if(getConfig().getBoolean("System.run-deleter-on-startup")) new FileChecker(getDataFolder());
    }

    @Override
    public void onLoad() {
        crownWorldGuard = new CrownWorldGuard(this);
        crownWorldGuard.registerFlags();
    }

    @Override
    public void onDisable() {
        if(getConfig().getBoolean("System.save-on-disable")) saveFTC();

        Bukkit.getScheduler().cancelTasks(this);

        for (Player p: Bukkit.getOnlinePlayers()){
            p.closeInventory();
        }

        for (ArmorStandLeaderboard a: LEADERBOARDS){
            a.destroy();
        }
    }

    private void registerEvents(){
        Server server = getServer();
        PluginManager pm = server.getPluginManager();

        pm.registerEvents(new JeromeEvent(), this);

        pm.registerEvents(new CoreListener(), this);
        pm.registerEvents(new GraveListener(), this);
        pm.registerEvents(new ChatEvents(), this);

        pm.registerEvents(new ShopCreateEvent(), this);
        pm.registerEvents(new ShopInteractEvent(), this);
        pm.registerEvents(new ShopDestroyEvent(), this);
        pm.registerEvents(new ShopTransactionEvent(), this);

        pm.registerEvents(new BlackMarketEvents(), this);
    }

    private void loadDefaultItemPrices(){
        ConfigurationSection itemPrices = getInstance().getConfig().getConfigurationSection("DefaultPrices");

        for(String s : itemPrices.getKeys(true)){
            Material mat;
            try {
                mat = Material.valueOf(s);
            } catch (Exception e){
                continue;
            }

            //defaultItemPrices.put(mat, (short) itemPrices.getInt(s));
            defaultItemPrices.put(mat,
                    ComVars.set(
                            "sellshop_price_" + s.toLowerCase(),
                            ComVarType.SHORT,
                            (short) itemPrices.getInt(s)
                    )
            );
        }
    }

    //every hour it saves everything
    private void periodicalSave(){
        final long interval = getConfig().getLong("System.save-interval-mins")*60*20;
        saverID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, FtcCore::saveFTC, interval, interval);
    }

    @Override
    public void saveConfig() {
        getConfig().set("King", king);
        getConfig().set("Taxes", taxesEnabled.getValue());
        getConfig().set("BranchSwapCooldown", branchSwapCooldown.getValue());
        getConfig().set("UserDataResetInterval", userDataResetInterval.getValue());
        getConfig().set("Shops.log-normal-purchases", logNormalShop.getValue());
        getConfig().set("Shops.log-admin-purchases", logAdminShop.getValue());
        getConfig().set("HoppersInOneChunk", hoppersInOneChunk.getValue());

        super.saveConfig();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        prefix = getConfig().getString("Prefix");
        discord = getConfig().getString("Discord");
        maxMoneyAmount = getConfig().getInt("MaxMoneyAmount");

        branchSwapCooldown = ComVars.set("sv_branchSwapInterval", ComVarType.LONG, getConfig().getLong("BranchSwapCooldown"));
        taxesEnabled = ComVars.set("sv_taxesEnabled", ComVarType.BOOLEAN, getConfig().getBoolean("Taxes"));
        userDataResetInterval = ComVars.set("sv_userEarningsResetInterval", ComVarType.LONG, getConfig().getLong("UserDataResetInterval"));
        logAdminShop = ComVars.set("sv_log_admin", ComVarType.BOOLEAN, getConfig().getBoolean("Shops.log-admin-purchases"));
        logNormalShop = ComVars.set("sv_log_normal", ComVarType.BOOLEAN, getConfig().getBoolean("Shops.log-normal-purchases"));
        hoppersInOneChunk = ComVars.set("sv_maxHoppersPerChunk", ComVarType.BYTE, (byte) getConfig().getInt("HoppersInOneChunk"));

        loadDefaultItemPrices();

        if(!getConfig().getString("King").contains("empty")) king = getConfig().getString("King");
        else king = "empty"; //like my soul

        if(getConfig().getBoolean("System.save-periodically")) periodicalSave();
        else if (saverID != 0){
            Bukkit.getScheduler().cancelTask(saverID);
            saverID = 0;
        }
    }

    public static void saveFTC(){
        ShopManager.save();
        getUserManager().save();
        getUserManager().saveUsers();
        getAnnouncer().save();
        getBalances().save();
        getBlackMarket().save();

        getInstance().saveConfig();
        Announcer.log(Level.INFO, "FTC-Core saved");
    }

    @Nullable
    public static UUID getKing() {
        UUID result;
        try {
            result = UUID.fromString(king);
        } catch (Exception e){
            result = null;
        }
        return result;
    }

    public static void setKing(@Nullable UUID newKing) {
        if(newKing == null){
            UserManager.getUser(getKing()).clearTabPrefix();
            king = "empty";
        }
        else king = newKing.toString();
    }

    /*public static Map<Material, Short> getItemPrices(){ //returns the default item Price Map
        return defaultItemPrices;
    }*/
    public static Short getItemPrice(Material material){ //Returns the default price for an item
        return defaultItemPrices.get(material).getValue((short) 2);
    }

    public static String getDiscord(){ //gets and sets the discord link
        return CrownUtils.translateHexCodes(discord);
    }

    public static String getPrefix(){
        return CrownUtils.translateHexCodes(prefix);
    }

    public static Component prefix(){
        return Component.text(ChatColor.stripColor(getPrefix()))
                .color(NamedTextColor.GOLD)
                .hoverEvent(HoverEvent.showText(Component.text("For The Crown :D, tell Botul you found this text lol").color(NamedTextColor.YELLOW)));
    }

    public static byte getHoppersInOneChunk() {
        return hoppersInOneChunk.getValue();
    }

    public static long getUserDataResetInterval(){
        return userDataResetInterval.getValue();
    }

    public static boolean areTaxesEnabled(){
        return taxesEnabled.getValue();
    }

    public static Integer getMaxMoneyAmount(){
        return instance.maxMoneyAmount;
    }

    public static long getBranchSwapCooldown() {
        return branchSwapCooldown.getValue();
    }

    public static boolean logAdminShopUsage(){
        return logAdminShop.getValue();
    }

    public static boolean logNormalShopUsage(){
        return logNormalShop.getValue();
    }

    //get a part of the plugin with these
    public static FtcCore getInstance(){
        return instance;
    }
    public static Announcer getAnnouncer(){
        return announcer;
    }
    public static Balances getBalances(){
        return balFile;
    }
    public static BlackMarket getBlackMarket() {
        return bm;
    }
    public static UserManager getUserManager() {
        return userManager;
    }
    public static RoyalBrigadier getRoyalBrigadier(){
        return brigadier;
    }
    public static CrownWorldGuard getCrownWorldGuard() {
        return crownWorldGuard;
    }
}
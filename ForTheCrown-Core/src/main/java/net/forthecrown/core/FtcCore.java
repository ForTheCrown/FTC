package net.forthecrown.core;

import net.forthecrown.core.api.*;
import net.forthecrown.core.commands.brigadier.RoyalBrigadier;
import net.forthecrown.core.crownevents.ArmorStandLeaderboard;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.events.*;
import net.forthecrown.core.events.npc.JeromeEvent;
import net.forthecrown.core.files.*;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class FtcCore extends JavaPlugin {

    private static FtcCore instance;

    private static String prefix = "&6[FTC]&r  ";
    private static long userDataResetInterval = 5356800000L; //2 months by default
    private static long branchSwapCooldown = 172800000; //2 days by default
    private static boolean taxesEnabled;
    private static String king;
    private static String discord;
    private static final Map<Material, Integer> defaultItemPrices = new HashMap<>();
    private Integer maxMoneyAmount;

    private Set<Player> sctPlayers = new HashSet<>();

    private static CrownAnnouncer announcer;
    private static CrownBalances balFile;
    private static CrownBlackMarket bm;
    private static RoyalBrigadier brigadier;

    private static Timer saver;

    public static final Set<CrownSignShop> LOADED_SHOPS = new HashSet<>();
    public static final Set<FtcUser> LOADED_USERS = new HashSet<>();
    public static final Set<ArmorStandLeaderboard> LEADERBOARDS = new HashSet<>();

    public static NamespacedKey SHOP_KEY;

    private static CrownWorldGuard crownWorldGuard;

    @Override
    public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        announcer = new CrownAnnouncer();
        balFile = new CrownBalances(this);
        bm = new CrownBlackMarket(this);
        brigadier = new RoyalBrigadier(this);

        SHOP_KEY = new NamespacedKey(this, "signshop");

        registerEvents();
        if(getConfig().getBoolean("System.run-deleter-on-startup")) new FileChecker(getDataFolder());

        File file = new File("plugins/DataPlugin/config.yml");
        if(file.exists()) new UserDataConverter(file);
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
        if(saver != null){
            saver.cancel();
            saver.purge();
        }

        for (Player p: Bukkit.getOnlinePlayers()){
            p.closeInventory();
        }

        for (ArmorStandLeaderboard a: LEADERBOARDS){
            a.destroy();
        }
    }

    private void registerEvents(){
        Server server = getServer();

        server.getPluginManager().registerEvents(new JeromeEvent(), this);

        server.getPluginManager().registerEvents(new CoreListener(), this);
        server.getPluginManager().registerEvents(new ChatEvents(), this);

        server.getPluginManager().registerEvents(new SignShopCreateEvent(), this);
        server.getPluginManager().registerEvents(new SignShopInteractEvent(), this);
        server.getPluginManager().registerEvents(new SignShopDestroyEvent(), this);
        server.getPluginManager().registerEvents(new ShopUseListener(), this);

        server.getPluginManager().registerEvents(new BlackMarketEvents(), this);
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

            defaultItemPrices.put(mat, itemPrices.getInt(s));
        }
    }

    //every hour it saves everything
    private void periodicalSave(){
        saver = new Timer();
        final long interval = getConfig().getInt("System.save-interval-mins")*60000;

        saver.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                saveFTC();
            }
        }, interval, interval);
    }

    @Override
    public void saveConfig() {
        getInstance().getConfig().set("King", king);

        super.saveConfig();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        prefix = getConfig().getString("Prefix");
        discord = getConfig().getString("Discord");
        userDataResetInterval = getConfig().getLong("UserDataResetInterval");
        taxesEnabled = getConfig().getBoolean("Taxes");
        maxMoneyAmount = getConfig().getInt("MaxMoneyAmount");
        branchSwapCooldown = getConfig().getLong("BranchSwapCooldown");

        loadDefaultItemPrices();

        if(!getConfig().getString("King").contains("empty")) king = getConfig().getString("King");
        else king = "empty"; //like my soul

        if(getConfig().getBoolean("System.save-periodically")) periodicalSave();
        else if (saver != null){
            saver.cancel();
            saver.purge();
        }
    }

    public static void saveFTC(){
        for(FtcUser data : LOADED_USERS) {
            data.save();
        }

        getAnnouncer().save();

        for(CrownSignShop shop : LOADED_SHOPS){
            try {
                shop.save();
            } catch (Exception ignored) {}
        }
        getBalances().save();
        getBlackMarket().save();

        getInstance().saveConfig();
        Announcer.log(Level.INFO, "FTC-Core saved");
    }

    public static Set<CrownUser> getLoadedUsers(){
        return new HashSet<>(LOADED_USERS);
    }

    public static Set<CrownUser> getOnlineUsers(){
        Set<CrownUser> temp = new HashSet<>();
        for (FtcUser u: LOADED_USERS){
            if(u.isOnline()) temp.add(u);
        }
        return temp;
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
            getUser(getKing()).clearTabPrefix();
            king = "empty";
        }
        else king = newKing.toString();
    }

    public Map<Material, Integer> getItemPrices(){ //returns the default item Price Map
        return defaultItemPrices;
    }
    public Integer getItemPrice(Material material){ //Returns the default price for an item
        return defaultItemPrices.getOrDefault(material, 2);
    }

    public static Set<Player> getSCTPlayers(){ //gets a list of all the players, whose messages will always go to staffchat
        return getInstance().sctPlayers;
    }
    public static void setSCTPlayers(Set<Player> sctPlayers){
        getInstance().sctPlayers = sctPlayers;
    }

    public static String getDiscord(){ //gets and sets the discord link
        return CrownUtils.translateHexCodes(discord);
    }

    public static String getPrefix(){
        return CrownUtils.translateHexCodes(prefix);
    }

    public static long getUserDataResetInterval(){
        return userDataResetInterval;
    }

    public static boolean areTaxesEnabled(){
        return taxesEnabled;
    }

    public static Integer getMaxMoneyAmount(){
        return instance.maxMoneyAmount;
    }

    public static long getBranchSwapCooldown() {
        return branchSwapCooldown;
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
    public static RoyalBrigadier getRoyalBrigadier(){
        return brigadier;
    }


    public static SignShop getShop(Location signShop) { //gets a signshop, throws a null exception if the shop file doesn't exist
        SignShop toReturn = null;

        for(CrownSignShop shop : LOADED_SHOPS){
            if(shop.getLocation().equals(signShop)){
                toReturn = shop;
                break;
            }
        }
        if(toReturn == null){
            try {
                toReturn = new CrownSignShop(signShop);
            } catch (Exception e){
                Announcer.log(Level.SEVERE, e.getMessage());
                toReturn = null;
            }
        }

        return toReturn;
    }
    public static SignShop createSignShop(Location location, ShopType shopType, Integer price, UUID ownerUUID){ //creates a signshop
        return new CrownSignShop(location, shopType, price, ownerUUID);
    }

    public static CrownUser getUser(Player base){
        return getUser(base.getUniqueId());
    }

    public static CrownUser getUser(OfflinePlayer base){
        return getUser(base.getUniqueId());
    }

    public static CrownUser getUser(@NotNull UUID base) {
        Validate.notNull(base, "UUID cannot be null");
        for (CrownUser data : LOADED_USERS) if(data.getBase().equals(base)) return data;
        return new FtcUser(base);
    }

    public static CrownUser getUser(String name){
        return getUser(getOffOnUUID(name));
    }

    public static UUID getOffOnUUID(String playerName){
        UUID toReturn;
        try{
            toReturn = Bukkit.getPlayerExact(playerName).getUniqueId();
        } catch (NullPointerException e){
            try {
                toReturn = Bukkit.getOfflinePlayerIfCached(playerName).getUniqueId();
            } catch (Exception ignored){ toReturn = null; }
        }
        return toReturn;
    }
}

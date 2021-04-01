package net.forthecrown.core;

import net.forthecrown.core.api.*;
import net.forthecrown.core.commands.brigadier.RoyalBrigadier;
import net.forthecrown.core.crownevents.ArmorStandLeaderboard;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.events.*;
import net.forthecrown.core.events.npc.JeromeEvent;
import net.forthecrown.core.files.*;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
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
    private static final Map<Material, Short> defaultItemPrices = new HashMap<>();
    private static int saverID;
    private Integer maxMoneyAmount;

    private static CrownAnnouncer announcer;
    private static CrownBalances balFile;
    private static CrownBlackMarket bm;
    private static RoyalBrigadier brigadier;
    private static CrownWorldGuard crownWorldGuard;

    public static final Map<Location, CrownSignShop> LOADED_SHOPS = new HashMap<>();
    public static final Map<UUID, FtcUser> LOADED_USERS = new HashMap<>();
    public static final Set<ArmorStandLeaderboard> LEADERBOARDS = new HashSet<>();
    public static final LuckPerms LUCK_PERMS = LuckPermsProvider.get();

    public static NamespacedKey SHOP_KEY;

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

            defaultItemPrices.put(mat, (short) itemPrices.getInt(s));
        }
    }

    //every hour it saves everything
    private void periodicalSave(){
        final long interval = getConfig().getLong("System.save-interval-mins")*60*20;
        saverID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, FtcCore::saveFTC, interval, interval);
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
        else if (saverID != 0){
            Bukkit.getScheduler().cancelTask(saverID);
            saverID = 0;
        }
    }

    public static void saveFTC(){
        for(FtcUser data : LOADED_USERS.values()) {
            data.save();
        }

        getAnnouncer().save();

        for(CrownSignShop shop : LOADED_SHOPS.values()){
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
        return new HashSet<>(LOADED_USERS.values());
    }

    public static Set<CrownUser> getOnlineUsers(){
        return ListUtils.convertToSet(Bukkit.getOnlinePlayers(), FtcCore::getUser);
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

    public static Map<Material, Short> getItemPrices(){ //returns the default item Price Map
        return defaultItemPrices;
    }
    public static Short getItemPrice(Material material){ //Returns the default price for an item
        return defaultItemPrices.getOrDefault(material, (short) 2);
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

    public static boolean logAdminShopUsage(){
        return getInstance().getConfig().getBoolean("Shops.log-admin-purchases");
    }

    public static boolean logNormalShopUsage(){
        return getInstance().getConfig().getBoolean("Shops.log-normal-purchases");
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
    public static CrownWorldGuard getCrownWorldGuard() {
        return crownWorldGuard;
    }

    public static SignShop getShop(Location signShop) { //gets a signshop, throws a null exception if the shop file doesn't exist
        if(LOADED_SHOPS.containsKey(signShop)) return LOADED_SHOPS.get(signShop);

        try {
            return new CrownSignShop(signShop);
        } catch (Exception e){
            Announcer.log(Level.SEVERE, e.getMessage());
        }

        return null;
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
        if(LOADED_USERS.containsKey(base)) return LOADED_USERS.get(base);
        return new FtcUser(base);
    }

    public static CrownUser getUser(String name){
        return getUser(getOffOnUUID(name));
    }

    public static UUID getOffOnUUID(String playerName){
        try{
            return Bukkit.getPlayerExact(playerName).getUniqueId();
        } catch (NullPointerException e){
            try {
                return Bukkit.getOfflinePlayerIfCached(playerName).getUniqueId();
            } catch (Exception ignored){ return null; }
        }
    }
}

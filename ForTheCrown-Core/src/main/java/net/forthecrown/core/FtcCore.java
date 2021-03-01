package net.forthecrown.core;

import net.forthecrown.core.api.*;
import net.forthecrown.core.commands.*;
import net.forthecrown.core.commands.brigadier.RoyalBrigadier;
import net.forthecrown.core.commands.emotes.*;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.events.*;
import net.forthecrown.core.events.npc.JeromeEvent;
import net.forthecrown.core.files.*;
import net.forthecrown.core.inventories.CustomInventoryHolder;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

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

    private static final Map<Material, Integer> defaultItemPrices = new HashMap<>();
    private Integer maxMoneyAmount;

    private Set<Player> sctPlayers = new HashSet<>();
    private static final Set<Player> onCooldown = new HashSet<>();
    private static String discord;

    private CrownAnnouncer autoAnnouncer;
    private CrownBalances balFile;
    private CrownBlackMarket bm;

    private static Timer saver;

    public static final Set<CrownSignShop> loadedShops = new HashSet<>();
    public static final Set<FtcUser> loadedUsers = new HashSet<>();

    private RoyalBrigadier brigadier;

    @Override
    public void onEnable() {
        instance = this;
        brigadier = new RoyalBrigadier(this);

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        prefix = getConfig().getString("Prefix");
        userDataResetInterval = getConfig().getLong("UserDataResetInterval");
        taxesEnabled = getConfig().getBoolean("Taxes");
        discord = getConfig().getString("Discord");
        maxMoneyAmount = getConfig().getInt("MaxMoneyAmount");
        loadDefaultItemPrices();

        autoAnnouncer = new CrownAnnouncer();
        balFile = new CrownBalances();
        bm = new CrownBlackMarket();

        if (!getConfig().getString("King").contains("empty")) king = getConfig().getString("King");
        else king = null;

        Server server = getServer();

        //events
        server.getPluginManager().registerEvents(new JeromeEvent(), this);

        server.getPluginManager().registerEvents(new CoreListener(), this);
        server.getPluginManager().registerEvents(new ChatEvents(), this);

        server.getPluginManager().registerEvents(new SignShopCreateEvent(), this);
        server.getPluginManager().registerEvents(new SignShopInteractEvent(), this);
        server.getPluginManager().registerEvents(new SignShopDestroyEvent(), this);
        server.getPluginManager().registerEvents(new ShopUseListener(), this);

        server.getPluginManager().registerEvents(new BlackMarketEvents(), this);

        doCommands();

        if(getConfig().getBoolean("System.save-periodically")) periodicalSave();
        if(getConfig().getBoolean("System.run-deleter-on-startup")) new FileDeleter(getDataFolder());
    }

    private void doCommands(){
        new KingMakerCommand();

        new BecomeBaronCommand();
        new RankCommand();

        new CoreCommand();

        new BalanceCommand();
        new BalanceTopCommand();

        new PayCommand();
        new AddBalanceCommand();
        new SetBalanceCommand();
        new ResetBalance();

        new GemsCommand();
        new ProfileCommand();

        new ShopCommand();
        //new ShopEditCommand();

        new WithdrawCommand();
        new DepositCommand();

        new StaffChatCommand();
        new StaffChatToggleCommand();

        new BroadcastCommand();

        new Discord();
        new FindPost();
        new PostHelp();
        new SpawnCommand();
        new MapCommand();

        new TpaskCommand();
        new TpaskHereCommand();

        new ToggleEmotes();
        new Bonk();
        new Mwah();
        new Poke();
        new Scare();
        new Jingle();

        new Hug();
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
            InventoryHolder holder = p.getOpenInventory().getTopInventory().getHolder();
            if(holder instanceof CustomInventoryHolder || holder instanceof SignShop){
                p.closeInventory();
            }
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

        if(!getConfig().getString("King").contains("empty")) king = getConfig().getString("King");
        else king = "empty"; //like my soul

        if(getConfig().getBoolean("System.save-periodically")) periodicalSave();
        else if (saver != null){
            saver.cancel();
            saver.purge();
        }
    }

    public static void reloadFTC(){
        getInstance().reloadConfig();

        getAnnouncer().reload();
        for (FtcUser data : loadedUsers){
            data.reload();
        }

        getInstance().loadDefaultItemPrices();
        for(CrownSignShop shop : loadedShops){
            shop.reload();
        }
        getBalances().reload();

        getBlackMarket().reload();
    }
    public static void saveFTC(){
        for(FtcUser data : loadedUsers) {
            data.save();
        }

        getAnnouncer().save();

        for(CrownSignShop shop : loadedShops){
            try {
                shop.save();
            } catch (Exception ignored) {}
        }
        getBalances().save();
        getBlackMarket().save();

        getInstance().saveConfig();
        FtcCore.getInstance().getLogger().log(Level.INFO, "FTC-Core saved");
    }

    public static Set<CrownUser> getLoadedUsers(){
        Set<CrownUser> temp = new HashSet<>();
        temp.addAll(loadedUsers);
        return temp;
    }

    public static Set<CrownUser> getOnlineUsers(){
        Set<CrownUser> temp = new HashSet<>();
        for (FtcUser u: loadedUsers){
            if(u.isOnline()) temp.add(u);
        }
        return temp;
    }

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
        if(newKing == null) king = "empty";
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
        return getInstance().autoAnnouncer;
    }
    public static Balances getBalances(){
        return getInstance().balFile;
    }
    public static BlackMarket getBlackMarket() {
        return getInstance().bm;
    }


    public static SignShop getShop(Location signShop) { //gets a signshop, throws a null exception if the shop file doesn't exist
        SignShop toReturn = null;

        for(CrownSignShop shop : loadedShops){
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

    public static CrownUser getUser(UUID base) {
        for (CrownUser data : loadedUsers) if(base == data.getBase()) return data;
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
}

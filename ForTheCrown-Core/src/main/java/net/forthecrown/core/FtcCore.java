package net.forthecrown.core;

import net.forthecrown.core.api.*;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.commands.*;
import net.forthecrown.core.commands.emotes.*;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.events.*;
import net.forthecrown.core.events.npc.JeromeEvent;
import net.forthecrown.core.files.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FtcCore extends JavaPlugin {

    private static FtcCore instance;
    private static String prefix = "&6[FTC]&r  ";
    private static long userDataResetInterval = 5356800000L; //2 months by default
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

    @Override
    public void onEnable() {
        instance = this;

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
        server.getPluginManager().registerEvents(new RankGuiUseEvent(), this);
        server.getPluginManager().registerEvents(new ChatEvents(), this);

        server.getPluginManager().registerEvents(new SellShopEvents(), this);
        server.getPluginManager().registerEvents(new SignShopCreateEvent(), this);
        server.getPluginManager().registerEvents(new SignShopInteractEvent(), this);
        server.getPluginManager().registerEvents(new SignShopDestroyEvent(), this);
        server.getPluginManager().registerEvents(new ShopUseListener(), this);

        server.getPluginManager().registerEvents(new BlackMarketEvents(), this);

        //commands
        doCommands();

        if(getConfig().getBoolean("System.save-periodically")) periodicalSave();
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

        new GemsCommand();

        new ShopCommand();
        //new ShopEditCommand();

        new WithdrawCommand();
        new DepositCommand();

        getCommand("staffchat").setExecutor(new StaffChatCommand());
        new StaffChatToggleCommand();

        new BroadcastCommand();

        new Discord();
        new FindPost();
        new PostHelp();
        new SpawnCommand();

        new TpaskCommand();
        new TpaskHereCommand();

        new ToggleEmotes();
        new Bonk();
        new Mwah();
        new Poke();
        new Scare();
        new Jingle();
    }

    @Override
    public void onDisable() {
        if(getConfig().getBoolean("System.save-on-disable")) saveFTC();
    }

    //every hour it saves everything
    private void periodicalSave(){
        saver = new Timer();
        final long interval = getConfig().getInt("System.save-interval-mins")*60*1000;

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
        getInstance().getConfig().set("Taxes", taxesEnabled);
        getInstance().getConfig().set("Prefix", prefix);

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
            if(!shop.wasDeleted()) shop.save();
        }
        getBalances().save();
        getBlackMarket().save();

        getInstance().saveConfig();
        System.out.println("[SAVED] FtcCore saved");
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
        return defaultItemPrices.get(material);
    }

    public static Set<Player> getSCTPlayers(){ //gets a list of all the players, whose messages will always go to staffchat
        return getInstance().sctPlayers;
    }
    public static void setSCTPlayers(Set<Player> sctPlayers){
        getInstance().sctPlayers = sctPlayers;
    }

    public static String getDiscord(){ //gets and sets the discord link
        return translateHexCodes(discord);
    }

    public static String getPrefix(){
        return FtcCore.translateHexCodes(prefix);
    }

    public static long getUserDataResetInterval(){
        return userDataResetInterval;
    }

    public static boolean areTaxesEnabled(){
        return taxesEnabled;
    }

    public static Integer getMaxMoneyAmount(){
        return getInstance().maxMoneyAmount;
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

    //Yeah, no clue
    public static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    public static String translateHexCodes (String textToTranslate) {

        Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
        StringBuffer buffer = new StringBuffer();

        while(matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static boolean isOnCooldown(Player player){
        return onCooldown.contains(player);
    }
    public static void addToCooldown(Player player, int timeinDelay, boolean permissionIgnore){
        if(player.hasPermission("ftc.cooldownignore") && permissionIgnore) return;

        onCooldown.add(player);
        Bukkit.getScheduler().runTaskLater(getInstance(), () -> onCooldown.remove(player), timeinDelay);
    }

    public static String replaceEmojis(String string){ //replaces every emoji in the given string
        String message = string;
        message = message.replaceAll(":shrug:", "¯\\\\_(ツ)_/¯");
        message = message.replaceAll(":ughcry:", "(ಥ﹏ಥ)");
        message = message.replaceAll(":hug:", "༼ つ ◕_◕ ༽つ");
        message = message.replaceAll(":hugcry:", "༼ つ ಥ_ಥ ༽つ");
        message = message.replaceAll(":bear:", "ʕ• ᴥ •ʔ");
        message = message.replaceAll(":smooch:", "( ^ 3^) ❤");
        message = message.replaceAll(":why:", "ლ(ಠ益ಠლ)");
        message = message.replaceAll(":tableflip:", "(ノಠ益ಠ)ノ彡┻━┻");
        message = message.replaceAll(":tableput:", " ┬──┬ ノ( ゜-゜ノ)");
        message = message.replaceAll(":pretty:", "(◕‿◕ ✿)");
        message = message.replaceAll(":sparkle:", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧");
        message = message.replaceAll(":blush:", "(▰˘◡˘▰)");
        message = message.replaceAll(":sad:", "(._. )");
        message = message.replaceAll(":pleased:", "(ᵔᴥᵔ)");
        message = message.replaceAll(":fedup:", "(¬_¬)");
        return message;
    }

    public static void senderEmoteOffMessage(Player player){ //Honestly I just got bored of copy pasting this
        player.sendMessage(ChatColor.GRAY + "You have emotes turned off.");
        player.sendMessage(ChatColor.GRAY + "Do " + ChatColor.RESET + "/toggleemotes" + ChatColor.GRAY + " to enable them.");
    }

    public static SignShop getShop(Location signShop) throws Exception { //gets a signshop, throws a null exception if the shop file doesn't exist
        for(CrownSignShop shop : loadedShops) if(shop.getLocation().equals(signShop)) return shop;
        return new CrownSignShop(signShop);
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

    public static UUID getOffOnUUID(String playerName){
        UUID toReturn;
        try{
            toReturn = Bukkit.getPlayer(playerName).getUniqueId();
        } catch (NullPointerException e){
            try {
                toReturn = Bukkit.getOfflinePlayerIfCached(playerName).getUniqueId();
            } catch (NullPointerException e1){ toReturn = null;}
        }
        return toReturn;
    }

    public static Integer getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            return 0;
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static ItemStack makeItem(Material material, int amount, boolean hideFlags, String name, String... loreStrings) {
        ItemStack result = new ItemStack(material, amount);
        ItemMeta meta = result.getItemMeta();

        if (name != null) meta.setDisplayName(ChatColor.RESET + translateHexCodes(name));
        if (loreStrings != null) {
            List<String> lore = new ArrayList<>();
            for(String s : loreStrings){ lore.add(ChatColor.RESET + translateHexCodes(s)); }
            meta.setLore(lore);
        }
        if (hideFlags) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        }

        result.setItemMeta(meta);
        return result;
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

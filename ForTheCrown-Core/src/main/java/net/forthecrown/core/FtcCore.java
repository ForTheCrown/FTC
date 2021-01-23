package net.forthecrown.core;

import net.forthecrown.core.commands.*;
import net.forthecrown.core.commands.emotes.*;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.events.*;
import net.forthecrown.core.events.npc.JeromeEvent;
import net.forthecrown.core.files.AutoAnnouncer;
import net.forthecrown.core.files.Balances;
import net.forthecrown.core.files.FtcUser;
import net.forthecrown.core.files.SignShop;
import net.forthecrown.core.clickevent.ClickEventCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FtcCore extends JavaPlugin {

    private static FtcCore instance;
    private static String prefix = "&6[FTC]&r  ";
    private static long userDataResetInterval = 5356800000L; //2 months by default
    private static boolean taxesEnabled = true;
    private static UUID king;

    private static final Map<Material, Integer> defaultItemPrices = new HashMap<>();
    private Integer maxMoneyAmount;

    private Set<Player> sctPlayers = new HashSet<>();
    private static final Set<Player> onCooldown = new HashSet<>();
    private static String discord;

    private AutoAnnouncer autoAnnouncer;
    private Balances balFile;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        getServer().getPluginCommand("npcconverse").setExecutor(new ClickEventCommand());

        prefix = getConfig().getString("Prefix");
        userDataResetInterval = getConfig().getLong("UserDataResetInterval");
        taxesEnabled = getConfig().getBoolean("Taxes");
        discord = FtcCore.getInstance().getConfig().getString("Discord");
        autoAnnouncer = new AutoAnnouncer();
        balFile = new Balances();
        maxMoneyAmount = FtcCore.getInstance().getConfig().getInt("MaxMoneyAmount");
        loadDefaultItemPrices();

        if(!getConfig().getString("King").contains("empty")) king = UUID.fromString(getConfig().getString("King"));
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

        //commands
        server.getPluginCommand("kingmaker").setExecutor(new KingMakerCommand());
        server.getPluginCommand("kingmaker").setTabCompleter(new KingMakerTabCompleter());

        server.getPluginCommand("leavevanish").setExecutor(new LeaveVanishCommand());
        server.getPluginCommand("becomebaron").setExecutor(new BecomeBaronCommand());
        server.getPluginCommand("rank").setExecutor(new RankCommand());
        server.getPluginCommand("ftccore").setExecutor(new CoreCommand());
        server.getPluginCommand("ftccore").setTabCompleter(new CoreTabCompleter());

        server.getPluginCommand("balance").setExecutor(new BalanceCommand());
        server.getPluginCommand("balancetop").setExecutor(new BalanceTopCommand());

        server.getPluginCommand("pay").setExecutor(new PayCommand());
        server.getPluginCommand("addbalance").setExecutor(new AddBalanceCommand());
        server.getPluginCommand("setbalance").setExecutor(new SetBalanceCommand());

        server.getPluginCommand("shop").setExecutor(new ShopCommand());
        server.getPluginCommand("shop").setTabCompleter(new ShopTabCompleter());

        server.getPluginCommand("editshop").setExecutor(new ShopEditCommand());
        server.getPluginCommand("editshop").setTabCompleter(new ShopEditTabCompleter());

        server.getPluginCommand("staffchat").setExecutor(new StaffChatCommand());
        server.getPluginCommand("staffchat").setTabCompleter(new EmojiTabCompleter());
        server.getPluginCommand("staffchattoggle").setExecutor(new StaffChatToggleCommand());

        server.getPluginCommand("broadcast").setExecutor(new BroadcastCommand());

        server.getPluginCommand("discord").setExecutor(new Discord());
        server.getPluginCommand("findpost").setExecutor(new  FindPost());
        server.getPluginCommand("posthelp").setExecutor(new PostHelp());
        server.getPluginCommand("spawn").setExecutor(new SpawnCommand());

        server.getPluginCommand("tpask").setExecutor(new  TpaskCommand());
        server.getPluginCommand("tpaskhere").setExecutor(new  TpaskHereCommand());

        server.getPluginCommand("toggleemotes").setExecutor(new ToggleEmotes());
        server.getPluginCommand("bonk").setExecutor(new Bonk());
        server.getPluginCommand("mwah").setExecutor(new Mwah());
        server.getPluginCommand("poke").setExecutor(new Poke());
        server.getPluginCommand("scare").setExecutor(new Scare());
        server.getPluginCommand("jingle").setExecutor(new Jingle());

        periodicalSave();
    }


    @Override
    public void onDisable() {
        saveFTC();
    }

    //every hour it saves everything
    private void periodicalSave(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, FtcCore::saveFTC, 72000, 72000);
    }

    public static void reloadFTC(){
        getAnnouncer().reload();
        for (FtcUser data : FtcUser.loadedData){
            data.reload();
        }

        getInstance().loadDefaultItemPrices();
        for(SignShop shop : SignShop.loadedShops){
            shop.reload();
        }
        getBalances().reload();
        getInstance().maxMoneyAmount = FtcCore.getInstance().getConfig().getInt("MaxMoneyAmount");

        prefix = getInstance().getConfig().getString("Prefix");
        discord = FtcCore.getInstance().getConfig().getString("Discord");
        userDataResetInterval = getInstance().getConfig().getLong("UserDataResetInterval");
        taxesEnabled = getInstance().getConfig().getBoolean("Taxes");

        if(!getInstance().getConfig().getString("King").contains("empty")) king = UUID.fromString(getInstance().getConfig().getString("King"));
        else king = null;
    }
    public static void saveFTC(){
        for(FtcUser data : FtcUser.loadedData){
            data.save();
        }
        getInstance().getConfig().set("prefix", prefix);
        getInstance().saveConfig();

        getAnnouncer().save();

        for(SignShop shop : SignShop.loadedShops){
            if(!shop.wasDeleted()) shop.save();
        }
        getBalances().save();

        if(getKing() == null) getInstance().getConfig().set("King", "empty");
        else getInstance().getConfig().set("King", getKing().toString());

        System.out.println("[SAVED] FtcCore saved");
    }


    public Set<FtcUser> getOnlineUsers(){
        return FtcUser.loadedData;
    }

    public static UUID getKing() {
        return king;
    }

    public static void setKing(UUID king) {
        FtcCore.king = king;
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
        return translateHexCodes(prefix);
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
    public static AutoAnnouncer getAnnouncer(){
        return getInstance().autoAnnouncer;
    }
    public static Balances getBalances(){ //The only way to get the balances class
        return getInstance().balFile;
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

    public static SignShop getSignShop(Location signShop) throws NullPointerException { //gets a signshop, throws a null exception if the shop file doesn't exist
        for(SignShop shop : SignShop.loadedShops){
            if(shop.getLocation() == signShop) return shop;
        }
        return new SignShop(signShop);
    }
    public static SignShop createSignShop(Location location, ShopType shopType, Integer price, UUID ownerUUID){ //creates a signshop
        return new SignShop(location, shopType, price, ownerUUID);
    }

    public static FtcUser getUser(UUID base) {
        for (FtcUser data : FtcUser.loadedData){
            if(base == data.getBase()) return data;
        }
        return new FtcUser(base);
    }

    public static UUID getOffOnUUID(String playerName){
        UUID toReturn;
        try{
            toReturn = Bukkit.getPlayer(playerName).getUniqueId();
        } catch (NullPointerException e){
            try {
                toReturn = Bukkit.getOfflinePlayerIfCached(playerName).getUniqueId();
            } catch (NullPointerException e1){
                toReturn = null;
            }
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

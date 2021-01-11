package net.forthecrown.core;

import net.forthecrown.core.chat.Chat;
import net.forthecrown.core.commands.BecomeBaronCommand;
import net.forthecrown.core.commands.FtcReloadCommand;
import net.forthecrown.core.commands.LeaveVanishCommand;
import net.forthecrown.core.economy.Economy;
import net.forthecrown.core.files.AutoAnnouncer;
import net.forthecrown.core.files.FtcUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class FtcCore extends JavaPlugin {

    private static FtcCore instance;
    private AutoAnnouncer autoAnnouncer;
    private static String prefix = "&6[FTC]&r  ";
    private static Chat chatMain;
    private static Economy economyMain;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        prefix = getConfig().getString("prefix");

        chatMain = new Chat();
        economyMain = new Economy();
        autoAnnouncer = new AutoAnnouncer();

        getServer().getPluginManager().registerEvents(new CoreListener(), this);

        getServer().getPluginCommand("ftcreload").setExecutor(new FtcReloadCommand());
        getServer().getPluginCommand("leavevanish").setExecutor(new LeaveVanishCommand());
        getServer().getPluginCommand("becomebaron").setExecutor(new BecomeBaronCommand());
        //getServer().getPluginCommand("ftccore").setExecutor(new DataCommand());
        //getServer().getPluginCommand("ftccore").setTabCompleter(new DataTabCompleter());

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
        getEconomy().reloadEconomy();
        for (FtcUser data : FtcUser.loadedData){
            data.reload();
        }
        prefix = getInstance().getConfig().getString("prefix");
        Chat.setDiscord(getInstance().getConfig().getString("discord"));
    }

    public static void saveFTC(){
        Economy.saveEconomy();
        for(FtcUser data : FtcUser.loadedData){
            data.save();
        }
        getInstance().getConfig().set("prefix", prefix);
        getInstance().getConfig().set("sctPlayers", Chat.getSCTPlayers());
        getInstance().saveConfig();

        getAnnouncer().save();

        System.out.println("[SAVED] FtcCore saved");
    }

    public static String getPrefix(){
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }


    //get a part of the plugin with these
    public static AutoAnnouncer getAnnouncer(){
        return getInstance().autoAnnouncer;
    }

    public static Chat getChat(){
        return chatMain;
    }

    public static Economy getEconomy(){
        return economyMain;
    }

    public static FtcCore getInstance(){
        return instance;
    }

    public static FtcUser getUserData(UUID base) {
        for (FtcUser data : FtcUser.loadedData){
            if(base == data.getBase()) return data;
        }
        return new FtcUser(base);
    }

    public static UUID getOffOnUUID(String playerName){
        UUID toReturn;
        try{
            toReturn = Bukkit.getPlayer(playerName).getUniqueId();
        } catch (Exception e){
            try {
                toReturn = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            } catch (Exception e1){
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

        if (name != null) meta.setDisplayName(ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', name));
        if (loreStrings != null) {
            List<String> lore = new ArrayList<>();
            for(String s : loreStrings){
                lore.add(ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', s));
            }
            meta.setLore(lore);
        }
        if (hideFlags) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        }

        result.setItemMeta(meta);
        return result;
    }
}

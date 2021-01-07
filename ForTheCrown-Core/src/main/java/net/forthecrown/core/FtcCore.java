package net.forthecrown.core;

import net.forthecrown.core.chat.Chat;
import net.forthecrown.core.commands.FtcReloadCommand;
import net.forthecrown.core.economy.Economy;
import net.forthecrown.core.files.AutoAnnouncer;
import net.forthecrown.core.files.FtcUserData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

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
        //getServer().getPluginCommand("data").setExecutor(new DataCommand());
        //getServer().getPluginCommand("data").setTabCompleter(new DataTabCompleter());
    }

    @Override
    public void onDisable() {

        saveFTC();
    }


    public static void reloadFTC(){
        getAnnouncer().reload();
        getEconomy().reloadEconomy();
        for (FtcUserData data : FtcUserData.loadedData){
            data.reload();
        }
        prefix = getInstance().getConfig().getString("prefix");
        Chat.setDiscord(getInstance().getConfig().getString("discord"));
    }
    public static void reloadPlugin() throws InterruptedException {
        getInstance().getServer().getPluginManager().disablePlugin(getInstance(), true);

        ((Runnable) () -> getInstance().getServer().getPluginManager().enablePlugin(getInstance())).wait(1000);
    }
    public static void saveFTC(){
        Economy.saveEconomy();
        for(FtcUserData data : FtcUserData.loadedData){
            data.save();
        }
        getInstance().getConfig().set("prefix", prefix);
        getInstance().getConfig().set("sctPlayers", Chat.getSCTPlayers());
        getInstance().saveConfig();

        getAnnouncer().save();
    }

    public static String getPrefix(){
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }

    public static AutoAnnouncer getAnnouncer(){
        return getInstance().autoAnnouncer;
    }

    //get a part of the plugin with these
    public static Chat getChat(){
        return chatMain;
    }

    public static Economy getEconomy(){
        return economyMain;
    }

    public static FtcCore getInstance(){
        return instance;
    }

    public static FtcUserData getUserData(UUID base) {
        for (FtcUserData data : FtcUserData.loadedData){
            if(base == data.getBase()) return data;
        }
        return new FtcUserData(base);
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
}

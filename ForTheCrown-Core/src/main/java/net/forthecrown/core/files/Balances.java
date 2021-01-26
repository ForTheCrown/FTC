package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Balances extends FtcFileManager {

    private Map<UUID, Integer> balanceMap = new HashMap<>(); //this is how all the balances are stored, in a private Map
    private int startRhines;

    public Balances() { //This class should only get constructed once, in the main class on startup
        super("balance");

        startRhines = FtcCore.getInstance().getConfig().getInt("StartRhines");

        if(legacyFileExists()) convertFromLegacy();
        else reload();
    }

    public Map<UUID, Integer> getBalanceMap(){
        return balanceMap;
    }
    public void setBalanceMap(Map<UUID, Integer> balanceMap){
        this.balanceMap = balanceMap;
    }

    public Integer getBalance(UUID uuid){
        if(balanceMap.containsKey(uuid)) return balanceMap.getOrDefault(uuid, startRhines);
        return 100;
    }
    public void setBalance(UUID uuid, Integer amount){
        if(amount >= FtcCore.getMaxMoneyAmount()){
            System.out.println(ChatColor.YELLOW + "[WARNING] " + uuid.toString() + " is on or over the maximum balance limit!");
            balanceMap.put(uuid, FtcCore.getMaxMoneyAmount());
            return;
        }

        balanceMap.put(uuid, amount);
    }

    public void addBalance(UUID uuid, Integer amount){
        addBalance(uuid, amount, false);
    }
    public void addBalance(UUID uuid, Integer amount, boolean isTaxed){
        if(amount + getBalance(uuid) >= FtcCore.getMaxMoneyAmount()){
            System.out.println(ChatColor.YELLOW + "[Warning] " + uuid.toString() + " / " + Bukkit.getOfflinePlayer(uuid).getName() + " is on or over the maximum balance limit!");
            balanceMap.put(uuid, FtcCore.getMaxMoneyAmount());
            return;
        }

        FtcCore.getUser(uuid).addTotalEarnings(amount);

        if(FtcCore.areTaxesEnabled() && isTaxed && getTaxPercentage(uuid) > 1){
            int amountToRemove = (int) (amount * ((float) getTaxPercentage(uuid)/100));
            amount -= amountToRemove;

            FtcCore.getUser(uuid).sendMessage("&7You were taxed " + getTaxPercentage(uuid) + "%, which means you lost " + amountToRemove + " Rhines of your last transaction");
        }

        balanceMap.put(uuid, getBalance(uuid) + amount);
    }

    public Integer getTaxPercentage(UUID uuid){
        if(getBalance(uuid) < 500000) return 0; //if the player has less thank 500k rhines, no tax

        int percent = (int) (FtcCore.getUser(uuid).getTotalEarnings() / 50000 * 10);
        if(percent >= 30) return 50;
        return percent;
    }

    @Override
    public void save(){
        for(UUID id : balanceMap.keySet()){
            if(balanceMap.get(id) != startRhines) getFile().set(id.toString(), getBalanceMap().get(id));
        }
        super.save();
    }

    @Override
    public void reload(){
        super.reload();
        startRhines = FtcCore.getInstance().getConfig().getInt("StartRhines");
        for(String string : getFile().getKeys(true)){
            UUID id;
            try {
                id = UUID.fromString(string);
            } catch (Exception e){
                e.printStackTrace();
                continue;
            }

            int balance = getFile().getInt(string);
            balanceMap.put(id, balance);
        }
    }


    private void convertFromLegacy(){ //this actually works :D
        File oldFile = new File("plugins/ShopsReworked/PlayerBalances.yml");
        if(!oldFile.exists()) return;

        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(oldFile);
        ConfigurationSection oldDataSection = fileConfig.getConfigurationSection("PlayerData");

        for(String s : oldDataSection.getKeys(true)) {
            UUID id;
            try {
                id = UUID.fromString(s);
            } catch (Exception e){
                continue;
            }

            if(oldDataSection.getInt(s) > startRhines) balanceMap.put(id, oldDataSection.getInt(s));
        }
        save();

        oldFile.delete();
    }

    private boolean legacyFileExists(){
        File oldFile = new File("plugins/ShopsReworked/PlayerBalances.yml");
        return oldFile.exists();
    }
}

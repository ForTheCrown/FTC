package net.forthecrown.core.economy.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.files.FtcFileManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Balances extends FtcFileManager {

    private Map<UUID, Integer> balanceMap = new HashMap<>();
    private int startRhines;

    public Balances() {
        super("balance", "data");

        startRhines = FtcCore.getInstance().getConfig().getInt("StartRhines");
        if(legacyFileExists()) convertFromLegacy();
        else reload();
    }

    public Map<UUID, Integer> getBalances(){
        return balanceMap;
    }
    public void setBalances(Map<UUID, Integer> balanceMap){
        this.balanceMap = balanceMap;
    }

    public Integer getBalance(UUID uuid){
        if(balanceMap.containsKey(uuid)) return balanceMap.getOrDefault(uuid, startRhines);
        return 100;
    }
    public void setBalance(UUID uuid, Integer amount){
        balanceMap.put(uuid, amount);
    }

    public void save(){
        for(UUID id : balanceMap.keySet()){
            if(balanceMap.get(id) != 100) fileConfig.set(id.toString(), getBalances().get(id));
        }
        super.save();
    }
    public void reload(){
        super.reload();
        startRhines = FtcCore.getInstance().getConfig().getInt("StartRhines");
        for(String string : fileConfig.getKeys(true)){
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


    private void convertFromLegacy(){
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

            if(oldDataSection.getInt(s) > 100) balanceMap.put(id, oldDataSection.getInt(s));
        }
        save();

        oldFile.delete();
    }

    private boolean legacyFileExists(){
        File oldFile = new File("plugins/ShopsReworked/PlayerBalances.yml");
        return oldFile.exists();
    }
}

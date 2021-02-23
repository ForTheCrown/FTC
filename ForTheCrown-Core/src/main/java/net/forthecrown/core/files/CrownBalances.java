package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CrownBalances extends FtcFileManager implements Balances {

    private Map<UUID, Integer> balanceMap = new HashMap<>(); //this is how all the balances are stored, in a private Map
    private int startRhines;

    public CrownBalances() { //This class should only get constructed once, in the main class on startup
        super("balance");

        startRhines = FtcCore.getInstance().getConfig().getInt("StartRhines");

        reload();
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

    @Override
    public Map<UUID, Integer> getBalanceMap(){
        return balanceMap;
    }
    @Override
    public void setBalanceMap(Map<UUID, Integer> balanceMap){
        this.balanceMap = balanceMap;
    }

    @Override
    public Integer getBalance(UUID uuid){
        if(balanceMap.containsKey(uuid)) return balanceMap.getOrDefault(uuid, startRhines);
        return 100;
    }
    @Override
    public void setBalance(UUID uuid, Integer amount){
        if(amount >= FtcCore.getMaxMoneyAmount()){
            FtcCore.getInstance().getLogger().log(Level.WARNING, Bukkit.getOfflinePlayer(uuid).getName() + " has reached the balance limit.");
            balanceMap.put(uuid, FtcCore.getMaxMoneyAmount());
            return;
        }

        balanceMap.put(uuid, amount);
    }

    @Override
    public void addBalance(UUID uuid, Integer amount){
        addBalance(uuid, amount, false);
    }

    @Override
    public void addBalance(UUID uuid, Integer amount, boolean isTaxed){
        if(amount + getBalance(uuid) >= FtcCore.getMaxMoneyAmount()){
            FtcCore.getInstance().getLogger().log(Level.WARNING, Bukkit.getOfflinePlayer(uuid).getName() + " has reached the balance limit.");
            balanceMap.put(uuid, FtcCore.getMaxMoneyAmount());
            return;
        }

        FtcCore.getUser(uuid).addTotalEarnings(amount);

        if(FtcCore.areTaxesEnabled() && isTaxed && getTaxPercentage(uuid) > 1 && amount > 1){
            int amountToRemove = (int) (amount * ((float) getTaxPercentage(uuid)/100));
            amount -= amountToRemove;

            FtcCore.getUser(uuid).sendMessage("&7You were taxed " + getTaxPercentage(uuid) + "%, which means you lost " + amountToRemove + " Rhines of your last transaction");
        }

        balanceMap.put(uuid, getBalance(uuid) + amount);
    }

    @Override
    public Integer getTaxPercentage(UUID uuid){
        if(getBalance(uuid) < 500000) return 0; //if the player has less thank 500k rhines, no tax

        int percent = (int) (FtcCore.getUser(uuid).getTotalEarnings() / 50000 * 10);
        if(percent >= 30) return 50;
        return percent;
    }
}

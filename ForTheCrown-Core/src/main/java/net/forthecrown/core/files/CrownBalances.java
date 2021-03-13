package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.Balances;
import org.bukkit.Bukkit;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CrownBalances extends FtcFileManager<FtcCore> implements Balances {

    private Map<UUID, Integer> balanceMap = new HashMap<>(); //this is how all the balances are stored, in a private Map
    private int startRhines;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public CrownBalances(FtcCore core) { //This class should only get constructed once, in the main class on startup
        super("balance", core);

        startRhines = FtcCore.getInstance().getConfig().getInt("StartRhines");

        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);

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
                Announcer.log(Level.WARNING, string + " is not a valid UUID, reload() in Balance");
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
    public Integer get(UUID uuid){
        if(balanceMap.containsKey(uuid)) return balanceMap.getOrDefault(uuid, startRhines);
        return 100;
    }

    @Override
    public String getDecimalized(UUID id){
        return decimalFormat.format(get(id));
    }

    @Override
    public void set(UUID uuid, Integer amount){
        if(amount >= FtcCore.getMaxMoneyAmount()){
            FtcCore.getInstance().getLogger().log(Level.WARNING, Bukkit.getOfflinePlayer(uuid).getName() + " has reached the balance limit.");
            amount = FtcCore.getMaxMoneyAmount();
        }

        setUnlimited(uuid, amount);
    }

    @Override
    public void setUnlimited(UUID id, Integer amount){
        balanceMap.put(id, amount);
    }

    @Override
    public void add(UUID uuid, Integer amount){
        add(uuid, amount, false);
    }

    @Override
    public void add(UUID uuid, Integer amount, boolean isTaxed){
        if(amount + get(uuid) >= FtcCore.getMaxMoneyAmount()){
            Announcer.log(Level.WARNING, Bukkit.getOfflinePlayer(uuid).getName() + " has reached the balance limit.");
            balanceMap.put(uuid, FtcCore.getMaxMoneyAmount());
            return;
        }

        if(amount > 0) FtcCore.getUser(uuid).addTotalEarnings(amount);

        if(FtcCore.areTaxesEnabled() && isTaxed && getTax(uuid) > 1 && amount > 1){
            int amountToRemove = (int) (amount * ((float) getTax(uuid)/100));
            amount -= amountToRemove;

            FtcCore.getUser(uuid).sendMessage("&7You were taxed " + getTax(uuid) + "%, which means you lost " + amountToRemove + " Rhines of your last transaction");
        }

        balanceMap.put(uuid, get(uuid) + amount);
    }

    @Override
    public Integer getTax(UUID uuid){
        if(get(uuid) < 500000) return 0; //if the player has less thank 500k rhines, no tax

        int percent = (int) (FtcCore.getUser(uuid).getTotalEarnings() / 50000 * 10);
        if(percent >= 30) return 50;
        return percent;
    }
}

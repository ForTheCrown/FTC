package net.forthecrown.economy;

import com.google.gson.JsonElement;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CrownBalances extends AbstractJsonSerializer implements Balances {

    private BalanceMap balanceMap = new SortedBalanceMap(ComVars::getStartRhines);

    public CrownBalances() {
        super("balances");

        reload();
        Crown.logger().info("Balances loaded");
    }

    @Override
    protected void save(JsonBuf json) {
        Set<UUID> serialized = new HashSet<>();

        for (BalanceMap.BalEntry e: getMap().entries()) {
            if(serialized.contains(e)) continue;
            serialized.add(e.getUniqueId());

            json.add(e.getUniqueId().toString(), e.getValue());
        }
    }

    @Override
    protected void reload(JsonBuf json) {
        balanceMap.clear();

        for (Map.Entry<String, JsonElement> e: json.entrySet()) {
            balanceMap.put(UUID.fromString(e.getKey()), e.getValue().getAsInt());
        }
    }

    @Override
    public synchronized BalanceMap getMap(){
        return balanceMap;
    }
    @Override
    public synchronized void setMap(BalanceMap balanceMap){
        this.balanceMap = balanceMap;
    }

    @Override
    public synchronized int get(UUID uuid){
        return balanceMap.get(uuid);
    }

    @Override
    public synchronized void set(UUID uuid, int amount){
        Balances.checkUnderMax(uuid, amount);
        setUnlimited(uuid, Balances.clampToBalBounds(amount));
    }

    @Override
    public synchronized void setUnlimited(UUID id, int amount){
        balanceMap.put(id, amount);
    }

    @Override
    public boolean canAfford(UUID id, int amount) {
        return get(id) >= amount;
    }

    @Override
    public synchronized void add(UUID uuid, int amount){
        add(uuid, amount, false);
    }

    @Override
    public synchronized void remove(UUID id, int amount) {
        int bal = get(id);
        set(id, bal - amount);
    }

    @Override
    public synchronized void add(UUID uuid, int amount, boolean isTaxed){
        int current = get(uuid);

        //If should be taxed
        int tax = getTax(uuid, current);
        if(ComVars.areTaxesEnabled() && isTaxed && amount > 1 && tax > 1){
            int amountToRemove = (int) (amount * ((float) tax/100));
            amount -= amountToRemove;

            //Tell em they lost mulaa
            UserManager.getUser(uuid).sendMessage(
                    Component.translatable("economy.taxed", NamedTextColor.GRAY,
                            Component.text(tax),
                            FtcFormatter.rhines(amountToRemove)
                    )
            );
        }

        //If it's more than 0, add earnings
        if(amount > 0) UserManager.getUser(uuid).addTotalEarnings(amount);

        int actual = current + amount;
        set(uuid, actual);
    }

    @Override
    public int getTax(UUID uuid, int currentBal){
        if(currentBal < 500000) return 0; //if the player has less thank 500k rhines, no tax

        int percent = (int) (UserManager.getUser(uuid).getTotalEarnings() / 50000 * 10);
        if(percent >= 30) return 50;
        return percent;
    }
}

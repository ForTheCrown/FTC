package net.forthecrown.economy;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcVars;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import org.apache.commons.lang.Validate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FtcEconomy extends AbstractJsonSerializer implements Economy {

    private BalanceMap balanceMap = new SortedBalanceMap(100, FtcVars.startRhines::get);

    public FtcEconomy() {
        super("balances");

        reload();
        Crown.logger().info("Balances loaded");
    }

    @Override
    protected void save(JsonWrapper json) {
        Set<UUID> serialized = new HashSet<>();

        balanceMap.readerStream().forEach(e -> {
            if(serialized.contains(e.getUniqueId())) return;
            serialized.add(e.getUniqueId());

            json.add(e.getUniqueId().toString(), e.getValue());
        });
    }

    @Override
    protected void reload(JsonWrapper json) {
        balanceMap.clear();

        for (Map.Entry<String, JsonElement> e: json.entrySet()) {
            balanceMap.put(UUID.fromString(e.getKey()), e.getValue().getAsInt());
        }
    }

    @Override
    public synchronized BalanceMap getMap() {
        return balanceMap;
    }
    @Override
    public synchronized void setMap(BalanceMap balanceMap) {
        this.balanceMap = balanceMap;
    }

    @Override
    public synchronized int get(UUID uuid) {
        validateID(uuid);
        return balanceMap.get(uuid);
    }

    @Override
    public synchronized void set(UUID uuid, int amount) {
        Economy.checkUnderMax(uuid, amount);
        setUnlimited(uuid, Economy.clampToBalBounds(amount));
    }

    @Override
    public synchronized void setUnlimited(UUID id, int amount) {
        validateID(id);
        balanceMap.put(id, amount);
    }

    @Override
    public boolean has(UUID id, int amount) {
        return get(id) >= amount;
    }

    @Override
    public synchronized void remove(UUID id, int amount) {
        validate(id, amount);

        int bal = get(id);
        set(id, bal - amount);
    }

    @Override
    public synchronized void add(UUID uuid, int amount ) {
        validate(uuid, amount);

        int current = get(uuid);
        CrownUser user = UserManager.getUser(uuid);

        // Add earnings
        user.addTotalEarnings(amount);
        user.unloadIfOffline();

        int actual = current + amount;
        set(uuid, actual);
    }

    private void validate(UUID id, int amount) {
        validateID(id);
        Validate.isTrue(amount > 0, "Amount cannot be less than 1");
    }

    @Override
    public int getIncomeTax(UUID uuid, int currentBal){
        if(!FtcVars.taxesEnabled.get()) return 0;
        if(currentBal < 500000) return 0; //if the player has less thank 500k rhines, no tax

        int percent = (int) (UserManager.getUser(uuid).getTotalEarnings() / 50000 * 10);
        if(percent >= 30) return 50;
        return percent;
    }

    private void validateID(UUID id) {
        Validate.isTrue(UserManager.isPlayerID(id), "Given UUID doesn't belong to a player");
    }
}
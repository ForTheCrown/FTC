package net.forthecrown.economy.market;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonBuf;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class FtcMarketRegion extends AbstractJsonSerializer implements MarketRegion {
    private final Object2ObjectMap<UUID, MarketShop> byOwner = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, MarketShop> byName = new Object2ObjectOpenHashMap<>();

    public FtcMarketRegion() {
        super("market_region");

        reload();
    }

    @Override
    public MarketShop get(UUID owner) {
        return byOwner.get(owner);
    }

    @Override
    public MarketShop get(String claimName) {
        return byName.get(claimName);
    }

    @Override
    public void add(MarketShop claim) {
        if(claim.getOwner() != null) byOwner.put(claim.getOwner(), claim);

        byName.put(claim.getName(), claim);
    }

    @Override
    public void attemptPurchase(MarketShop claim, Player player) {

    }

    @Override
    public void evict(MarketShop shop) {

    }

    @Override
    public void evict(UUID owner) {

    }

    @Override
    public void remove(String name) {

    }

    @Override
    public void clear() {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Set<UUID> getOwners() {
        return null;
    }

    @Override
    protected void save(JsonBuf json) {

    }

    @Override
    protected void reload(JsonBuf json) {

    }
}

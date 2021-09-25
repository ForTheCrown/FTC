package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.Economy;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMarketOwnership;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.BoundingBoxes;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.Date;
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
    public World getWorld() {
        return Worlds.OVERWORLD;
    }

    @Override
    public void add(MarketShop claim) {
        if(claim.getOwner() != null) byOwner.put(claim.getOwner(), claim);

        byName.put(claim.getName(), claim);
    }

    @Override
    public void attemptPurchase(MarketShop claim, CrownUser user) throws CommandSyntaxException {
        if(user.getMarketOwnership().currentlyOwnsShop()) {
            throw FtcExceptionProvider.translatable("market.alreadyOwner");
        }

        if(claim.hasOwner()) throw FtcExceptionProvider.translatable("market.alreadyOwned");

        Economy economy = Crown.getEconomy();
        if(!economy.has(user.getUniqueId(), claim.getPrice())) {
            throw FtcExceptionProvider.cannotAfford(claim.getPrice());
        }

        UserMarketOwnership ownership = user.getMarketOwnership();
        if(!ownership.hasOwnedBefore()) ownership.setOwnershipBegan(System.currentTimeMillis());
        ownership.setOwnedName(claim.getName());

        economy.remove(user.getUniqueId(), claim.getPrice());
        claim.setOwner(user.getUniqueId());
        claim.setDateOfPurchase(new Date());

        claim.getWorldGuard().getMembers().addPlayer(user.getUniqueId());

        for (ShopEntrance e: claim.getEntrances()) {
            e.onClaim(user, getWorld());
        }
    }

    @Override
    public void unclaim(MarketShop shop, boolean eviction) {
        Validate.isTrue(shop.hasOwner(), "Market has no owner");

        shop.getWorldGuard().getMembers().clear();
        shop.getWorldGuard().getOwners().clear();

        CrownUser owner = shop.ownerUser();
        UserMarketOwnership ownership = owner.getMarketOwnership();

        ownership.setOwnedName(null);

        shop.setDateOfPurchase(null);
        shop.setMerged(null);
        shop.setOwner(null);
        shop.getCoOwners().clear();

        if(eviction) {
            ownership.setOwnershipBegan(0L);

            BoundingBoxes.copyTo(
                    Worlds.VOID,
                    shop.getVoidExample(),
                    shop.getResetPos().toWorldVector(getWorld())
            );
        }

        for (ShopEntrance e: shop.getEntrances()) {
            e.onUnclaim(getWorld(), shop);
        }
    }

    @Override
    public void merge(MarketShop shop, MarketShop merged) {

    }

    @Override
    public void remove(String name) {
        MarketShop shop = get(name);

        if(shop.hasOwner()) byOwner.remove(shop.getOwner());

        byName.remove(shop.getName());
    }

    @Override
    public void clear() {
        byName.clear();
        byOwner.clear();
    }

    @Override
    public int size() {
        return byName.size();
    }

    @Override
    public Set<UUID> getOwners() {
        return byOwner.keySet();
    }

    @Override
    public Set<String> getNames() {
        return byName.keySet();
    }

    @Override
    public Collection<MarketShop> getAllShops() {
        return byName.values();
    }

    @Override
    public Collection<MarketShop> getOwnedShops() {
        return byOwner.values();
    }

    @Override
    protected void save(JsonWrapper json) {
        json.addList("entries", byName.values());
    }

    @Override
    protected void reload(JsonWrapper json) {
        byName.clear();
        byOwner.clear();

        if(json.has("entries")) {
            for (JsonElement e: json.getArray("entries")) {
                add(FtcMarketShop.fromJson(e));
            }
        }
    }
}

package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.Economy;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.MarketOwnership;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.BoundingBoxes;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class FtcMarkets extends AbstractJsonSerializer implements Markets {
    //2 maps for tracking shops, byName stores all saved shops
    private final Object2ObjectMap<UUID, MarketShop> byOwner = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, MarketShop> byName = new Object2ObjectOpenHashMap<>();

    public FtcMarkets() {
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
        //Return the OVERWORLD constant in Worlds
        return Worlds.OVERWORLD;
    }

    @Override
    public void add(MarketShop claim) {
        if(claim.getOwner() != null) byOwner.put(claim.getOwner(), claim);

        byName.put(claim.getName(), claim);
    }

    @Override
    public void attemptPurchase(MarketShop claim, CrownUser user) throws CommandSyntaxException {
        MarketOwnership ownership = user.getMarketOwnership();

        //If they already own a shop
        if(ownership.currentlyOwnsShop()) {
            throw FtcExceptionProvider.translatable("market.alreadyOwner");
        }

        //If the shop already has an owner, idk how this could even be triggered lol
        if(claim.hasOwner()) throw FtcExceptionProvider.translatable("market.alreadyOwned");

        //Check if they can even buy it
        Markets.checkCanChangeStatus(ownership);

        //Check if they can afford it
        Economy economy = Crown.getEconomy();
        if(!economy.has(user.getUniqueId(), claim.getPrice())) {
            throw FtcExceptionProvider.cannotAfford(claim.getPrice());
        }

        economy.remove(user.getUniqueId(), claim.getPrice());

        //Claim it
        claim(claim, user);
    }

    @Override
    public void claim(MarketShop claim, CrownUser user) {
        Validate.isTrue(!claim.hasOwner(), "Market already has owner");

        MarketOwnership ownership = user.getMarketOwnership();
        if(!ownership.hasOwnedBefore()) ownership.setOwnershipBegan(System.currentTimeMillis());
        ownership.setOwnedName(claim.getName());
        ownership.setLastStatusChange(System.currentTimeMillis());

        claim.setOwner(user.getUniqueId());
        claim.setDateOfPurchase(new Date());

        claim.getWorldGuard().getMembers().addPlayer(user.getUniqueId());

        for (ShopEntrance e: claim.getEntrances()) {
            e.onClaim(user, getWorld());
        }
    }

    @Override
    public void unclaim(MarketShop shop, boolean complete) {
        Validate.isTrue(shop.hasOwner(), "Market has no owner");

        if(shop.isMerged()) unmerge(shop);

        CrownUser owner = shop.ownerUser();
        MarketOwnership ownership = owner.getMarketOwnership();

        ownership.setOwnedName(null);

        shop.setDateOfPurchase(null);
        shop.setMerged(null);
        shop.setOwner(null);

        shop.getWorldGuard().getMembers().clear();
        shop.getCoOwners().clear();

        if(complete) {
            ownership.setOwnershipBegan(0L);

            reset(shop);
        }

        for (ShopEntrance e: shop.getEntrances()) {
            e.onUnclaim(getWorld(), shop);
        }
    }

    @Override
    public void merge(MarketShop shop, MarketShop merged) {
        Validate.isTrue(shop.equals(merged), "Same shops given in parameters");

        shop.setMerged(merged);
        merged.setMerged(shop);

        for (UUID id: shop.getCoOwners()) {
            merged.getWorldGuard().getMembers().addPlayer(id);
        }

        for (UUID id: merged.getCoOwners()) {
            shop.getWorldGuard().getMembers().addPlayer(id);
        }
    }

    @Override
    public void unmerge(MarketShop shop) {
        Validate.isTrue(shop.isMerged(), "Given shop was not merged");

        MarketShop merged = shop.getMerged();

        merged.setMerged(null);
        shop.setMerged(null);

        for (UUID id: merged.getCoOwners()) {
            shop.getWorldGuard().getMembers().removePlayer(id);
        }

        for (UUID id: shop.getCoOwners()) {
            merged.getWorldGuard().getMembers().removePlayer(id);
        }
    }

    @Override
    public void trust(MarketShop shop, UUID uuid) {
        shop.getCoOwners().add(uuid);
        shop.getWorldGuard().getMembers().addPlayer(uuid);

        if(shop.isMerged()) {
            MarketShop merged = shop.getMerged();
            merged.getWorldGuard().getMembers().addPlayer(uuid);
        }
    }

    @Override
    public void untrust(MarketShop shop, UUID uuid) {
        shop.getCoOwners().remove(uuid);
        shop.getWorldGuard().getMembers().removePlayer(uuid);

        if(shop.isMerged()) {
            MarketShop merged = shop.getMerged();
            merged.getWorldGuard().getMembers().removePlayer(uuid);
        }
    }

    @Override
    public void removeEntrance(MarketShop shop, int index) {
        ShopEntrance entrance = shop.getEntrances().get(index);

        entrance.removeSign(getWorld());
        entrance.removeNotice(getWorld());

        shop.getEntrances().remove(index);
    }

    @Override
    public void addEntrance(MarketShop shop, ShopEntrance entrance) {
        shop.getEntrances().add(entrance);
    }

    @Override
    public boolean areConnected(MarketShop shop, MarketShop other) {
        return shop.getConnectedNames().contains(other.getName());
    }

    @Override
    public void connect(MarketShop shop, MarketShop other) {
        shop.getConnectedNames().add(other.getName());
        other.getConnectedNames().add(shop.getName());
    }

    @Override
    public void disconnect(MarketShop shop, MarketShop other) {
        shop.getConnectedNames().remove(other.getName());
        other.getConnectedNames().remove(shop.getName());
    }

    @Override
    public void remove(MarketShop shop) {
        if(shop.hasOwner()) byOwner.remove(shop.getOwner());

        byName.remove(shop.getName());

        for (ShopEntrance e: shop.getEntrances()) {
            e.removeNotice(getWorld());
            e.removeSign(getWorld());
        }

        ProtectedRegion region = shop.getWorldGuard();

        if(shop.isMerged()) unmerge(shop);

        if(shop.hasOwner()) {
            CrownUser user = shop.ownerUser();
            shop.setOwner(null);

            region.getMembers().removePlayer(user.getUniqueId());

            MarketOwnership ownership = user.getMarketOwnership();
            ownership.setOwnedName(null);
            ownership.setOwnershipBegan(0L);

            if(!shop.getCoOwners().isEmpty()) {
                for (UUID id: shop.getCoOwners()) {
                    region.getMembers().removePlayer(id);
                }
            }
        }
    }

    @Override
    public void transfer(MarketShop shop, UUID target) {
        Validate.isTrue(shop.hasOwner(), "Shop has no owner");

        CrownUser user = UserManager.getUser(target);

        shop.setOwner(target);
        shop.getCoOwners().clear();

        ProtectedRegion region = shop.getWorldGuard();
        region.getMembers().clear();
        region.getMembers().addPlayer(target);

        for (ShopEntrance e: shop.getEntrances()) {
            e.onClaim(user, getWorld());
        }

        user.unloadIfOffline();
    }

    @Override
    public void reset(MarketShop shop) {
        BoundingBoxes.mainThreadCopy(
                Worlds.VOID,
                shop.getVoidExample(),
                shop.getResetPos().toWorldVector(getWorld())
        );
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

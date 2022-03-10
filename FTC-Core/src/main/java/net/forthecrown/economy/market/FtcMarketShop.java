package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Worlds;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.TimeUtil;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class FtcMarketShop implements MarketShop {
    private final ProtectedRegion worldGuard;

    private final ObjectList<ShopEntrance> entrances = new ObjectArrayList<>();
    private final ObjectList<String> connected = new ObjectArrayList<>();

    private UUID owner;
    private Date dateOfPurchase;
    private EvictionData evictionData;
    private final ObjectList<UUID> coOwners = new ObjectArrayList<>();

    private int price = -1;
    private String mergedName;

    public FtcMarketShop(ProtectedRegion worldGuard) {
        this.worldGuard = worldGuard;
    }

    @Override
    public ProtectedRegion getWorldGuard() {
        return worldGuard;
    }

    @Override
    public ObjectList<ShopEntrance> getEntrances() {
        return entrances;
    }

    @Override
    public int getPrice() {
        return price == -1 ? FtcVars.defaultMarketPrice.get() : price;
    }

    @Override
    public void setPrice(int price) {
        this.price = Mth.clamp(price, -1, FtcVars.maxMoneyAmount.get());
    }

    @Override
    public Date getDateOfPurchase() {
        return dateOfPurchase;
    }

    @Override
    public void setDateOfPurchase(Date dateOfPurchase) {
        this.dateOfPurchase = dateOfPurchase;
    }

    @Override
    public boolean canBeEvicted() {
        if(dateOfPurchase == null || owner == null) return false;
        return TimeUtil.hasCooldownEnded(FtcVars.marketOwnershipSafeTime.get(), getDateOfPurchase().getTime());
    }

    @Override
    public MarketShop getMerged() {
        return FtcUtils.isNullOrBlank(mergedName) ? null : Crown.getMarkets().get(mergedName);
    }

    @Override
    public void setMerged(MarketShop shop) {
        mergedName = shop == null ? null : shop.getName();
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    @Override
    public ObjectList<UUID> getCoOwners() {
        return coOwners;
    }

    @Override
    public ObjectList<String> getConnectedNames() {
        return connected;
    }

    @Override
    public void setEvictionDate(@Nullable Date date) {
        if(evictionData != null) {
            evictionData.cancel();
        }

        if(date != null) {
            this.evictionData = new EvictionData(this, date);
            evictionData.start();
        } else {
            evictionData = null;
        }
    }

    @Override
    public Date getEvictionDate() {
        return evictionData == null ? null : evictionData.getDate();
    }

    @Override
    public JsonObject serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("name", getName());
        json.add("price", price);

        if(!ListUtils.isNullOrEmpty(entrances)) {
            json.addList("entrances", entrances);
        }

        if (!ListUtils.isNullOrEmpty(connected)) {
            json.addList("connected", connected, JsonPrimitive::new);
        }

        if(hasOwner()) {
            JsonWrapper ownership = JsonWrapper.empty();

            ownership.addUUID("owner", getOwner());
            if(getDateOfPurchase() != null) ownership.addDate("dateOfPurchase", getDateOfPurchase());

            if(markedForEviction()) {
                ownership.addDate("evictionDate", evictionData.getDate());
            }

            if(mergedName != null) {
                ownership.add("merged", mergedName);
            }

            if(!ListUtils.isNullOrEmpty(getCoOwners())) {
                ownership.addList("coOwners", getCoOwners(), JsonUtils::writeUUID);
            }

            json.add("ownershipData", ownership);
        }

        return json.getSource();
    }

    public static FtcMarketShop fromJson(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        String name = json.getString("name");

        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(
                        BukkitAdapter.adapt(Worlds.OVERWORLD)
                );

        ProtectedRegion region = manager.getRegion(name);
        FtcMarketShop market = new FtcMarketShop(region);

        market.price = json.getInt("price");

        if(json.has("entrances")) {
            market.entrances.addAll(json.getList("entrances", ShopEntrance::fromJson));
        }

        if(json.has("connected")) {
            market.connected.addAll(json.getList("connected", JsonElement::getAsString));
        }

        if(json.has("ownershipData")) {
            JsonWrapper ownership = json.getWrapped("ownershipData");

            market.owner = ownership.getUUID("owner");
            market.dateOfPurchase = ownership.getDate("dateOfPurchase");

            if(ownership.has("evictionDate")) {
                market.setEvictionDate(ownership.getDate("evictionDate"));
            }

            if(ownership.has("coOwners")) {
                market.coOwners.addAll(ownership.getList("coOwners", JsonUtils::readUUID));
            }

            if(ownership.has("merged")) {
                market.mergedName = ownership.getString("merged");
            }
        }

        return market;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FtcMarketShop shop = (FtcMarketShop) o;

        return new EqualsBuilder()
                .append(getPrice(), shop.getPrice())
                .append(getWorldGuard(), shop.getWorldGuard())
                .append(getEntrances(), shop.getEntrances())
                .append(getOwner(), shop.getOwner())
                .append(getCoOwners(), shop.getCoOwners())
                .append(mergedName, shop.mergedName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getWorldGuard())
                .append(getEntrances())
                .append(getOwner())
                .append(getCoOwners())
                .append(getPrice())
                .toHashCode();
    }
}

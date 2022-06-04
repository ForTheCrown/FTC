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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Worlds;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
public class FtcMarketShop implements MarketShop {
    @Getter
    private final ProtectedRegion worldGuard;

    @Getter @Setter
    private UUID owner;

    @Getter @Setter
    private Date dateOfPurchase;

    @Getter
    private final ObjectList<UUID> coOwners = new ObjectArrayList<>();

    @Getter @Setter
    private boolean taxed;

    @Getter
    private final ObjectList<ShopEntrance> entrances = new ObjectArrayList<>();

    private final ObjectList<String> connected = new ObjectArrayList<>();

    @Getter
    private final ObjectList<MarketScan> scans = new ObjectArrayList<>();

    @Getter @Setter
    private boolean memberEditingAllowed = true;

    @Getter
    private MarketEviction eviction;

    private int price = -1;
    private String mergedName;

    @Override
    public int getPrice() {
        return price == -1 ? FtcVars.defaultMarketPrice.get() : price;
    }

    @Override
    public void setPrice(int price) {
        this.price = Mth.clamp(price, -1, FtcVars.maxMoneyAmount.get());
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
    public ObjectList<String> getConnectedNames() {
        return connected;
    }

    @Override
    public void setEviction(MarketEviction eviction) {
        if (this.eviction != null) {
            this.eviction.cancel();
        }

        this.eviction = eviction;

        if (eviction != null) {
            eviction.start();
        }
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
                ownership.add("eviction", eviction);
            }

            if (!scans.isEmpty()) {
                ownership.addList("scans", scans);
            }

            if(mergedName != null) {
                ownership.add("merged", mergedName);
            }

            if(!ListUtils.isNullOrEmpty(getCoOwners())) {
                ownership.addList("coOwners", getCoOwners(), JsonUtils::writeUUID);
            }

            if (!memberEditingAllowed) {
                ownership.add("membersCanEditShops", false);
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
                MarketEviction data = new MarketEviction(
                        market.getName(),
                        MarketEviction.CAUSE_COMMAND,
                        ownership.getDate("evictionData").getTime(),
                        Component.text("Admin")
                );
                market.setEviction(data);
            } else if (ownership.has("eviction")) {
                market.setEviction(MarketEviction.deserialize(ownership.getArray("eviction"), market));
            }

            if (ownership.has("scans")) {
                market.scans.addAll(ownership.getList("scans", MarketScan::deserialize));
            }

            if(ownership.has("coOwners")) {
                market.coOwners.addAll(ownership.getList("coOwners", JsonUtils::readUUID));
            }

            if(ownership.has("merged")) {
                market.mergedName = ownership.getString("merged");
            }

            market.memberEditingAllowed = ownership.getBool("membersCanEditShops", true);
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
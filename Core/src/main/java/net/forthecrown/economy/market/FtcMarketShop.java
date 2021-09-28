package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

public class FtcMarketShop implements MarketShop {
    private final ProtectedRegion worldGuard;

    private final BoundingBox voidExample;
    private final Vector3i resetPos;
    private ObjectList<ShopEntrance> entrances = new ObjectArrayList<>();
    private ObjectList<String> connected = new ObjectArrayList<>();

    private UUID owner;
    private Date dateOfPurchase;
    private ObjectList<UUID> coOwners = new ObjectArrayList<>();

    private int price = 35000;
    private String mergedName;

    public FtcMarketShop(ProtectedRegion worldGuard, BoundingBox voidExample, Vector3i resetPos) {
        this.worldGuard = worldGuard;
        this.voidExample = voidExample;
        this.resetPos = resetPos;
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
    public void setEntrances(ObjectList<ShopEntrance> entrances) {
        this.entrances = entrances;
    }

    @Override
    public BoundingBox getVoidExample() {
        return voidExample;
    }

    @Override
    public Vector3i getResetPos() {
        return resetPos;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public void setPrice(int price) {
        this.price = price;
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
        Date validRemoval = new Date(dateOfPurchase.getTime() + ComVars.getShopOwnershipSafeTime());
        Date current = new Date();

        return validRemoval.before(current);
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
    public void setCoOwners(ObjectList<UUID> coOwners) {
        this.coOwners = coOwners;
    }

    @Override
    public ObjectList<String> getConnectedNames() {
        return connected;
    }

    @Override
    public void setConnected(ObjectList<String> strings) {
        this.connected = strings;
    }

    @Override
    public JsonObject serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("name", getName());
        json.add("resetPos", resetPos);
        json.add("voidExample", JsonUtils.writeVanillaBoundingBox(voidExample));
        json.add("price", price);

        if(!ListUtils.isNullOrEmpty(entrances)) {
            json.addList("entrances", entrances);
        }

        if(hasOwner()) {
            JsonWrapper ownership = JsonWrapper.empty();

            ownership.addUUID("owner", owner);
            ownership.add("dateOfPurchase", dateOfPurchase.toString());

            if(mergedName != null) {
                ownership.add("merged", mergedName);
            }

            if(!ListUtils.isNullOrEmpty(coOwners)) {
                ownership.addList("coOwners", coOwners, JsonUtils::writeUUID);
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

        Vector3i resetPos = Vector3i.of(json.get("resetPos"));
        BoundingBox box = JsonUtils.readVanillaBoundingBox(json.getObject("voidExample"));

        FtcMarketShop market = new FtcMarketShop(region, box, resetPos);

        market.price = json.getInt("price");

        if(json.has("entrances")) {
            market.entrances.addAll(json.getList("entrances", ShopEntrance::fromJson));
        }

        if(json.has("ownershipData")) {
            JsonWrapper ownership = json.getWrapped("ownershipData");

            market.owner = ownership.getUUID("owner");

            try {
                market.dateOfPurchase = DateFormat.getDateInstance().parse(json.getString("dateOfPurchase"));
            } catch (ParseException e) {
                e.printStackTrace();
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
}

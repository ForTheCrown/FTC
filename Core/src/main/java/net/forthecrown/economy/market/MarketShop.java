package net.forthecrown.economy.market;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Date;
import java.util.UUID;

public interface MarketShop extends JsonSerializable, Nameable {
    ProtectedRegion getWorldGuard();

    @Override
    default String getName() {
        return getWorldGuard().getId();
    }

    ObjectList<ShopEntrance> getEntrances();
    void setEntrances(ObjectList<ShopEntrance> entrances);

    BoundingBox getVoidExample();
    Vector3i getShopResetPos();

    int getPrice();
    Date getDateOfPurchase();
    void setDateOfPurchase(Date dateOfPurchase);

    MarketShop getMerged();
    void setMerged(MarketShop shop);

    UUID getOwner();
    void setOwner(UUID uuid);

    ObjectList<UUID> getCoOwners();
    void setCoOwners(ObjectList<UUID> coOwners);
}

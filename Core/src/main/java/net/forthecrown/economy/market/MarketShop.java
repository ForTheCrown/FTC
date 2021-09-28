package net.forthecrown.economy.market;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.Struct;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Date;
import java.util.UUID;

public interface MarketShop extends JsonSerializable, Nameable, Struct {
    ProtectedRegion getWorldGuard();

    @Override
    default String getName() {
        return getWorldGuard().getId();
    }

    ObjectList<ShopEntrance> getEntrances();
    void setEntrances(ObjectList<ShopEntrance> entrances);

    BoundingBox getVoidExample();
    Vector3i getResetPos();

    int getPrice();
    void setPrice(int price);

    Date getDateOfPurchase();
    void setDateOfPurchase(Date dateOfPurchase);

    boolean canBeEvicted();

    MarketShop getMerged();
    void setMerged(MarketShop shop);

    default boolean isMerged() {
        return getMerged() != null;
    }

    UUID getOwner();
    void setOwner(UUID uuid);

    default boolean hasOwner() {
        return getOwner() != null;
    }

    default CrownUser ownerUser() {
        return hasOwner() ? UserManager.getUser(getOwner()) : null;
    }

    ObjectList<UUID> getCoOwners();
    void setCoOwners(ObjectList<UUID> coOwners);

    ObjectList<String> getConnectedNames();
    void setConnected(ObjectList<String> strings);

    default boolean canInteractWith(UUID id) {
        if(id.equals(getOwner())) return true;
        if(getCoOwners().contains(id)) return true;

        if(isMerged()) {
            MarketShop shop = getMerged();
            if(id.equals(shop.getOwner())) return true;
            return shop.getCoOwners().contains(id);
        }

        return false;
    }
}

package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Date;
import java.util.UUID;

public class FtcMarketShop implements MarketShop {
    private final ProtectedRegion worldGuard;

    private final BoundingBox voidExample;
    private final Vector3i resetPos;

    private UUID owner;
    private Date dateOfPurchase;
    private ObjectList<UUID> coOwners;

    private int price;
    private String mergedName;

    public FtcMarketShop(ProtectedRegion worldGuard, BoundingBox voidExample, Vector3i resetPos) {
        this.worldGuard = worldGuard;
        this.voidExample = voidExample;
        this.resetPos = resetPos;
    }

    @Override
    public ProtectedRegion getWorldGuard() {
        return null;
    }

    @Override
    public ObjectList<ShopEntrance> getEntrances() {
        return null;
    }

    @Override
    public void setEntrances(ObjectList<ShopEntrance> entrances) {

    }

    @Override
    public BoundingBox getVoidExample() {
        return null;
    }

    @Override
    public Vector3i getShopResetPos() {
        return null;
    }

    @Override
    public int getPrice() {
        return 0;
    }

    @Override
    public void setPrice(int price) {

    }

    @Override
    public Date getDateOfPurchase() {
        return null;
    }

    @Override
    public void setDateOfPurchase(Date dateOfPurchase) {

    }

    @Override
    public boolean canBeEvicted() {
        return false;
    }

    @Override
    public MarketShop getMerged() {
        return null;
    }

    @Override
    public void setMerged(MarketShop shop) {

    }

    @Override
    public UUID getOwner() {
        return null;
    }

    @Override
    public void setOwner(UUID uuid) {

    }

    @Override
    public ObjectList<UUID> getCoOwners() {
        return null;
    }

    @Override
    public void setCoOwners(ObjectList<UUID> coOwners) {

    }

    @Override
    public JsonElement serialize() {
        return null;
    }
}

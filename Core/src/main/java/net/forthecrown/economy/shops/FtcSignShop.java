package net.forthecrown.economy.shops;

import net.forthecrown.core.Crown;
import net.forthecrown.serializer.ShopSerializer;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class FtcSignShop implements SignShop {

    private final LocationFileName fileName;
    private final WorldVec3i loc;
    private final Block block;
    private final FtcShopInventory inventory;
    private final ShopOwnership ownership;

    private ShopType type;
    private int price;
    private boolean outOfStock;

    //used by getSignShop
    public FtcSignShop(WorldVec3i loc) throws IllegalArgumentException {
        this.fileName = LocationFileName.of(loc);

        ShopSerializer serializer = Crown.getShopManager().getSerializer();
        Validate.isTrue(serializer.fileExists(this), getFileName() + " has no file");

        this.loc = loc;
        block = loc.getBlock();
        this.ownership = new ShopOwnership();

        inventory = new FtcShopInventory(this);

        reload();
    }

    //used by createSignShop
    public FtcSignShop(WorldVec3i loc, ShopType shopType, int price, UUID shopOwner) {
        this.fileName = LocationFileName.of(loc);
        this.loc = loc;
        this.price = price;
        this.block = loc.getBlock();
        this.type = shopType;

        this.ownership = new ShopOwnership();
        ownership.setOwner(shopOwner);

        if(type != ShopType.ADMIN_BUY && type != ShopType.ADMIN_SELL) this.outOfStock = true;

        this.inventory = new FtcShopInventory(this);

        save();
    }

    @Override
    public void save() {
        Crown.getShopManager().getSerializer().serialize(this);
    }

    @Override
    public void reload() {
        Crown.getShopManager().getSerializer().deserialize(this);

        inventory.checkStock();
        update();
    }

    @Override
    public void destroy(boolean removeBlock) {
        Crown.getShopManager().removeShop(this);
        if(inventory != null && !inventory.getShopContents().isEmpty()) {
            for (ItemStack stack : inventory.getShopContents()){
                loc.getWorld().dropItemNaturally(loc.toLocation(), stack);
            }
        }

        loc.getWorld().spawnParticle(Particle.CLOUD, loc.toLocation().add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);

        if(removeBlock) getBlock().breakNaturally();
        delete();
    }

    @Override
    public void delete() {
        Crown.getShopManager().getSerializer().delete(this);
    }

    @Override
    public void unload(){
        Crown.getShopManager().removeShop(this);
        save();
    }

    @Override
    public WorldVec3i getPosition() {
        return loc;
    }

    @Override
    public LocationFileName getFileName() {
        return fileName;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public ShopOwnership getOwnership() {
        return ownership;
    }

    @Override
    public ShopType getType() {
        return type;
    }

    @Override
    public void setType(ShopType shopType) {
        this.type = shopType;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public void setPrice(int price, boolean updateSign) {
        this.price = price;

        if(updateSign) update();
    }

    @Override
    public boolean isOutOfStock() {
        return outOfStock;
    }

    @Override
    public void setOutOfStock(boolean outOfStock) {
        if(getType().equals(ShopType.ADMIN_SELL)) return;

        this.outOfStock = outOfStock;
        update();
    }

    @Override
    public boolean wasDeleted(){
        return Crown.getShopManager().getSerializer().wasDeleted(this);
    }

    @Nonnull
    @Override
    public FtcShopInventory getInventory() {
        return inventory;
    }

    @Override
    public int getPrice(ShopCustomer customer) {
        return getPrice();
    }

    @Override
    public Component getPriceLineFor(CrownUser user) {
        return Crown.getShopManager().getPriceLine(getPrice());
    }

    @Override
    public Sign getSign(){
        return (Sign) getBlock().getState();
    }

    @Override
    public void update(){
        Sign s = getSign();
        Component ln1 = getType().inStockLabel();
        if(isOutOfStock()) ln1 = getType().outOfStockLabel();

        s.line(0, ln1);
        s.line(3, Crown.getShopManager().getPriceLine(price));

        Bukkit.getScheduler().runTask(Crown.inst(), () -> s.update(true));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignShop that = (SignShop) o;
        return getPosition().equals(that.getPosition()) &&
                getOwnership().equals(that.getOwnership()) &&
                getPrice() == that.getPrice() &&
                getType() == that.getType() &&
                getInventory().equals(that.getInventory());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosition(), getOwnership(), getPrice(), getType());
    }
}

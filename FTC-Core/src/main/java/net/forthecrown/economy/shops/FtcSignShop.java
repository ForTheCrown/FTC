package net.forthecrown.economy.shops;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.Crown;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.TagUtil;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Validate;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.UUID;

public class FtcSignShop implements SignShop {

    @Getter private final WorldVec3i position;

    @Getter private final FtcShopInventory inventory;
    @Getter private final ShopOwnership ownership;
    @Getter private final ShopHistory history;

    @Getter
    public long lastUse = -1L;

    @Getter @Setter
    public ShopType type;

    @Getter
    public int price;

    //used by getSignShop
    public FtcSignShop(WorldVec3i position) throws IllegalArgumentException {
        this(position, false);
    }

    //used by createSignShop
    public FtcSignShop(WorldVec3i position, ShopType shopType, int price, UUID shopOwner) {
        this(position, true);

        this.price = price;
        this.type = shopType;
        ownership.setOwner(shopOwner);
    }

    public FtcSignShop(WorldVec3i pos, boolean newShop) {
        this.position = pos;

        this.ownership = new ShopOwnership();
        this.history = new ShopHistory(this);
        this.inventory = new FtcShopInventory(this);

        if(!newShop) {
            Validate.isTrue(
                    getSign().getPersistentDataContainer().has(ShopConstants.SHOP_KEY),
                    getFileName() + " has no shop data"
            );

            load();
        }
    }

    @Override
    public void load() {
        load(getSign());
    }

    @Override
    public void destroy(boolean removeBlock) {
        Crown.getShopManager().onShopDestroy(this);

        if(inventory != null && !inventory.getShopContents().isEmpty()) {
            for (ItemStack stack : inventory.getShopContents()){
                position.getWorld().dropItemNaturally(position.toLocation(), stack);
            }
        }

        position.getWorld().spawnParticle(Particle.CLOUD, position.toLocation().add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);

        if(removeBlock) getBlock().breakNaturally();
    }

    @Override
    public LocationFileName getFileName() {
        return LocationFileName.of(getPosition());
    }

    @Override
    public Block getBlock() {
        return getPosition().getBlock();
    }

    @Override
    public void setPrice(int price, boolean updateSign) {
        this.price = price;

        if(updateSign) update();
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
    public void updateLastUse() {
        lastUse = System.currentTimeMillis();
    }

    @Override
    public Sign getSign(){
        return (Sign) getBlock().getState();
    }

    @Override
    public void update() {
        Sign s = getSign();

        s.line(0, !getInventory().inStock() ? getType().outOfStockLabel() : getType().inStockLabel());
        s.line(3, Crown.getShopManager().getPriceLine(price));

        save(s);

        s.update(true);
    }

    public void save(Sign sign) {
        CompoundTag tag = new CompoundTag();
        save(tag);

        sign.getPersistentDataContainer().set(
                ShopConstants.SHOP_KEY,
                PersistentDataType.TAG_CONTAINER,
                TagUtil.ofCompound(tag)
        );
    }

    private void load(Sign sign) {
        PersistentDataContainer container = sign.getPersistentDataContainer().getOrDefault(
                ShopConstants.SHOP_KEY,
                PersistentDataType.TAG_CONTAINER,
                TagUtil.newContainer()
        );

        CompoundTag tag = TagUtil.ofContainer(container);
        load(tag);
    }

    public void save(CompoundTag tag) {
        tag.putInt("price", price);
        tag.put("type", TagUtil.writeEnum(type));

        if (lastUse != -1L) {
            tag.putLong("lastStockEdit", lastUse);
        }

        writeComponent(ownership, tag);
        writeComponent(inventory, tag);
        writeComponent(history, tag);
    }

    public void load(CompoundTag tag) {
        price = tag.getInt("price");
        type = TagUtil.readEnum(tag.get("type"), ShopType.class);

        if (tag.contains("lastStockEdit")) lastUse = tag.getLong("lastStockEdit");
        else lastUse = -1L;

        readComponent(ownership, tag);
        readComponent(inventory, tag);
        readComponent(history, tag);
    }

    private void writeComponent(ShopComponent c, CompoundTag tag) {
        Tag t = c.save();
        if(t == null) return;

        tag.put(c.getSerialKey(), t);
    }

    private void readComponent(ShopComponent c, CompoundTag tag) {
        c.load(tag.get(c.getSerialKey()));
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
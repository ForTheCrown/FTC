package net.forthecrown.core.api;

import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.inventories.CrownShopStock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public interface SignShop {
    void destroyShop();

    Inventory getShopInventory();

    Inventory getExampleInventory();

    Location getLocation();

    Block getBlock();

    UUID getOwner();

    void setOwner(UUID shopOwner);

    ShopType getType();

    void setType(ShopType shopType);

    Integer getPrice();

    void setPrice(Integer price);

    boolean isOutOfStock();

    void setOutOfStock(boolean outOfStock);

    boolean wasDeleted();

    CrownShopStock getStock();
}

package net.forthecrown.core.api;

import net.forthecrown.core.enums.ShopType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface SignShop {
    void destroyShop();

    Inventory getShopInventory();

    void setShopInventory(Inventory inventory);

    Inventory getExampleInventory();

    boolean setExampleItems(ItemStack[] exampleItem);

    ItemStack[] setItems(ItemStack[] toSet);

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

    ItemStack getExampleItem();

    void setExampleItem(ItemStack exampleItem);

    boolean wasDeleted();
}

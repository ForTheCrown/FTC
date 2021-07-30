package net.forthecrown.economy.shops;

import net.forthecrown.core.ForTheCrown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.UUID;

/**
 * Manages shops n stuff
 */
public interface ShopManager {
    NamespacedKey SHOP_KEY = new NamespacedKey(ForTheCrown.inst(), "signshop");

    String BUY_LABEL = "=[Buy]=";
    String SELL_LABEL = "=[Sell]=";

    Style OUT_OF_STOCK_STYLE = Style.style(NamedTextColor.RED, TextDecoration.BOLD);
    Style NORMAL_STYLE = Style.style(NamedTextColor.GREEN, TextDecoration.BOLD);
    Style ADMIN_STYLE = Style.style(NamedTextColor.AQUA, TextDecoration.BOLD);

    Component PRICE_LINE = Component.text().append(Component.text("Price: ").color(NamedTextColor.DARK_GRAY)).build();

    /**
     * Checks whether a block is a preexisting signshop.
     * A null check is also performed in the statement
     * @param block The block to check
     * @return Whether the block is a shop or not
     */
    static boolean isShop(Block block){
        if(block == null) return false;
        if(!(block.getState() instanceof Sign)) return false;
        return ((Sign) block.getState()).getPersistentDataContainer().has(SHOP_KEY, PersistentDataType.BYTE);
    }

    SignShop getShop(Location signShop);

    SignShop createSignShop(Location location, ShopType shopType, Integer price, UUID ownerUUID);

    Component getPriceLine(int amount);

    void save();

    void reload();

    void addShop(CrownSignShop shop);

    void removeShop(SignShop shop);

    void clearShops();

    Collection<SignShop> getShops();
}
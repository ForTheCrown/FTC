package net.forthecrown.core.api;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.types.CrownSignShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages shops n stuff
 */
public interface ShopManager {
    /**
     * All loaded shops and their location. It's a map because Java can do lookup algorithms better than I can lol
     */
    Map<Location, CrownSignShop> LOADED_SHOPS = new HashMap<>();

    String BUY_LABEL = "=[Buy]=";
    String SELL_LABEL = "=[Sell]=";

    Style OUT_OF_STOCK_STYLE = Style.style(NamedTextColor.RED, TextDecoration.BOLD);
    Style NORMAL_STYLE = Style.style(NamedTextColor.GREEN, TextDecoration.BOLD);
    Style ADMIN_STYLE = Style.style(NamedTextColor.AQUA, TextDecoration.BOLD);

    Component PRICE_LINE = Component.text().append(Component.text("Price: ").color(NamedTextColor.DARK_GRAY)).build();

    /**
     * Gets a shop at a specific location
     * @param signShop The location of the shop
     * @return The shop at the location, null if no preexisting shop is there
     */
    static SignShop getShop(Location signShop) { //gets a signshop, throws a null exception if the shop file doesn't exist
        if(LOADED_SHOPS.containsKey(signShop)) return LOADED_SHOPS.get(signShop);

        try {
            return new CrownSignShop(signShop);
        } catch (Exception e){
            Announcer.log(Level.SEVERE, e.getMessage());
        }

        return null;
    }

    /**
     * Creates a sign shop
     * @param location The location it's at
     * @param shopType It's type
     * @param price The price of the item it's selling
     * @param ownerUUID The owner
     * @return The created sign shop
     */
    static SignShop createSignShop(Location location, ShopType shopType, Integer price, UUID ownerUUID){ //creates a signshop
        return new CrownSignShop(location, shopType, price, ownerUUID);
    }

    /**
     * Gets the price line for shops
     * @param amount The amount to get the component for
     * @return The price component, ex: Price: $100
     */
    static Component getPriceLine(int amount){
        return PRICE_LINE.append(Component.text("$" + amount));
    }

    /**
     * Saves all sign shops
     */
    static void save(){
        for (CrownSignShop shop: LOADED_SHOPS.values()){
            try {
                shop.save();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Reloads all sign shops
     */
    static void reload(){
        for (CrownSignShop shop: LOADED_SHOPS.values()){
            try {
                shop.reload();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Checks whether a block is a preexisting signshop.
     * A null check is also performed in the statement
     * @param block The block to check
     * @return Whether the block is a shop or not
     */
    static boolean isShop(Block block){
        if(block == null) return false;
        if(!(block.getState() instanceof Sign)) return false;
        return ((Sign) block.getState()).getPersistentDataContainer().has(FtcCore.SHOP_KEY, PersistentDataType.BYTE);
    }
}
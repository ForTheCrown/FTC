package net.forthecrown.core;

import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.files.CrownSignShop;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages shops n stuff
 */
public final class ShopManager {
    /**
     * All loaded shops and their location. It's a map because Java can do lookup algorithms better than I can lol
     */
    public static final Map<Location, CrownSignShop> LOADED_SHOPS = new HashMap<>();

    public static final String BUY_LABEL = "=[Buy]=";
    public static final String SELL_LABEL = "=[Sell]=";

    public static final Style OUT_OF_STOCK_STYLE = Style.style(NamedTextColor.RED, TextDecoration.BOLD);
    public static final Style NORMAL_STYLE = Style.style(NamedTextColor.GREEN, TextDecoration.BOLD);
    public static final Style ADMIN_STYLE = Style.style(NamedTextColor.AQUA, TextDecoration.BOLD);

    /**
     * Gets a shop at a specific location
     * @param signShop The location of the shop
     * @return The shop at the location, null if no preexisting shop is there
     */
    public static SignShop getShop(Location signShop) { //gets a signshop, throws a null exception if the shop file doesn't exist
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
    public static SignShop createSignShop(Location location, ShopType shopType, Integer price, UUID ownerUUID){ //creates a signshop
        return new CrownSignShop(location, shopType, price, ownerUUID);
    }

    /**
     * Saves all sign shops
     */
    public static void save(){
        for (CrownSignShop shop: LOADED_SHOPS.values()){
            try {
                shop.save();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Reloads all sign shops
     */
    public static void reload(){
        for (CrownSignShop shop: LOADED_SHOPS.values()){
            try {
                shop.reload();
            } catch (Exception ignored) {}
        }
    }
}
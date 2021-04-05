package net.forthecrown.core;

import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.files.CrownSignShop;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class ShopManager {
    public static final Map<Location, CrownSignShop> LOADED_SHOPS = new HashMap<>();

    public static SignShop getShop(Location signShop) { //gets a signshop, throws a null exception if the shop file doesn't exist
        if(LOADED_SHOPS.containsKey(signShop)) return LOADED_SHOPS.get(signShop);

        try {
            return new CrownSignShop(signShop);
        } catch (Exception e){
            Announcer.log(Level.SEVERE, e.getMessage());
        }

        return null;
    }

    public static SignShop createSignShop(Location location, ShopType shopType, Integer price, UUID ownerUUID){ //creates a signshop
        return new CrownSignShop(location, shopType, price, ownerUUID);
    }

    public static void save(){
        for (CrownSignShop shop: LOADED_SHOPS.values()){
            try {
                shop.save();
            } catch (Exception ignored) {}
        }
    }

    public static void reload(){
        for (CrownSignShop shop: LOADED_SHOPS.values()){
            try {
                shop.reload();
            } catch (Exception ignored) {}
        }
    }
}
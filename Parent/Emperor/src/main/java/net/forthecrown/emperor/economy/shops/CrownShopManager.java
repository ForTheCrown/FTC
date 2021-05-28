package net.forthecrown.emperor.economy.shops;

import net.forthecrown.emperor.Announcer;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

import java.util.*;
import java.util.logging.Level;

public class CrownShopManager implements ShopManager{
    Map<Location, CrownSignShop> loadedShops = new HashMap<>();

    @Override
    public SignShop getShop(Location signShop) { //gets a signshop, throws a null exception if the shop file doesn't exist
        if(loadedShops.containsKey(signShop)) return loadedShops.get(signShop);

        try {
            return new CrownSignShop(signShop);
        } catch (Exception e){
            Announcer.log(Level.SEVERE, e.getMessage());
        }

        return null;
    }

    @Override
    public SignShop createSignShop(Location location, ShopType shopType, Integer price, UUID ownerUUID){ //creates a signshop
        return new CrownSignShop(location, shopType, price, ownerUUID);
    }

    @Override
    public Component getPriceLine(int amount){
        return PRICE_LINE.append(Component.text("$" + amount));
    }

    @Override
    public void save(){
        for (CrownSignShop shop: loadedShops.values()){
            try {
                shop.save();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void reload(){
        for (CrownSignShop shop: loadedShops.values()){
            try {
                shop.reload();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void addShop(CrownSignShop shop){
        loadedShops.put(shop.getLocation(), shop);
    }

    @Override
    public void removeShop(SignShop shop){
        loadedShops.remove(shop.getLocation());
    }

    @Override
    public void clearShops() {
        loadedShops.clear();
    }

    @Override
    public Collection<SignShop> getShops() {
        return new ArrayList<>(loadedShops.values());
    }
}

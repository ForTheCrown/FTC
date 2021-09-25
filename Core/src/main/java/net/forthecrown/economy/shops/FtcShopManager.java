package net.forthecrown.economy.shops;

import net.forthecrown.core.Crown;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

import java.util.*;

public class FtcShopManager implements ShopManager {
    public final Map<WorldVec3i, CrownSignShop> loadedShops = new HashMap<>();
    public final FtcShopInteractionHandler handler = new FtcShopInteractionHandler();

    @Override
    public SignShop getShop(Location signShop) { //gets a signshop, null if not found
        WorldVec3i loc = WorldVec3i.of(signShop);
        if(loadedShops.containsKey(loc)) return loadedShops.get(loc);

        try {
            return new CrownSignShop(loc);
        } catch (Exception e){
            Crown.logger().severe(e.getMessage());
        }

        return null;
    }

    @Override
    public SignShop getShop(String name) {
        return getShop(FtcUtils.filenameToLocation(name));
    }

    @Override
    public SignShop createSignShop(Location location, ShopType shopType, Integer price, UUID ownerUUID){ //creates a signshop
        return new CrownSignShop(WorldVec3i.of(location), shopType, price, ownerUUID);
    }

    @Override
    public Component getPriceLine(int amount){
        return PRICE_LINE
                .append(Component.text("$" + amount)
                        .color(NamedTextColor.BLACK)
                );
    }

    @Override
    public void save(){
        for (CrownSignShop shop: loadedShops.values()){
            try {
                shop.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reload(){
        for (CrownSignShop shop: loadedShops.values()){
            try {
                shop.reload();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addShop(CrownSignShop shop){
        loadedShops.put(shop.getShopLocation(), shop);
    }

    @Override
    public void removeShop(SignShop shop){
        loadedShops.remove(shop.getShopLocation());
    }

    @Override
    public void clearShops() {
        loadedShops.clear();
    }

    @Override
    public Collection<SignShop> getShops() {
        return new ArrayList<>(loadedShops.values());
    }

    @Override
    public ShopInteractionHandler getInteractionHandler() {
        return handler;
    }
}

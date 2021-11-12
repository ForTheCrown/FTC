package net.forthecrown.economy.shops;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.serializer.ShopJsonSerializer;
import net.forthecrown.serializer.ShopSerializer;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class FtcShopManager implements ShopManager {
    private static final ItemStack EXAMPLE_BARRIER = new ItemStackBuilder(Material.BARRIER, 1)
            .setName(Component.text(""))
            .build();

    public final Map<WorldVec3i, SignShop> loadedShops = new Object2ObjectOpenHashMap<>();
    public final FtcShopInteractionHandler handler = new FtcShopInteractionHandler();
    public final ShopJsonSerializer serializer = new ShopJsonSerializer();

    @Override
    public SignShop getShop(WorldVec3i vec) {
        if(loadedShops.containsKey(vec)) return loadedShops.get(vec);

        try {
            FtcSignShop shop = new FtcSignShop(vec);
            addShop(shop);

            return shop;
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public SignShop getShop(String name) {
        return getShop(LocationFileName.parse(name).toLocation());
    }

    @Override
    public SignShop createSignShop(WorldVec3i vec, ShopType type, int price, UUID owner) {
        FtcSignShop shop = new FtcSignShop(vec, type, price, owner);
        addShop(shop);

        return shop;
    }

    @Override
    public Component getPriceLine(int amount){
        return PRICE_LINE
                .append(Component.text("$" + amount)
                        .color(NamedTextColor.BLACK)
                );
    }

    @Override
    public Inventory getExampleInventory() {
        Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, Component.text("Specify what and how much"));
        inv.setItem(0, EXAMPLE_BARRIER);
        inv.setItem(1, EXAMPLE_BARRIER);
        inv.setItem(3, EXAMPLE_BARRIER);
        inv.setItem(4, EXAMPLE_BARRIER);

        return inv;
    }

    @Override
    public void save(){
        for (SignShop shop: loadedShops.values()){
            try {
                shop.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reload(){
        for (SignShop shop: loadedShops.values()){
            try {
                shop.reload();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addShop(SignShop shop){
        loadedShops.put(shop.getPosition(), shop);
    }

    @Override
    public void removeShop(SignShop shop){
        loadedShops.remove(shop.getPosition());
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

    @Override
    public ShopSerializer getSerializer() {
        return serializer;
    }
}

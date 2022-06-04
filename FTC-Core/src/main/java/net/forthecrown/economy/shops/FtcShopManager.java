package net.forthecrown.economy.shops;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.forthecrown.core.Crown;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FtcShopManager implements ShopManager {
    public static final String FILE_HEADER = "!!! This file contains the name of every shop on FTC, DO NOT MODIFY, the shop manager requires this !!!";

    private static final ItemStack EXAMPLE_BARRIER = new ItemStackBuilder(Material.BARRIER, 1)
            .setName(Component.text(""))
            .build();

    @Getter
    public final Set<LocationFileName> allShopNames = new ObjectOpenHashSet<>();
    public final Map<WorldVec3i, SignShop> loadedShops = new Object2ObjectOpenHashMap<>();

    public final FtcShopInteractionHandler handler = new FtcShopInteractionHandler();

    @Override
    public SignShop getShop(WorldVec3i vec) {
        SignShop result = loadedShops.get(vec);
        if(result != null) return result;

        LocationFileName name = LocationFileName.of(vec);
        if(!allShopNames.contains(name)) {
            if (ShopManager.isShop(vec.getBlock())) allShopNames.add(name);
            else return null;
        }

        FtcSignShop shop = new FtcSignShop(vec);
        loadedShops.put(vec, shop);

        return shop;
    }

    @Override
    public SignShop createSignShop(WorldVec3i vec, ShopType type, int price, UUID owner) {
        FtcSignShop shop = new FtcSignShop(vec, type, price, owner);
        loadedShops.put(vec, shop);
        allShopNames.add(shop.getFileName());

        return shop;
    }

    @Override
    public Component getPriceLine(int amount) {
        return ShopConstants.PRICE_LINE
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
    public File shopListFile() {
        return new File(Crown.dataFolder(), "shopList.txt");
    }

    @Override
    public void save() {
        loadedShops.values().forEach(SignShop::update);

        File f = shopListFile();

        try {
            if(!f.exists()) f.createNewFile();

            FileWriter fWriter = new FileWriter(f);
            BufferedWriter writer = new BufferedWriter(fWriter);

            writer.write(FILE_HEADER);

            for (LocationFileName n: allShopNames) {
                writer.newLine();
                writer.write(n.toString());
            }

            writer.close();
            fWriter.close();
        } catch (IOException e) {
            Crown.logger().error("Couldn't save shop list", e);
        }
    }

    @Override
    public void reload() {
        loadedShops.values().forEach(SignShop::load);

        File f = shopListFile();

        if (!f.exists()) {
            allShopNames.clear();
            clearLoaded();

            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));

            reader.lines()
                    .forEach(s -> {
                        if(FILE_HEADER.toLowerCase().contains(s.toLowerCase())) {
                            return;
                        }

                        LocationFileName n = LocationFileName.parse(s);
                        allShopNames.add(n);
                    });
        } catch (IOException e) {
            Crown.logger().error("Couldn't read shop list file", e);
        }
    }

    @Override
    public void clearLoaded() {
        loadedShops.clear();
    }

    @Override
    public void onShopDestroy(SignShop shop) {
        allShopNames.remove(shop.getFileName());
        loadedShops.remove(shop.getPosition());
    }

    @Override
    public ShopInteractionHandler getInteractionHandler() {
        return handler;
    }

    @Override
    public Collection<SignShop> getLoadedShops() {
        return loadedShops.values();
    }

    @Override
    public CompletableFuture<List<SignShop>> getAllShops() {
        return CompletableFuture.supplyAsync(() -> {
            List<SignShop> shops = new ObjectArrayList<>();

            for (LocationFileName f: allShopNames) {
                try {
                    SignShop shop = getShop(f);

                    if(shop == null) continue;
                    shops.add(shop);
                } catch (Exception e) {
                    Crown.logger().error("Error while loading shop " + f, e);
                }
            }

            return shops;
        });
    }
}
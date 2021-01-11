package net.forthecrown.core.economy;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.economy.commands.*;
import net.forthecrown.core.economy.events.SellShopEvents;
import net.forthecrown.core.economy.events.SignShopCreateEvent;
import net.forthecrown.core.economy.events.SignShopDestroyEvent;
import net.forthecrown.core.economy.events.SignShopInteractEvent;
import net.forthecrown.core.economy.files.Balances;
import net.forthecrown.core.economy.files.SignShop;
import net.forthecrown.core.enums.ShopType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Economy {

    private static FtcCore main;
    private static Economy economyMain;
    private Balances balFile;
    private static Map<Material, Integer> defaultItemPrices;

    public Economy(){
        main = FtcCore.getInstance();
        economyMain = this;
        balFile = new Balances();
        defaultItemPrices = new HashMap<>();
        loadDefaultItemPrices();
        Server server = main.getServer();

        server.getPluginManager().registerEvents(new SignShopCreateEvent(), FtcCore.getInstance());
        server.getPluginManager().registerEvents(new SignShopInteractEvent(), FtcCore.getInstance());
        server.getPluginManager().registerEvents(new SignShopDestroyEvent(), FtcCore.getInstance());

        server.getPluginManager().registerEvents(new SellShopEvents(), FtcCore.getInstance());

        server.getPluginCommand("balance").setExecutor(new BalanceCommand());
        //server.getPluginCommand("balancetop").setExecutor(new BalanceTopCommand());

        server.getPluginCommand("pay").setExecutor(new PayCommand());
        server.getPluginCommand("addbalance").setExecutor(new AddBalanceCommand());
        server.getPluginCommand("setbalance").setExecutor(new SetBalanceCommand());

        server.getPluginCommand("shop").setExecutor(new ShopCommand());
        server.getPluginCommand("shop").setTabCompleter(new ShopTabCompleter());
    }

    public static Economy getInstance(){
        return economyMain;
    }

    public void reloadEconomy(){
        loadDefaultItemPrices();
        for(SignShop shop : SignShop.loadedShops){
            shop.reload();
        }
        getBalances().reload();
    }

    public Map<Material, Integer> getItemPrices(){
        return defaultItemPrices;
    }

    public Integer getItemPrice(Material material){
        return defaultItemPrices.get(material);
    }

    public static SignShop getSignShop(Location signShop) throws NullPointerException {
        for(SignShop shop : SignShop.loadedShops){
            if(shop.getBlock() == signShop) return shop;
        }
        return new SignShop(signShop);
    }
    public static SignShop createSignShop(Location location, ShopType shopType, Integer price, UUID ownerUUID){
        return new SignShop(location, shopType, price, ownerUUID);
    }

    public static void saveEconomy(){
        for(SignShop shop : SignShop.loadedShops){
            shop.save();
        }
        getBalances().save();
        System.out.println("[SAVED] Economy saved");
    }

    public static Balances getBalances(){
        return getInstance().balFile;
    }

    private void loadDefaultItemPrices(){
        ConfigurationSection itemPrices = main.getConfig().getConfigurationSection("DefaultPrices");

        for(String s : itemPrices.getKeys(true)){
            Material mat;
            try {
                mat = Material.valueOf(s);
            } catch (Exception e){
                continue;
            }

            defaultItemPrices.put(mat, itemPrices.getInt(s));
        }
    }
}

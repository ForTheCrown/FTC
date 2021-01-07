package net.forthecrown.core.economy;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.economy.commands.BalanceCommand;
import net.forthecrown.core.economy.commands.BalanceTopCommand;
import net.forthecrown.core.economy.files.Balances;
import net.forthecrown.core.economy.files.SignShop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
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

        server.getPluginCommand("balance").setExecutor(new BalanceCommand());
        server.getPluginCommand("balancetop").setExecutor(new BalanceTopCommand());

        //server.getPluginCommand("pay").setExecutor(new PayCommand());
        //server.getPluginCommand("addbalance").setExecutor(new AddBalanceCommand());
        //server.getPluginCommand("setbalance").setExecutor(new SetBalanceCommand());

        //server.getPluginCommand("shop").setExecutor(new ShopCommand());
    }

    public static Economy getInstance(){
        return economyMain;
    }

    public void reloadEconomy(){
        loadDefaultItemPrices();
    }

    public Map<Material, Integer> getItemPrices(){
        return defaultItemPrices;
    }

    public Integer getItemPrice(Material material){
        return defaultItemPrices.get(material);
    }

    public static SignShop getSignShop(Block signShop){
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

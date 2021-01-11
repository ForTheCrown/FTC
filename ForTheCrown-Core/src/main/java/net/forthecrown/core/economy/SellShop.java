package net.forthecrown.core.economy;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.enums.SellAmount;
import net.forthecrown.core.files.FtcUser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class SellShop implements InventoryHolder {

    private final FtcUser user;
    private Inventory inven;

    public SellShop(Player base){
        user = FtcCore.getUserData(base.getUniqueId());
    }
    public SellShop(FtcUser base){
        user = base;
    }

    public Inventory dropsMenu(){
        Inventory inv = getBaseInventory("Mob Drops Shop Menu");
        inv.setItem(4, FtcCore.makeItem(Material.ROTTEN_FLESH, 1, true, "&bDrops"));

        inv.setItem(20, getSellableItem(Material.ROTTEN_FLESH));
        inv.setItem(21, getSellableItem(Material.BONE));
        inv.setItem(22, getSellableItem(Material.ARROW));
        inv.setItem(23, getSellableItem(Material.STRING));
        inv.setItem(24, getSellableItem(Material.SPIDER_EYE));
        inv.setItem(29, getSellableItem(Material.LEATHER));
        inv.setItem(30, getSellableItem(Material.GUNPOWDER));
        inv.setItem(31, getSellableItem(Material.BLAZE_ROD));
        inv.setItem(32, getSellableItem(Material.SLIME_BALL));
        inv.setItem(33, getSellableItem(Material.COD));

        inven = inv;
        return inv;
    }

    public Inventory farmingMenu(){
        Inventory inv = getBaseInventory("Farming Items Shop Menu");
        inv.setItem(4, FtcCore.makeItem(Material.OAK_SAPLING, 1, true, "&bFarming"));

        inv.setItem(20, getSellableItem(Material.BAMBOO));
        inv.setItem(21, getSellableItem(Material.KELP));
        inv.setItem(22, getSellableItem(Material.CACTUS));
        inv.setItem(23, getSellableItem(Material.MELON));
        inv.setItem(24, getSellableItem(Material.VINE));
        inv.setItem(29, getSellableItem(Material.SUGAR_CANE));
        inv.setItem(30, getSellableItem(Material.POTATO));
        inv.setItem(31, getSellableItem(Material.WHEAT));
        inv.setItem(32, getSellableItem(Material.CARROT));
        inv.setItem(33, getSellableItem(Material.PUMPKIN));
        inv.setItem(38, getSellableItem(Material.BEETROOT_SEEDS));
        inv.setItem(39, getSellableItem(Material.BEETROOT));
        inv.setItem(40, getSellableItem(Material.SWEET_BERRIES));
        inv.setItem(41, getSellableItem(Material.CHORUS_FRUIT));
        inv.setItem(42, getSellableItem(Material.WHEAT_SEEDS));

        inven = inv;
        return inv;
    }

    public Inventory miningMenu(){
        Inventory inv = getBaseInventory("Mining Items Shop Menu");
        inv.setItem(4, FtcCore.makeItem(Material.IRON_PICKAXE, 1, true, "&bMining"));

        inv.setItem(11, getSellableItem(Material.LAPIS_LAZULI));
        inv.setItem(12, getSellableItem(Material.QUARTZ));
        inv.setItem(20, getSellableItem(Material.DIAMOND));
        inv.setItem(21, getSellableItem(Material.IRON_INGOT));
        inv.setItem(29, getSellableItem(Material.EMERALD));
        inv.setItem(30, getSellableItem(Material.GOLD_INGOT));
        inv.setItem(38, getSellableItem(Material.COAL));
        inv.setItem(39, getSellableItem(Material.REDSTONE));

        inv.setItem(14, getSellableItem(Material.STONE));
        inv.setItem(15, getSellableItem(Material.ANDESITE));
        inv.setItem(23, getSellableItem(Material.COBBLESTONE));
        inv.setItem(24, getSellableItem(Material.DIORITE));
        inv.setItem(32, getSellableItem(Material.GRAVEL));
        inv.setItem(33, getSellableItem(Material.GRANITE));
        inv.setItem(41, getSellableItem(Material.SAND));
        inv.setItem(42, getSellableItem(Material.DIRT));

        inven = inv;
        return inv;
    }

    public Inventory mainMenu(){
        Inventory inv = Bukkit.createInventory(null, 27, "FTC Shop");

        inv.setItem(11, FtcCore.makeItem(Material.GOLD_BLOCK, 1, true, "&e-Item Shop-", "&7Sell vanilla items."));
        inv.setItem(15, FtcCore.makeItem(Material.EMERALD_BLOCK, 1, true, "&e-Web store-", "&7Online server shop."));

        for(int i = 0; i < 10; i++){
            inv.setItem(i, FtcCore.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
            inv.setItem(i+17, FtcCore.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
        }

        inven = inv;
        return inv;
    }

    public Inventory decidingMenu(){
        Inventory inv = Bukkit.createInventory(null, 27, "FTC Shop");

        inv.setItem(11, FtcCore.makeItem(Material.OAK_SAPLING, 1, true, "&bFarming", "&7Crops and other farmable items."));
        inv.setItem(13, FtcCore.makeItem(Material.IRON_PICKAXE, 1, true, "&bMining", "&7Ores and common blocks."));
        inv.setItem(15, FtcCore.makeItem(Material.ROTTEN_FLESH, 1, true, "&bDrops", "&7Common mobdrops."));

        for(int i = 0; i < 10; i++){
            inv.setItem(i, FtcCore.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
            inv.setItem(i+17, FtcCore.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
        }

        return inv;
    }

    private ItemStack getSellableItem(Material material){
        int price = user.getItemPrice(material);
        SellAmount sellAmount = user.getSellAmount();
        String[] asd = {
                "&eValue: " + price + " Rhines per item,",
                ChatColor.GOLD + "" + price*64 + " Rhines per stack.",
                "&7Amount you will sell: " + sellAmount.toString().replaceAll("PER_", "").toLowerCase() + ".",
                "&7Change the amount setting on the right."
        };
        return FtcCore.makeItem(material, 1, true, null, asd);
    }

    private Inventory getBaseInventory(String menuName){
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', menuName));
        inv.setItem(0, FtcCore.makeItem(Material.PAPER, 1, true, "&e< Previous page"));

        //the following 3  B L O C K S  adds the gray glass panes
        for (int i = 1; i < 9; i++){
            if(i == 0 || i == 4) continue;
            inv.setItem(i, FtcCore.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
        }

        for(int i = 0; i < 9; i++){
            inv.setItem(i+45, FtcCore.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
        }

        inv.setItem(9, FtcCore.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
        inv.setItem(18, FtcCore.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
        inv.setItem(27, FtcCore.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
        inv.setItem(36, FtcCore.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));

        inv.setItem(17, getSellAmountPane(SellAmount.PER_1));
        inv.setItem(26, getSellAmountPane(SellAmount.PER_16));
        inv.setItem(35, getSellAmountPane(SellAmount.PER_64));
        inv.setItem(44, getSellAmountPane(SellAmount.ALL));

        return inv;
    }

    private ItemStack getSellAmountPane(SellAmount paneToGet){
        String[] asd = {"Set the amount of items you", "will sell per click"};
        ItemStack toReturn;
        switch (paneToGet){
            case ALL:
                toReturn = FtcCore.makeItem(Material.BLACK_STAINED_GLASS_PANE, 1, true, "Sell all", asd);
                break;
            case PER_1:
                toReturn = FtcCore.makeItem(Material.BLACK_STAINED_GLASS_PANE, 1, true, "Sell 1", asd);
                break;
            case PER_16:
                toReturn = FtcCore.makeItem(Material.BLACK_STAINED_GLASS_PANE, 1, true, "Sell per 16", asd);
                break;
            case PER_64:
                toReturn = FtcCore.makeItem(Material.BLACK_STAINED_GLASS_PANE, 1, true, "Sell per 64", asd);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + paneToGet);
        }

        if(user.getSellAmount() == paneToGet) toReturn.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        return toReturn;
    }

    @Override
    public Inventory getInventory() {
        return inven;
    }
}

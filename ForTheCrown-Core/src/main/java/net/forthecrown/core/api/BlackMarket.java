package net.forthecrown.core.api;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface BlackMarket extends CrownFileManager{

    Integer getAmountEarned(Material material);

    void setAmountEarned(Material material, Integer amount);

    boolean isSoldOut(Material material);

    Integer getItemPrice(Material material);

    void doEnchantTimer();

    boolean isAllowedToBuyEnchant(Player player);

    void setAllowedToBuyEnchant(Player p, boolean out);

    boolean enchantAvailable();

    void setItemPrice(String branch, Material material, Integer price);

    Integer getEnchantPrice(Enchantment enchantment);

    void setEnchantPrice(Enchantment enchantment, Integer price);

    Enchantment getDailyEnchantment();

    Inventory getDropInventory(CrownUser user);

    Inventory getMiningInventory(CrownUser user);

    Inventory getFarmingInventory(CrownUser user);

    Inventory getEnchantInventory();

    Inventory getParrotInventory();

    ItemStack getDailyEnchantBook();
}

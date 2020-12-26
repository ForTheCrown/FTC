package me.wout.shopsreworked.signshopClicks;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import me.wout.shopsreworked.main;

public class AdminSell extends SignShop {

	public AdminSell(main plugin, File file, YamlConfiguration yaml, String playername, Boolean isShifting, Sign sign) 
	{
		super(plugin, file, yaml, playername, isShifting, sign);
		
		
		// Trying to access shop.
		if (isShifting == true) 
		{
			if (super.getPlayer().isOp() == false) 
			{
				super.rejectAccess();
				return;
			}
			else
			{
				super.getPlayer().openInventory(super.getInv());
				return;
			}
		}
		
		// Trying to buy from shop.
		else 
		{
			super.getPlugin().loadFiles();
			trySell();
			super.getPlugin().saveyaml(super.getPlugin().cashyaml, super.getPlugin().moneyfile);
			super.getPlugin().unloadFiles();
		}
	}

	private void trySell() {
		int price = Integer.parseInt(super.getSign().getLine(3).substring(12));
		  
		if (!super.inventoryHasAllItems(super.getYaml(), super.getPlayer().getInventory())) 
		{
			super.rejectPurchaseNoItems();
			return;
		}
		
		ItemStack item = (ItemStack) super.getYaml().getList("Inventory.shop").get(0);
		if (super.getPlayer().getInventory().containsAtLeast(item, item.getAmount())) 
		{
			super.getPlayer().getInventory().removeItem(item);
			super.getPlayer().sendMessage(ChatColor.GRAY + "You've sold " + ChatColor.YELLOW + item.getType().name().replace("_", " ").toLowerCase() + ChatColor.GRAY + " for " + ChatColor.GOLD + price + " Rhines" + ChatColor.GRAY + ".");
			super.getPlayer().playSound(super.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.3f, 1f);
		}
		
		super.payMoneyForPurchase(-price);
	}
}



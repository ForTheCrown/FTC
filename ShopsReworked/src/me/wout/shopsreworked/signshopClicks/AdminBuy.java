package me.wout.shopsreworked.signshopClicks;

import java.io.File;

import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import me.wout.shopsreworked.main;
import net.md_5.bungee.api.ChatColor;

public class AdminBuy extends SignShop {

	public AdminBuy(main plugin, File file, YamlConfiguration yaml, String playername, Boolean isShifting, Sign sign) 
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
			if (super.checkIfSpaceInInventory(super.getPlayer().getInventory()) == false) 
			{
				super.rejectUseBecauseNoFreeInv();
				return;
			}
			
			super.getPlugin().loadFiles();
			tryBuy();
			super.getPlugin().saveyaml(super.getPlugin().cashyaml, super.getPlugin().moneyfile);
			super.getPlugin().unloadFiles();
		}
	}

	private void tryBuy() {
		int price = Integer.parseInt(super.getSign().getLine(3).substring(12));
		  
		if (!super.enoughMoney(super.getPlayer().getUniqueId(), price)) 
		{
			super.rejectPurchaseNoMoney();
			return;
		}
		
		super.payMoneyForPurchase(price);

		ItemStack itemtobuy = (ItemStack) super.getYaml().getList("Inventory.shop").get(0);
		super.getPlayer().getInventory().addItem(itemtobuy);
		super.getPlayer().sendMessage(ChatColor.GRAY + "You've bought " + ChatColor.YELLOW + itemtobuy.getType().name().replace("_", " ").toLowerCase() + ChatColor.GRAY + " for " + ChatColor.GOLD + price + " Rhines" + ChatColor.GRAY + ".");
		super.getPlayer().playSound(super.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.3f, 1f);
	}

	
	
	

	
}

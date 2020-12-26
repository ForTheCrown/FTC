package me.wout.shopsreworked.signshopClicks;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.wout.shopsreworked.main;

public class GenericSell extends SignShop {

	public GenericSell(main plugin, File file, YamlConfiguration yaml, String playername, Boolean isShifting, Sign sign) 
	{
		super(plugin, file, yaml, playername, isShifting, sign);
		
		// Trying to access shop.
		if (isShifting == true) 
		{
			if (super.getPlayer().isOp() == true || super.isShopOwner(super.getPlayer().getUniqueId().toString())) 
			{
				Inventory temp = super.getInv();
				plugin.editInvs.put(temp, file);
				super.getPlayer().openInventory(temp);
				super.resetColor();
				return;
			}
			else
			{
				super.rejectAccess();
				return;
			}

		}
		
		// Trying to sell to shop.
		else 
		{
			if (super.isBeingEdited()) 
			{
				super.rejectUseBecauseInEdit();
				return;
			}
			if (super.checkIfSpaceInInventory(super.getInv()) == false) 
			{
				super.rejectUseBecauseStockFull();
				super.setColorRed();
				return;
			}
			if (!inventoryHasAllItems(yaml, super.getPlayer().getInventory()))
			{
				super.rejectPurchaseNoItems();
				return;
			}
			
			super.getPlugin().loadFiles();
			trySell();
			super.getPlugin().saveyaml(super.getPlugin().cashyaml, super.getPlugin().moneyfile);
			super.getPlugin().unloadFiles();
		}
	}
			
			
	
	private void trySell() {
		int price = 0;
		try {
			price = Integer.parseInt(super.getSign().getLine(3).substring(12));
		}
		catch (Exception e) {
			super.getPlayer().sendMessage(ChatColor.GRAY + "This shop is broken :(");
			super.getPlayer().sendMessage(ChatColor.GRAY + "To fix this, break and replace it.");
			return;
		}
		
		super.payMoneyForPurchase(-price);
		putItemsInStock();
		
		ItemStack item = (ItemStack) super.getYaml().getList("Inventory.shop").get(0);
		super.getPlayer().getInventory().removeItem(item);
		super.getPlayer().sendMessage(ChatColor.GRAY + "You've sold " + ChatColor.YELLOW + item.getType().name().replace("_", " ").toLowerCase() + ChatColor.GRAY + " for " + ChatColor.GOLD + price + " Rhines" + ChatColor.GRAY + ".");
		super.getPlayer().playSound(super.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.3f, 1f);
		
		takeMoneyFromShopOwner(price, item.getType().name().replace("_", " ").toLowerCase());
	}

	
	private void putItemsInStock() {
		Inventory superr = super.getInv();
		ItemStack item = (ItemStack) super.getYaml().getList("Inventory.shop").get(0);
		
		int amount = item.getAmount();
		superr.addItem(item); // De stomme shit
		item.setAmount(amount);
		
		super.saveInventory(super.getYaml(), superr);
	}
	
	public void takeMoneyFromShopOwner(int price, String itemBought) {
	   String shopOwnerUuid = getYaml().getString("Player");
	   Player shopOwner = Bukkit.getPlayer(UUID.fromString(shopOwnerUuid));
	   
	   getPlugin().cashyaml.set("PlayerData." + shopOwnerUuid, getPlugin().cashyaml.getInt("PlayerData." + shopOwnerUuid) - price);
	   if (shopOwner != null)
	   {
		   shopOwner.sendMessage(ChatColor.GOLD + getPlayer().getPlayerListName() + ChatColor.GRAY + " has sold " + ChatColor.YELLOW + itemBought + ChatColor.GRAY + " to you for " + ChatColor.GOLD + price + " Rhines" + ChatColor.GRAY + ".");
		   shopOwner.playSound(getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 2f);
	   }
	}
	
}

package me.wout.shopsreworked.signshopClicks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.wout.shopsreworked.main;
import net.md_5.bungee.api.ChatColor;

public class SignShop {

	private String playername;
	private YamlConfiguration shopDataYaml;
	private main plugin;
	private Sign sign;
	private File fileWhereYamlIsStored;

	
	public SignShop (main plugin, File file, YamlConfiguration yaml, String playername, Boolean isShifting, Sign sign) 
	{
		this.fileWhereYamlIsStored = file;
		this.plugin = plugin;
		this.playername = playername;
		this.shopDataYaml = yaml;
		this.sign = sign;
	}

	
	public Sign getSign()
	{
		return this.sign;
	}

	public Player getPlayer() 
	{
		if (this.playername == null)
			return null;
		else 
			return Bukkit.getServer().getPlayer(this.playername);
	}
	
	
	public YamlConfiguration getYaml()
	{
		return this.shopDataYaml;
	}
	
	
	public main getPlugin() 
	{
		return this.plugin;
	}
	
	protected File getFile()
	{
		return fileWhereYamlIsStored;
	}
	
	//Gets an inventory containing all items in the yaml-inventory.content-list.
	public Inventory getInv()
	{
		Inventory inv = Bukkit.createInventory(getPlayer(), 27, "Shop content");
		for (int i = 0; i < inv.getSize(); i++) 
		{
			Object item = getYaml().getList("Inventory.content").get(i);
			inv.setItem(i, (ItemStack) item);
		}
		
		return inv;
	}
	
	public void setAllVarsNull() 
	{
		this.playername = null;
		this.shopDataYaml = null;
		this.plugin = null;
		this.sign = null;
	}
	
	
	public boolean isBeingEdited()
	{
		return (getPlugin().editInvs.containsValue(fileWhereYamlIsStored));
	}
	
	
	protected boolean enoughMoney(UUID playerUUID, int price)
	{
		return ((getPlugin().cashyaml.getInt("PlayerData." + playerUUID.toString()) - price) >= 0);
	}

	protected void payMoneyForPurchase(int price) 
	{
		getPlugin().cashyaml.set("PlayerData." + getPlayer().getUniqueId().toString(), getPlugin().cashyaml.getInt("PlayerData." +  getPlayer().getUniqueId().toString()) - price);
		//getPlugin().saveyaml(getPlugin().cashyaml, getPlugin().moneyfile);	
	}
	
	protected void saveInventory(YamlConfiguration yaml, Inventory inv)
	{
		List<ItemStack> content = new ArrayList<ItemStack>();
		for (int i = 0; i < inv.getSize(); i++) 
		{
			content.add(inv.getItem(i));
		}
		yaml.set("Inventory.content", content);	
		getPlugin().saveyaml(yaml, getFile());
	}

	public boolean inventoryHasAllItems(YamlConfiguration yaml, Inventory inv) 
	{
		ItemStack item = (ItemStack) yaml.getList("Inventory.shop").get(0);
		return (inv.containsAtLeast(item, item.getAmount()));
	}
	
	/**
	 * Checks whether there is a null spot in a given inventory.
	 * @param 	inv: The inventory to check.
	 * @return 	true: a free spot has been found.
	 * 			false: no free spot found.
	 */
	public boolean checkIfSpaceInInventory(Inventory inv) {
		int size;
		if (inv instanceof PlayerInventory) size = 36;
		else size = inv.getSize();
		
    	for (int i = 0; i < size; i++) 
    	{
    		if (inv.getItem(i) == null)
    			return true;
    	}
    	return false;
	}
	
	public boolean isShopOwner(String uuid) 
	{
		return (uuid.equals(getYaml().getString("Player")));
	}
	
	public void setColorRed() 
	{
		getSign().setLine(0, ChatColor.RED + getSign().getLine(0).substring(2));
		getSign().update();
	}

	public void resetColor() 
	{
		getSign().setLine(0, ChatColor.DARK_GREEN + getSign().getLine(0).substring(2));
		getSign().update();
	}
	
	
	// Reject sessages
	protected void rejectAccess() 
	{
		this.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to do that.");
	}
	
	protected void rejectPurchaseNoMoney() 
	{
		this.getPlayer().sendMessage(ChatColor.RED + "You don't have enough money to pay for that!");
	}

	protected void rejectUseBecauseInEdit()
	{
		this.getPlayer().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "$" + ChatColor.RESET + ChatColor.GRAY + " This shop is being edited. Please wait a moment.");
	}
	
	
	protected void rejectUseBecauseNoFreeInv() 
	{
		this.getPlayer().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "$" + ChatColor.RESET + ChatColor.GRAY + " You don't have a free spot in your inventory!");
	}
	
	protected void rejectPurchaseNoItems() 
	{
		this.getPlayer().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "$" + ChatColor.RESET + ChatColor.GRAY +  "You don't have all the items in your inventory!");
	}


	protected void rejectUseBecauseStockFull() 
	{
		this.getPlayer().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "$" + ChatColor.RESET + ChatColor.GRAY + " You can't sell to this shop anymore, it's full!");
	}


	public void rejectPurchaseShopOwnerNoMoney() 
	{
		this.getPlayer().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "$" + ChatColor.RESET + ChatColor.GRAY + "The shop owner doesn't have enough money to pay you.");
	}


	
}

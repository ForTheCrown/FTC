package ftc.cosmetics.inventories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ftc.cosmetics.Main;
import net.md_5.bungee.api.ChatColor;

public class CustomInventory {

	public Inventory inv;
	
	private int headItemSlot = 4;
	private ItemStack headItem;
	
	private int returnItemSlot = 0;
	private ItemStack returnItem;
	
	public CustomInventory(int size, String title, boolean needHead, boolean needReturn) {
		this.inv = Bukkit.createInventory(Main.holder, size, title);
		
		if (needHead) this.headItem = Main.plugin.makeItem(Material.NETHER_STAR, 1, ChatColor.YELLOW + "Menu", "", ChatColor.DARK_GRAY + "ulala");
		else this.headItem = getGlassFiller();
		
		if (needReturn) this.returnItem = Main.plugin.makeItem(Material.PAPER, 1, ChatColor.YELLOW + "< Go Back");
		else this.returnItem = getGlassFiller();
	}
	
	
	public int getHeadItemSlot() {
		return this.headItemSlot;
	}
	
	public int getReturnItemSlot() {
		return this.returnItemSlot;
	}
	
	
	public void setHeadItemSlot(int newSlot) {
		if (newSlot >= 0 && newSlot < inv.getSize())
			this.headItemSlot = newSlot;
	}
	
	public void setReturnItemSlot(int newSlot) {
		if (newSlot >= 0 && newSlot < inv.getSize())
			this.returnItemSlot = newSlot;
	}
	
	
	public ItemStack getHeadItem() {
		return this.headItem;
	}
	
	public ItemStack getReturnItem() {
		return this.returnItem;
	}
	
	
	public void setHeadItem(ItemStack item) {
		this.headItem = item;
	}
	
	public void setReturnItem(ItemStack item) {
		this.returnItem = item;
	}
	

	public ItemStack getGlassFiller() {
		return Main.plugin.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, " ");
	}
	
	public Inventory getInventory() {
		return makeInventory();
	}
	
	
	private Inventory makeInventory() {
		Inventory result = this.inv;
		int size = result.getSize();
		ItemStack glass = getGlassFiller();
		
		for (int i = 0; i < size; i += 9) {
			result.setItem(i, glass);
		}
		for (int i = 8; i < size; i += 9) {
			result.setItem(i, glass);
		}
		for (int i = 1; i < 8; i++) {
			result.setItem(i, glass);
		}
		for (int i = size-8; i <= size-2; i++) {
			result.setItem(i, glass);
		}
		
		result.setItem(getHeadItemSlot(), getHeadItem());
		result.setItem(getReturnItemSlot(), getReturnItem());
		return result;
	}
}

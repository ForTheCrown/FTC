package ftc.cosmetics.inventories;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ftc.cosmetics.Main;
import net.md_5.bungee.api.ChatColor;

public class EmoteMenu implements Listener {

	private Inventory inv;
	private String playerUUID;
	
	public EmoteMenu(String playerUUID) {
		CustomInventory cinv = new CustomInventory(36, "Emotes", false, true);
		cinv.setHeadItemSlot(0);
		cinv.setReturnItemSlot(4);
		
		this.inv = cinv.getInventory();
		this.playerUUID = playerUUID;
	}
	
	
	public Inventory getInv() {
		return makeInventory();
	}


	private Inventory makeInventory() {
		Inventory result = this.inv;
		Boolean noEmoter = Main.plugin.getServer().getPluginManager().getPlugin("Chat").getConfig().getStringList("NoEmotes").contains(playerUUID);
		
		List<String> emotes = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + playerUUID + ".EmotesAvailable");
		
		ItemStack noEmote;
		if (noEmoter) noEmote = Main.plugin.makeItem(Material.BARRIER, 1, ChatColor.GOLD + "Emotes Disabled", ChatColor.GRAY + "Rightclick to enable sending and receiving emotes.");
		else noEmote = Main.plugin.makeItem(Material.STRUCTURE_VOID, 1, ChatColor.GOLD + "Emotes Enabled", ChatColor.GRAY + "Rightclick to disable sending and receiving emotes.");
		noEmote.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
		
		ItemStack bonk = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "/bonk", ChatColor.GRAY + "Bonk.");
		ItemStack mwah = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "/mwah", ChatColor.GRAY + "Shower your friends with love.");
		ItemStack poke = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "/poke", ChatColor.GRAY + "Poking someone makes them jump back a bit.");
		
		ItemStack scare = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "/scare", ChatColor.GRAY + "Can be earned around Halloween.");
		if (emotes.contains("SCARE")) scare = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "/scare", ChatColor.GRAY + "Can be earned around Halloween.");
		
		
		result.setItem(12, bonk);
		result.setItem(13, mwah);
		result.setItem(14, poke);
		
		result.setItem(19, scare);
		
		result.setItem(31, noEmote);
		
		return result;
	}

}

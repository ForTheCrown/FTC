package ftc.dataplugin.commands;

import ftc.crownapi.EventApi;
import ftc.crownapi.types.CrownEvent;
import ftc.dataplugin.DataPlugin;
import ftc.dataplugin.FtcUserData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class rank implements CommandExecutor {

	private FtcUserData userData;

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Opens the rank GUI
	 *
	 *
	 * Valid usages of command:
	 * - /rank
	 *
	 * Permissions used:
	 * - NONE
	 *
	 * Referenced other classes:
	 * - DataPlugin: DataPlugin.plugin
	 * - Plugin: CrownEventAPI, Class: CrownEvent, Method: getKing()
	 *
	 * Author: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Check if sender of command is a player.
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can do this.");
			return false;
		}
		Player player = (Player) sender;
		
		if (EventApi.getKing() != null && EventApi.getKing().contains(player.getName()))
		{
			player.sendMessage(ChatColor.GRAY + "[FTC] You are not able to open this, because you won the crown event.");
			return false;
		}
				
		player.openInventory(makeRankInventory(player.getUniqueId(), DataPlugin.getInstance().getActiveBranch(player.getUniqueId()), true));
		
		return true;
	}

	public Inventory makeRankInventory(UUID playeruuid, String branch, Boolean isActiveBranch) {
		Inventory result = createBaseInventory(branch);
		addInvItems(result, playeruuid, branch, isActiveBranch);
		addNextPageItem(result);
		
		if (isActiveBranch) enchantCurrentRank(result, playeruuid);
		else convertToBlackedOutVersion(result);
		return result;
	}
	
	
	private static Inventory createBaseInventory(String branch) {
		Inventory result = Bukkit.createInventory(null, 54, branch + "s");
		
		ItemStack pane = makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, " ");
		
		for (int i = 0; i < 9; i++)
			result.setItem(i, pane);
		for (int i = 45; i < 54; i++)
			result.setItem(i, pane);
		for (int i = 9; i < 54; i += 9)
			result.setItem(i, pane);
		for (int i = 17; i < 54; i += 9)
			result.setItem(i, pane);
		
		return result;
	}
	
	
	private void addInvItems(Inventory invToOpen, UUID playeruuid, String branch, Boolean isActiveBranch) {
		ItemStack mainItem;

		if ("Pirate".equals(branch)) {
			mainItem = makeItem(Material.OAK_BOAT, 1, ChatColor.AQUA + "Pirates", ChatColor.GRAY + "Sailors who bow to no Crown.");
			setPirateRanks(invToOpen, playeruuid, isActiveBranch);
		} else {
			mainItem = makeItem(Material.IRON_SWORD, 1, ChatColor.AQUA + "Knights", ChatColor.GRAY + "The Crown's most loyal players.");
			setKnightRanks(invToOpen, playeruuid, isActiveBranch);
		}
		invToOpen.setItem(4, mainItem);
	
		ItemStack defaultRankItem = makeItem(Material.MAP, 1, "Default", ChatColor.GRAY + "This is default for new players.");
		if (isActiveBranch) makeAvailable(defaultRankItem, true, isActiveBranch);
		invToOpen.setItem(10, defaultRankItem);
	}
	
	private void setKnightRanks(Inventory invToOpen, UUID playeruuid, Boolean isActiveBranch) {
		ItemStack rankItem;
		userData = DataPlugin.getUserData(Bukkit.getPlayer(playeruuid));
		
		// Knight item
		rankItem = makeItem(Material.PAPER, 1, ChatColor.WHITE + "" + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Knight" + ChatColor.DARK_GRAY + "]",
				ChatColor.GRAY + "Acquired by completing the first three",
				ChatColor.GRAY + "levels in the dungeons and giving the",
				ChatColor.GRAY + "apples to Diego in the shop.");
		invToOpen.setItem(12, rankItem);
		
		// Baron items
		rankItem = makeItem(Material.PAPER, 1, ChatColor.WHITE + "" + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Baron" + ChatColor.DARK_GRAY + "]",
				ChatColor.GRAY + "Acquired by paying 500,000 Rhines.");
		invToOpen.setItem(13, rankItem);
		rankItem = makeItem(Material.PAPER, 1, ChatColor.WHITE + "" + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Baroness" + ChatColor.DARK_GRAY + "]",
				ChatColor.GRAY + "Gotten together with the Baron rank.");
		invToOpen.setItem(14, rankItem);
		
		// Lord Items
		rankItem = makeItem(Material.GLOBE_BANNER_PATTERN, 1, ChatColor.WHITE + "" + net.md_5.bungee.api.ChatColor.of("#959595") + "[" + ChatColor.GOLD + "Lord" + net.md_5.bungee.api.ChatColor.of("#959595") + "]",
				ChatColor.GRAY + "Included in the Tier 1 Donator ranks package,",
				ChatColor.GRAY + "which costs €10.00 in the webstore.");
		invToOpen.setItem(29, rankItem);
		rankItem = makeItem(Material.GLOBE_BANNER_PATTERN, 1, ChatColor.WHITE + "" + net.md_5.bungee.api.ChatColor.of("#959595") + "[" + ChatColor.GOLD + "Lady" + net.md_5.bungee.api.ChatColor.of("#959595") + "]",
				ChatColor.GRAY + "Included in the Tier 1 Donator ranks package,",
				ChatColor.GRAY + "which costs €10.00 in the webstore.");
		invToOpen.setItem(38, rankItem);
		
		// Duke Items
		rankItem = makeItem(Material.GLOBE_BANNER_PATTERN, 1, ChatColor.WHITE + "" + net.md_5.bungee.api.ChatColor.of("#bfbfbf") + "[" + net.md_5.bungee.api.ChatColor.of("#ffcd00") + "Duke" + net.md_5.bungee.api.ChatColor.of("#bfbfbf") + "]",
				ChatColor.GRAY + "Included in the Tier 2 Donator ranks package,",
				ChatColor.GRAY + "which costs €20.00 in the webstore.");
		invToOpen.setItem(31, rankItem);
		rankItem = makeItem(Material.GLOBE_BANNER_PATTERN, 1, ChatColor.WHITE + "" + net.md_5.bungee.api.ChatColor.of("#bfbfbf") + "[" + net.md_5.bungee.api.ChatColor.of("#ffcd00") + "Duchess" + net.md_5.bungee.api.ChatColor.of("#bfbfbf") + "]",
				ChatColor.GRAY + "Included in the Tier 2 Donator ranks package,",
				ChatColor.GRAY + "which costs €20.00 in the webstore.");
		invToOpen.setItem(40, rankItem);
		
		// Prince Items
		rankItem = makeItem(Material.GLOBE_BANNER_PATTERN, 1, ChatColor.WHITE + "[" + net.md_5.bungee.api.ChatColor.of("#fbff0f") + "Prince" + ChatColor.WHITE + "]",
				ChatColor.GRAY + "Included in the Tier 3 Donator ranks package,",
				ChatColor.GRAY + "which costs €6.00/month in the webstore.");
		invToOpen.setItem(33, rankItem);
		rankItem = makeItem(Material.GLOBE_BANNER_PATTERN, 1, ChatColor.WHITE + "[" + net.md_5.bungee.api.ChatColor.of("#fbff0f") + "Princess" + ChatColor.WHITE + "]",
				ChatColor.GRAY + "Included in the Tier 3 Donator ranks package,",
				ChatColor.GRAY + "which costs €6.00/month in the webstore.");
		invToOpen.setItem(42, rankItem);
		
		
		// Add loreline to earned ranks
		List<String> knightRanks = userData.getKnightRanks();
		
		if (knightRanks.contains("knight")) {
			makeAvailable(invToOpen.getItem(12), true, isActiveBranch);
		}
		if (knightRanks.contains("baron")) {
			makeAvailable(invToOpen.getItem(13), true, isActiveBranch);
			makeAvailable(invToOpen.getItem(14), true, isActiveBranch);
		}
		if (Bukkit.getPlayer(playeruuid).hasPermission("ftc.donator1")) {
			makeAvailable(invToOpen.getItem(29), false, isActiveBranch);
			makeAvailable(invToOpen.getItem(38), false, isActiveBranch);
		}
		if (Bukkit.getPlayer(playeruuid).hasPermission("ftc.donator2")) {
			makeAvailable(invToOpen.getItem(31), false, isActiveBranch);
			makeAvailable(invToOpen.getItem(40), false, isActiveBranch);
		}
		if (Bukkit.getPlayer(playeruuid).hasPermission("ftc.donator3")) {
			makeAvailable(invToOpen.getItem(33), false, isActiveBranch);
			makeAvailable(invToOpen.getItem(42), false, isActiveBranch);
		}
	}

	private void setPirateRanks(Inventory invToOpen, UUID playeruuid, Boolean isActiveBranch) {
		ItemStack rankItem;
		userData = DataPlugin.getUserData(Bukkit.getPlayer(playeruuid));
		
		// Sailor item
		rankItem = makeItem(Material.PAPER, 1, ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "{" + ChatColor.RESET + ChatColor.GRAY + "Sailor" + ChatColor.DARK_GRAY + ChatColor.BOLD + "}",
				ChatColor.GRAY + "Acquired by gathering 10 pirate points by finding",
				ChatColor.GRAY + "treasures, hunting mob heads or completing",
				ChatColor.GRAY + "some grappling hook challenge levels.");
		invToOpen.setItem(21, rankItem);
		
		// Pirate item
		rankItem = makeItem(Material.PAPER, 1, ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "{" + ChatColor.RESET + ChatColor.GRAY + "Pirate" + ChatColor.DARK_GRAY + ChatColor.BOLD + "}",
				ChatColor.GRAY + "Acquired by gathering 50 pirate points by finding",
				ChatColor.GRAY + "treasures, hunting mob heads or completing",
				ChatColor.GRAY + "some grappling hook challenge levels.");
		invToOpen.setItem(23, rankItem);
		
		// Captain item
		rankItem = makeItem(Material.GLOBE_BANNER_PATTERN, 1, ChatColor.WHITE + "" + net.md_5.bungee.api.ChatColor.of("#bfbfbf") + "{" + net.md_5.bungee.api.ChatColor.of("#ffcd00") + "Captain" + net.md_5.bungee.api.ChatColor.of("#bfbfbf") + "}",
				ChatColor.GRAY + "Included in the Tier 2 Donator ranks package,",
				ChatColor.GRAY + "which costs €20.00 in the webstore.");
		invToOpen.setItem(39, rankItem);
		
		// Admiral item
		rankItem = makeItem(Material.GLOBE_BANNER_PATTERN, 1, ChatColor.WHITE + "{" + ChatColor.YELLOW + "Admiral" + ChatColor.WHITE + "}", // '{&eAdmiral&r} '
				ChatColor.GRAY + "Included in the Tier 3 Donator ranks package,",
				ChatColor.GRAY + "which costs €6.00/month in the webstore.");
		invToOpen.setItem(41, rankItem);
		
		// Add loreline to earned ranks
		List<String> pirateRanks = userData.getPirateRanks();
		
		if (pirateRanks.contains("sailor"))
			makeAvailable(invToOpen.getItem(21), true, isActiveBranch);
		if (pirateRanks.contains("pirate"))
			makeAvailable(invToOpen.getItem(23), true, isActiveBranch);
		
		if (Bukkit.getPlayer(playeruuid).hasPermission("ftc.donator2"))
			makeAvailable(invToOpen.getItem(39), false, isActiveBranch);
		if (Bukkit.getPlayer(playeruuid).hasPermission("ftc.donator3"))
			makeAvailable(invToOpen.getItem(41), false, isActiveBranch);
		
	}
	
	private void enchantCurrentRank(Inventory invToOpen, UUID playeruuid) {
		userData = DataPlugin.getUserData(Bukkit.getPlayer(playeruuid));

		String currentRank = userData.getCurrentRank();
		ItemStack currentRankItem = invToOpen.getItem(DataPlugin.getInstance().getSlotsOfRankItems().get(currentRank));
		if (currentRankItem == null) return;
		
		currentRankItem.addUnsafeEnchantment(Enchantment.CHANNELING, 1); // Adds glare
		
		ItemMeta meta = currentRankItem.getItemMeta(); // Removes last lore line
		List<String> lore = meta.getLore();
		lore.remove(lore.size()-1);
		meta.setLore(lore);
		currentRankItem.setItemMeta(meta);
	}

	private static void addNextPageItem(Inventory invToOpen) {
		ItemStack item = makeItem(Material.PAPER, 1, ChatColor.YELLOW + "Next Page >");
		invToOpen.setItem(8, item);
	}
	
	
	
	private void convertToBlackedOutVersion(Inventory result) {
		ItemStack invItem;
		
		for (int i = 0; i < result.getSize(); i++) {
			invItem = result.getItem(i);
			
			if (invItem == null || invItem.getType() == Material.AIR) {
				invItem = makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, " ");
				result.setItem(i, invItem);
			}
			
			else if (invItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
				invItem.setType(Material.BLACK_STAINED_GLASS_PANE);
			}
		}
	}
	
	
// ************************************ Help functions ************************************ //
	
	// Make an item with a name and lore
	private static ItemStack makeItem(Material material, int amount, String name, String... loreStrings) {
		ItemStack result = new ItemStack(material, amount);
		ItemMeta meta = result.getItemMeta();
		
		if (name != null) meta.setDisplayName(name);
		if (loreStrings != null) {
			List<String> lore = new ArrayList<>(Arrays.asList(loreStrings));
			meta.setLore(lore);
		}
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		
		result.setItemMeta(meta);
		return result;
	}
	
	
	// Adds  "Click to put as active"  lore line
	private static void makeAvailable(ItemStack rankItem, boolean free, boolean isActiveBranch) {
		if (free) rankItem.setType(Material.MAP); // Free ranks become maps if unlocked
		if (!isActiveBranch) return;
		ItemMeta meta = rankItem.getItemMeta();
		List<String> lore = meta.getLore();
		lore.add(net.md_5.bungee.api.ChatColor.of("#73715b") + "Click to make this your active rank.");
		meta.setLore(lore);
		rankItem.setItemMeta(meta);
	}
}

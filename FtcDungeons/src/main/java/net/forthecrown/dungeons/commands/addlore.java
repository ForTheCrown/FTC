package net.forthecrown.dungeons.commands;

import net.forthecrown.dungeons.Dungeons;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class addlore implements CommandExecutor {
    Plugin plugin;

    public addlore(Dungeons plugin) {
        plugin.getCommand("addlore").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Checks if sender is a player.
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Only opped players can do this.");
            return false;
        }
        Player player = (Player) sender;

        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() != Material.GOLDEN_SWORD) return false;

        ItemStack sword = player.getInventory().getItemInMainHand();
        swordSkipLevel(sword, player);
        return true;
    }


    private void swordSkipLevel(ItemStack sword, Player player) {
        if (sword == null) return;
        ItemMeta meta = sword.getItemMeta();
        if (!meta.hasLore()) return;
        List<String> lore = meta.getLore();
        String rankLine = lore.get(0);

        if (rankLine.contains(ChatColor.GRAY + "Rank IX")) {
            actuallyAddLevel(player.getName(), lore, meta, sword, "Rank X", ChatColor.DARK_AQUA + "Max Rank.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank VIII")) {
            actuallyAddLevel(player.getName(), lore, meta, sword, "Rank IX", ChatColor.AQUA + "0/3" + ChatColor.DARK_AQUA + " Withers to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank VII")) {
            actuallyAddLevel(player.getName(), lore, meta, sword, "Rank VIII", ChatColor.AQUA + "0/10" + ChatColor.DARK_AQUA + " Charged Creepers to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank VI")) {
            actuallyAddLevel(player.getName(), lore, meta, sword, "Rank VII", ChatColor.AQUA + "0/50"+ ChatColor.DARK_AQUA + " Ghasts to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank IV")) {
            actuallyAddLevel(player.getName(), lore, meta, sword, "Rank V", ChatColor.AQUA + "0/1000" + ChatColor.DARK_AQUA + " Blazes to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank V")) {
            actuallyAddLevel(player.getName(), lore, meta, sword, "Rank VI", ChatColor.AQUA + "0/1000" + ChatColor.DARK_AQUA + " Endermen to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank III")) {
            actuallyAddLevel(player.getName(), lore, meta, sword, "Rank IV", ChatColor.AQUA + "0/1000" + ChatColor.DARK_AQUA + " Creepers to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank II")) {
            actuallyAddLevel(player.getName(), lore, meta, sword, "Rank III", ChatColor.AQUA + "0/100" + ChatColor.DARK_AQUA + " Snowmen to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank I")) {
            actuallyAddLevel(player.getName(), lore, meta, sword, "Rank II", ChatColor.AQUA + "0/1000" + ChatColor.DARK_AQUA + " Skeletons to Rank Up.");
        }
        else return;

    }

    private void actuallyAddLevel(String player, List<String> lore, ItemMeta meta, ItemStack sword, String newRank, String Upgrade) {
        lore.remove(0);
        lore.add(0, ChatColor.GRAY + newRank);
        lore.remove(5);
        lore.add(5, Upgrade);
        Bukkit.getPlayer(player).playSound(Bukkit.getPlayer(player).getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
        Bukkit.getPlayer(player).playSound(Bukkit.getPlayer(player).getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
        for (int i = 0; i <= 5; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    Bukkit.getPlayer(player).getWorld().spawnParticle(Particle.TOTEM, Bukkit.getPlayer(player).getLocation().getX(), Bukkit.getPlayer(player).getLocation().getY()+2, Bukkit.getPlayer(player).getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 0.275d);
                }
            }, i*5L);
        }

        Bukkit.getPlayer(player).sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.WHITE + "Your Sword was upgraded to " + newRank + "!");
        if (newRank.contains("Rank V") && (!newRank.contains("I"))) {
            meta.setLore(lore);
            sword.setItemMeta(meta);
            sword.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 4);
            Bukkit.getPlayer(player).getInventory().setItemInMainHand(sword);
            Bukkit.getPlayer(player).sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "Looting IV has been added to your Sword.");
        }
        else if (newRank.contains("Rank X")) {
            meta.setLore(lore);
            sword.setItemMeta(meta);
            sword.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 5);
            Bukkit.getPlayer(player).getInventory().setItemInMainHand(sword);
            Bukkit.getPlayer(player).sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "Looting V has been added to your Sword.");

        }

        meta.setLore(lore);
        sword.setItemMeta(meta);
    }
}

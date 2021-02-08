package net.forthecrown.dungeons.commands;

import net.forthecrown.dungeons.Dungeons;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class removeDonator implements CommandExecutor {

    private Dungeons plugin;

    public removeDonator(Dungeons plugin) {
        this.plugin = plugin;
        plugin.getCommand("rankremove").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Checks if sender is a player.
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Only opped players can do this.");
            return false;
        }
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Invalid use of command: " + ChatColor.WHITE + "rankadd [player] [rank]");
            return false;
        }

        String rank = args[1];
        if (!plugin.getConfig().getConfigurationSection("Ranks").getKeys(false).contains(rank)) {
            sender.sendMessage(ChatColor.RED + "Dungeons rankremove error: " + ChatColor.WHITE + rank + " is not a known rank.");
            return false;
        }

        if (!plugin.getConfig().getStringList("Donators").contains(args[0])) {
            sender.sendMessage(ChatColor.RED + "Dungeons rankremove error: " + ChatColor.WHITE + rank + " is not a donator.");
            return false;
        }

        try {
            UUID playerID = Bukkit.getPlayer(args[0]).getUniqueId();
            List<String> list = plugin.getConfig().getStringList("Ranks." + rank);
            list.remove(playerID.toString());
            plugin.getConfig().set("Ranks." + rank, list);
        }
        catch (NullPointerException e) {
            @SuppressWarnings("deprecation")
            UUID playerID = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
            List<String> list = plugin.getConfig().getStringList("Ranks." + rank);
            list.remove(playerID.toString());
            plugin.getConfig().set("Ranks." + rank, list);
        }

        plugin.saveConfig();
        sender.sendMessage(args[0] + " has been remove from the " + rank + " list.");

        return true;
    }
}

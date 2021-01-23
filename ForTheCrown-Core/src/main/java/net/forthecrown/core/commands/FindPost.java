package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FindPost implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Shows the player where the nearest region pole is based
     * on the location from which they executed the command.
     *
     *
     * Valid usages of command:
     * - /findpole
     * - /findpost
     *
     * Referenced other classes:
     * - FtcCore: FtcCore.getPrefix
     * - Posthelp
     *
     * Author: Wout
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sender must be player:
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can do this.");
            return false;
        }

        Player player = (Player) sender;
        Location loc = player.getLocation();

        // Players in the wrong world get information:
        if (loc.getWorld().getName().equals("world_resource")) {
            player.sendMessage(ChatColor.RED + "You are currently in the resource world!");
            player.sendMessage(ChatColor.GRAY + "There are no regions here.");
            player.sendMessage(ChatColor.GRAY + "Try " + ChatColor.YELLOW + "/warp portal" + ChatColor.GRAY + " to get back to the normal world.");
            player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.YELLOW + "/posthelp" + ChatColor.GRAY + " for more help.");
            return false;
        }
        else if (loc.getWorld().getName().contains("world_")) {
            player.sendMessage(ChatColor.RED + "You are currently not in the world with regions!");
            player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.YELLOW + "/posthelp" + ChatColor.GRAY + " for more help.");
            return false;
        }

        // Calculate closed pole:
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int x_pole;
        int z_pole;

        if (x % 400 > 200) {
            x_pole = x - ((x % 400) - 200);
        } else {
            x_pole = x + (200 - (x % 400));
        }
        if (z % 400 > 200) {
            z_pole = z - ((z % 400) - 200);
        } else {
            z_pole = z + (200 - (z % 400));
        }
        if (x < 0) x_pole -= 400;
        if (z < 0) z_pole -= 400;

        player.sendMessage(FtcCore.getPrefix()+ ChatColor.YELLOW + "The region pole closest to you:");
        player.sendMessage(ChatColor.YELLOW + "x = " + x_pole + ", z = " + z_pole);

        return true;
    }
}

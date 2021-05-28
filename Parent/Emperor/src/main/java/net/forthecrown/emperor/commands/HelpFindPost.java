package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HelpFindPost extends CrownCommandBuilder {
    public HelpFindPost() {
        super("findpost", CrownCore.inst());

        setAliases("findpole");
        setDescription("Shows you the nearest region pole.");
        setPermission(Permissions.HELP);

        register();
    }

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
    protected void createCommand(BrigadierCommand command) {
        command.executes(c ->{
            Player player = getPlayerSender(c);

            Location loc = player.getLocation();

            // Players in the wrong world get information:
            if (loc.getWorld().getName().equals("world_resource")) {
                player.sendMessage(ChatColor.RED + "You are currently in the resource world!");
                player.sendMessage(ChatColor.GRAY + "There are no regions here.");
                player.sendMessage(ChatColor.GRAY + "Try " + ChatColor.YELLOW + "/warp portal" + ChatColor.GRAY + " to get back to the normal world.");
                player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.YELLOW + "/posthelp" + ChatColor.GRAY + " for more help.");
                return 0;
            }
            else if (loc.getWorld().getName().contains("world_")) {
                player.sendMessage(ChatColor.RED + "You are currently not in the world with regions!");
                player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.YELLOW + "/posthelp" + ChatColor.GRAY + " for more help.");
                return 0;
            }

            // Calculate closest pole:
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

            player.sendMessage(CrownCore.getPrefix()+ ChatColor.YELLOW + "The region pole closest to you:");
            player.sendMessage(ChatColor.YELLOW + "x = " + x_pole + ", z = " + z_pole);

            return 0;
        });
    }
}

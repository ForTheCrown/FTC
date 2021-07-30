package net.forthecrown.commands.help;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.RegionCords;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HelpFindPost extends FtcCommand {
    public HelpFindPost() {
        super("findpost", ForTheCrown.inst());

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
            } else if (loc.getWorld().getName().contains("world_")) {
                player.sendMessage(ChatColor.RED + "You are not currently in the world with regions!");
                player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.YELLOW + "/posthelp" + ChatColor.GRAY + " for more help.");
                return 0;
            }

            RegionCords cords = RegionCords.of(loc);

            player.sendMessage(ForTheCrown.getPrefix()+ ChatColor.YELLOW + "The region pole closest to you:");
            player.sendMessage(ChatColor.YELLOW + "x = " + cords.getAbsoluteX() + ", z = " + cords.getAbsoluteZ());

            return 0;
        });
    }
}

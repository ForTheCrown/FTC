package net.forthecrown.commands.help;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.core.Worlds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HelpFindPost extends FtcCommand {
    public HelpFindPost() {
        super("findpost", Crown.inst());

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
            if (loc.getWorld().equals(Worlds.RESOURCE)) {
                player.sendMessage(ChatColor.RED + "You are currently in the resource world!");
                player.sendMessage(ChatColor.GRAY + "There are no regions here.");
                player.sendMessage(ChatColor.GRAY + "Try " + ChatColor.YELLOW + "/warp portal" + ChatColor.GRAY + " to get back to the normal world.");
                player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.YELLOW + "/posthelp" + ChatColor.GRAY + " for more help.");
                return 0;
            } else if (!loc.getWorld().equals(Worlds.OVERWORLD)) {
                player.sendMessage(ChatColor.RED + "You are not currently in the world with regions!");
                player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.YELLOW + "/posthelp" + ChatColor.GRAY + " for more help.");
                return 0;
            }

            RegionPos cords = RegionPos.of(loc);
            PopulationRegion region = Crown.getRegionManager().get(cords);
            Crown.getRegionManager().getGenerator().generate(region);
            
            BlockVector2 vec2 = region.getPolePosition();

            player.sendMessage(
                    Component.text()
                            .append(Crown.prefix())
                            .append(Component.text("Closest region pole: ").color(NamedTextColor.YELLOW))
                            .append(Component.newline())
                            .append(Component.text("x= " + vec2.getX() + " z= " + vec2.getZ()))
            );

            return 0;
        });
    }
}

package net.forthecrown.commands.help;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionConstants;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.RegionVisit;
import net.forthecrown.user.actions.UserActionHandler;
import net.md_5.bungee.api.ChatColor;

public class HelpSpawn extends FtcCommand {

    public HelpSpawn(){
        super("spawn", Crown.inst());

        setPermission(Permissions.HELP);
        setDescription("Shows info about spawn");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Explains how to get to spawn.
     *
     *
     * Valid usages of command:
     * - /spawn
     *
     * Referenced other classes:
     * - FtcCore: FtcCore.getPrefix
     * - Findpost
     * - Posthelp
     *
     * Author: Wout
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c ->{
            CrownUser sender = getUserSender(c);

            PopulationRegion region = Crown.getRegionManager().get(ComVars.getSpawnRegion());
            if (region != null) {
                BlockVector2 pole = region.getPolePosition();
                BlockVector2 senderPos = sender.get2DLocation();

                if(pole.distance(senderPos) < RegionConstants.DISTANCE_TO_POLE) {
                    RegionVisit action = new RegionVisit(sender, region);
                    UserActionHandler.handleAction(action);

                    return 0;
                }
            }

            // Information:
            sender.sendMessage(Crown.getPrefix() + ChatColor.YELLOW + "Information about spawn:");
            sender.sendMessage("Spawn is called Hazelguard, you can tp using region poles.");
            sender.sendMessage("Use " + ChatColor.YELLOW + "/findpole" + ChatColor.RESET + " to find the closest pole.");
            sender.sendMessage("Then, use " + ChatColor.YELLOW + "/visit Hazelguard" + ChatColor.RESET + " to travel to spawn.");
            sender.sendMessage(ChatColor.GRAY + "If you need more help, use /posthelp.");

            return 0;
        });
    }
}

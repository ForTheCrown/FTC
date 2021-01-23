package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.inventories.RankInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Gives the executor the RankGUI and allows them to switch ranks
     *
     *
     * Valid usages of command:
     * - /rank
     * - /ranks
     * - /rankgui
     * - /rankui
     *
     * Author: Wout
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players may execute this command!");
            return false;
        }

        if(FtcCore.getKing() == ((Player) sender).getUniqueId()){
            sender.sendMessage("Kings and Queens aren't allowed to change their titles");
            return false;
        }

        Player player = (Player) sender;
        RankInventory rI = new RankInventory(FtcCore.getUser(player.getUniqueId()));
        player.openInventory(rI.getUsersRankGUI());
        return true;
    }
}

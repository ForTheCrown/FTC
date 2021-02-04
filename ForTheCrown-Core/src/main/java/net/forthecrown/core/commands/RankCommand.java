package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.core.inventories.RankInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand extends CrownCommand {

    public RankCommand(){
        super("rank", FtcCore.getInstance());

        setAliases("ranks");
        register();
    }

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
     *
     * Author: Wout
     */

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        if(FtcCore.getKing() == ((Player) sender).getUniqueId()) throw new InvalidCommandExecution(sender, "&eKings &7and &eQueens &7cannot change their titles!");

        Player player = (Player) sender;
        RankInventory rI = new RankInventory(FtcCore.getUser(player.getUniqueId()));
        player.openInventory(rI.getUsersRankGUI());
        return true;
    }
}

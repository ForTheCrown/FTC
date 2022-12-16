package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.RankMenu;
import net.forthecrown.user.Users;
import org.bukkit.entity.Player;

public class CommandRank extends FtcCommand {

    public CommandRank(){
        super("rank");

        setAliases("ranks");
        setDescription("Allows you to set your rank");

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
    protected void createCommand(BrigadierCommand command) {
        command.executes(c ->{
            Player player = c.getSource().asPlayer();
            RankMenu.openInventory(Users.get(player));
            return 0;
        });
    }
}
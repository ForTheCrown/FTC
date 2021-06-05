package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.events.RankGuiUseEvent;
import net.forthecrown.emperor.inventory.RankInventory;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandRank extends FtcCommand {

    public CommandRank(){
        super("rank", CrownCore.inst());

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
            Player player = getPlayerSender(c);

            Bukkit.getPluginManager().registerEvents(new RankGuiUseEvent(player), CrownCore.inst());
            player.openInventory(new RankInventory(UserManager.getUser(player)).getUsersRankGUI());
            return 0;
        });
    }
}

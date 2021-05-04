package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.events.RankGuiUseEvent;
import net.forthecrown.core.inventories.RankInventory;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandRank extends CrownCommandBuilder {

    public CommandRank(){
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
    protected void createCommand(BrigadierCommand command) {
        command.executes(c ->{
            Player player = getPlayerSender(c);

            Bukkit.getPluginManager().registerEvents(new RankGuiUseEvent(player), FtcCore.getInstance());
            player.openInventory(new RankInventory(UserManager.getUser(player)).getUsersRankGUI());
            return 0;
        });
    }
}

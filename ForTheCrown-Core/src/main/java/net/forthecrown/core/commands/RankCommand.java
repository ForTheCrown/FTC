package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.events.RankGuiUseEvent;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.core.inventories.RankInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

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
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        Player player = (Player) sender;

        FtcCore.getInstance().getServer().getPluginManager().registerEvents(new RankGuiUseEvent(player), FtcCore.getInstance());
        player.openInventory(new RankInventory(FtcCore.getUser(player)).getUsersRankGUI());
        return true;
    }
}

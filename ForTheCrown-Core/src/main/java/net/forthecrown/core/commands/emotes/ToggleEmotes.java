package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.core.files.FtcUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleEmotes implements CrownCommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Adds player to config list "NoEmotes", which
     * disables receiving and sending these emotes:
     * 	- mwah
     * 	- poke
     * 	- bonk
     *
     *
     * Valid usages of command:
     * - /toggleemotes
     *
     *
     * Referenced other classes:
     * - FtcUserData
     * - FtcCore
     *
     * Main Author: Botul
     */

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        Player player = (Player) sender;
        FtcUser user = FtcCore.getUser(player.getUniqueId());
        String message = "&7You can longer send or receive emotes.";

        user.setAllowsEmotes(!user.allowsEmotes());
        if(user.allowsEmotes()) message = "&eYou can now send and receive emotes :D";

        user.sendMessage(message);
        return true;
    }
}

package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleEmotes extends CrownCommand {

    public ToggleEmotes(){
        super("toggleemotes", FtcCore.getInstance());

        setPermission("ftc.emotes");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Adds player to config list "NoEmotes", which
     * disables receiving and sending these emotes:
     * 	- mwah
     * 	- poke
     * 	- bonk
     *  - jingle
     *  - scare
     *  - hug
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
        CrownUser user = FtcCore.getUser(player);
        String message = "&7You can longer send or receive emotes.";

        user.setAllowsEmotes(!user.allowsEmotes());
        if(user.allowsEmotes()) message = "&eYou can now send and receive emotes :D";

        user.sendMessage(message);
        return true;
    }
}

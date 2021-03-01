package net.forthecrown.core.commands;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

public class _template extends CrownCommand{
    public _template(Plugin plugin){
        super("COMMAND_NAME", plugin);

        register();
    }

    /*
     * This legacy stuff lol
     * Get with the times old man B)
     * We usin' Brigadier now lol
     */

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     *
     * Valid usages of command:
     *
     *
     * Referenced other classes:
     *
     *
     * Main Author:
     * Edit:
     */

    /*
     * Permissions are specified in the constructor with setPermission(String permissionName);
     * The usage message is set likewise, with setUsage(String usage);
     *
     * By default, the permission will be ftc.commands.COMMANDNAME
     * A TabCompleter can be set with setTabCompleter lol
     */

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        //Checks if the command sender is a player
        //As long as your using the CrownCommand, CrownExceptions won't cause any real issues, they'll just stop the code's execution
        //and send the executor a message

        //fun fact, sender instanceof CrownUser is valid, but will always return false lol

        //Checks if the sender is on cooldown for this command
        if(Cooldown.contains(sender, "PLUGIN_Commands_COMMANDNAME")) throw new InvalidCommandExecution(sender, "&7You're currently on cooldown :p");
        Cooldown.add(sender, "PLUGIN_Commands_COMMANDNAME", 3*20);
        //The category, aka PLUGIN_Commands_COMMANDNAME, can actually be named whatever you want, you can even just leave the category blank

        Player player = (Player) sender;
        CrownUser user = FtcCore.getUser(player);

        return true; //If a usage message is set, then returning false will send the sender the usage message
    }
}

package ftc.crownapi.commands;

import ftc.crownapi.EventApi;
import ftc.crownapi.types.interfaces.CrownEventIUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisqualifyCommand implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * This command disqualifies a player from participating
     * in the current CrownEvent
     *
     *
     * Valid usages of command:
     * - /disqualify <player name>
     *
     * Permissions used:
     * - crownapi.admin
     *
     * Referenced other classes:
     * - NONE
     *
     * Author: Botul
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1){
            sender.sendMessage("You must specify a player's name");
            return false;
        }

        Player target;
        try {
            target = Bukkit.getPlayer(args[0]);
        } catch (NullPointerException e){
            sender.sendMessage(args[0] + "is not a valid player!");
            return false;
        }
        CrownEventIUser user = EventApi.getInstance().getApiUser(target);
        final boolean notAlreadyDissed;
        final String senderMessage;
        final String targetMessage;

        if(user.isDisqualified()) {
            notAlreadyDissed = false;
            senderMessage = args[0] + " is now allowed to participate in Crown Events again!";
            targetMessage = "You are allowed to participate in Crown Events again!";
        } else {
            notAlreadyDissed = true;
            senderMessage = args[0] + " is now disqualified from participating in Crown Events!";
            targetMessage = "You have been disqualified from participating in any Crown Events!";
        }

        user.setDisqualified(notAlreadyDissed);
        sender.sendMessage(senderMessage);
        target.sendMessage(targetMessage);
        return false;
    }
}

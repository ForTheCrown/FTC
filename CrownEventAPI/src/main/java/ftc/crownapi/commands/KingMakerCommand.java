package ftc.crownapi.commands;

import ftc.crownapi.EventApi;
import ftc.crownapi.config.CrownMessages;
import ftc.crownapi.types.CrownEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KingMakerCommand implements CommandExecutor {

    private final EventApi main;
    private final CrownEvent crownEvent;

    public KingMakerCommand(EventApi main, CrownEvent crownEvent) {
        this.crownEvent = crownEvent;
        this.main = main;
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * This command is used for making someone king or removing a king
     *
     *
     * Valid usages of command:
     * - /kingmaker <player name> <king | queen>
     * - /kingmaker <remove>
     *
     * Permissions used:
     * - crownapi.kingmaker
     *
     * Referenced other classes:
     * - EventApi - main
     * - CrownEvent - crownEvent
     *
     * Author: Botul
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String message = crownEvent.getMessage(CrownMessages.KING);
        String senderMessage = crownEvent.getMessage(CrownMessages.SENDER_KING);
        String tooLittleArgs = crownEvent.getMessage(CrownMessages.TOO_LITTLE_ARGS);
        String kingRemoved = crownEvent.getMessage(CrownMessages.KING_REMOVED);

        if(args.length < 1){
            sender.sendMessage(tooLittleArgs);
            sender.sendMessage("Use these arguments: /kingmaker <Playername | remove> [king | queen]");
            return false;
        }
        if(args[0].contains("remove")){
            sender.sendMessage(kingRemoved);
            EventApi.setKing("FTCempty");
            EventApi.saveCrownConfig();
            return true;
        }

        //if there's already a king or queen
        String prevKing = EventApi.getKing();
        if(prevKing != null && !prevKing.contains("empty")){
            sender.sendMessage("You must remove the previous king/queen first!");
            return false;
        }

        //assigns the player and checks if they're online
        final Player target;
        try {
            target = Bukkit.getPlayer(args[0]);
        } catch (Exception e){
            sender.sendMessage("That is not a valid player");
            return false;
        }
        //sets the king to be the target
        EventApi.setKing(target.getUniqueId().toString());
        EventApi.saveCrownConfig();

        //checks if the person specified is a king or queen
        // if no king or queen is specified, it defaults to king
        String title;
        if(args.length != 2 || args[1].contains("king")){
            message = message.replaceAll("%TITLE%", "King");
            senderMessage = senderMessage.replaceAll("%TITLE%", "King");
            title = "King";
        } else {
            message = message.replaceAll("%TITLE%", "Queen");
            senderMessage = senderMessage.replaceAll("%TITLE%", "Queen");
            title = "Queen";
        }
        senderMessage = senderMessage.replaceAll("%NAME%", target.getName());

        sender.sendMessage(senderMessage);
        target.sendMessage(message);
        Bukkit.dispatchCommand(main.getServer().getConsoleSender(), "tab player " + target.getName() + " tabprefix &l[&e&l" + title + "&r&l] &r");
        return true;
    }
}

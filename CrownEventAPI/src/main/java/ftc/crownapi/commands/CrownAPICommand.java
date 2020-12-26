package ftc.crownapi.commands;

import ftc.crownapi.settings.CrownBooleanSettings;
import ftc.crownapi.settings.CrownSettings;
import ftc.crownapi.types.CrownEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrownAPICommand implements CommandExecutor {

    private static final CrownEvent crownMain = new CrownEvent();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1){
            sender.sendMessage("Please use one of the following arguments");
            sender.sendMessage("settings | debug | modifiers");
        }
        switch (args[0]){
            default:
                sender.sendMessage("Incorrect argument!");
                return false;
            case "settings":
                if(args.length < 2){
                    sender.sendMessage("Too little arguments, please use one of the following: <list | set>");
                    return false;
                }
                switch (args[1]){
                    case "list":
                        sender.sendMessage("Current settings:");
                        sender.sendMessage("Cumulative Points: " + CrownSettings.getBooleanSetting(CrownBooleanSettings.CUMULATIVE_POINTS));
                        sender.sendMessage("Remove players from event on quit: " + CrownSettings.getBooleanSetting(CrownBooleanSettings.REMOVE_ON_QUIT));
                        sender.sendMessage("Remove players from event on server shutdown: " +  CrownSettings.getBooleanSetting(CrownBooleanSettings.REMOVE_ON_SHUTDOWN));
                        sender.sendMessage("Teleport players to spawn after finishing event: " + CrownSettings.getBooleanSetting(CrownBooleanSettings.TO_SPAWN_ON_END));
                        return true;
                    case "set":
                        return true;
                    default:
                        sender.sendMessage("Incorrect argument!");
                        return false;
                }
            case "debug":
                if(!(sender instanceof Player)){
                    sender.sendMessage("Only the player may use the debug arguments");
                    return false;
                }
            case "modifiers":
                if(!(sender instanceof Player)){
                    sender.sendMessage("Only the player may use the modifiers arguments");
                    return false;
                }
                return true;
        }
    }
}

//Args: settings | debug | modifiers
//settings args: <list | set>, <set> args: <setting name>
//debug args: Basically every method from CrownEvent and CrownEventUser
//modifier args: <list | set>, <set> args: <lobby_location | event_location | >
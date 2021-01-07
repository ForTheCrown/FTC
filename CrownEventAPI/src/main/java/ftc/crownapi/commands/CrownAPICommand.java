package ftc.crownapi.commands;

import ftc.crownapi.EventApi;
import ftc.crownapi.config.CrownBooleanSettings;
import ftc.crownapi.config.CrownMessages;
import ftc.crownapi.types.CrownEvent;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrownAPICommand implements CommandExecutor {

    private final CrownEvent crownEvent;
    private final EventApi main;

    public CrownAPICommand(EventApi main, CrownEvent crownMain){
        this.main = main;
        this.crownEvent = crownMain;
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * This command disqualifies a player from participating
     * in the current CrownEvent. This feels like a mess
     *
     *
     * Valid usages of command:
     * - /crownapi settings <set> <setting> <value>
     * - /crownapi settings <list>
     * - /crownapi modifiers <list>
     * - /crownapi modifiers <set>
     * - /crownapi reload
     *
     * Permissions used:
     * - crownapi.admin
     *
     * Referenced other classes:
     * - CrownEvent - crownEvent
     * - EventApi - main
     *
     * Author: Botul
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final boolean playerSender = sender instanceof Player;
        String invalidArg = crownEvent.getMessage(CrownMessages.INVALID_ARG);
        String tooLittleArgs = crownEvent.getMessage(CrownMessages.TOO_LITTLE_ARGS);

        if(args.length <= 0){
            sender.sendMessage("Please use one of the following arguments");
            sender.sendMessage("settings | debug | modifiers");
            return false;
        }

        switch (args[0]){
            default:
                sender.sendMessage(invalidArg);
                return false;

            case "settings":
                if(args.length < 2){
                    sender.sendMessage(tooLittleArgs +" Please use one of the following: <list | set>");
                    return false;
                }

                switch (args[1]){
                    case "list":
                        sender.sendMessage("Current settings:");
                        sender.sendMessage("Cumulative Points: " + crownEvent.getSetting(CrownBooleanSettings.CUMULATIVE_POINTS));
                        sender.sendMessage("Remove players from event on quit: " + crownEvent.getSetting(CrownBooleanSettings.REMOVE_ON_QUIT));
                        sender.sendMessage("Remove players from event on server shutdown: " +  crownEvent.getSetting(CrownBooleanSettings.REMOVE_ON_SHUTDOWN));
                        sender.sendMessage("Teleport players to spawn after finishing event: " + crownEvent.getSetting(CrownBooleanSettings.TO_SPAWN_ON_END));
                        return true;

                    case "set":
                        if(args.length < 4){
                            sender.sendMessage(tooLittleArgs);
                            return false;
                        }

                        //assigns the variable
                        boolean value;
                        CrownBooleanSettings setting;
                        try{
                            setting = CrownBooleanSettings.valueOf(args[2].toUpperCase());
                            value = Boolean.parseBoolean(args[3]);
                        } catch (Exception e){
                            sender.sendMessage(invalidArg);
                            return false;
                        }

                        //sets the variable
                        crownEvent.setSetting(setting, value);
                        return true;

                    default:
                        sender.sendMessage(invalidArg);
                        return false;
                }

            case "modifiers": //stuff like the lobby location and start location
                if(args.length < 2 ){
                    sender.sendMessage(tooLittleArgs);
                    return false;
                }
                if(args[1].contains("list")){
                    Location loc = crownEvent.getLobbyLocation();
                    Location loc1 = crownEvent.getStartLocation();
                    String message = loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ();
                    String message1 = loc1.getWorld().getName() + " " + loc1.getX() + " " + loc1.getY() + " " + loc1.getZ();

                    sender.sendMessage("Lobby location: " + message);
                    sender.sendMessage("Start location: " + message1);
                    return true;
                }
                if(!playerSender){
                    sender.sendMessage("Only the player may use the modifiers arguments");
                    return false;
                }
                Player player = (Player) sender;

                Location loc = new Location(player.getWorld() ,player.getLocation().getBlockX() + 0.5, player.getLocation().getBlockY(), player.getLocation().getBlockZ() + 0.5);
                if(args[1].contains("start-location")) crownEvent.setStartLocation(loc);
                else crownEvent.setLobbyLocation(loc);
                return true;

            case "reload":
                main.reloadConfig();
                crownEvent.reloadSettings();
                sender.sendMessage(crownEvent.getMessage(CrownMessages.PLUGIN_RELOADED));
                return true;
        }
    }
}

//Args: settings | debug | modifiers
//settings args: <list | set>, <set> args: <setting name>
//debug args: Basically every method from CrownEvent and CrownEventUser
//modifier args: <list | set>, <set> args: <lobby_location | event_location | >
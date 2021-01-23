package net.forthecrown.mazegen;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MazeGenCommand implements CommandExecutor {

    private final Main main;
    public MazeGenCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if(args.length < 2){
            switch (args[0]){
                case "pos1":
                    Location loc = new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                    main.pos1 = loc;
                    player.sendMessage("Pos1 set!");
                    break;
                case "pos2":
                    Location loc1 = new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                    main.pos2 = loc1;
                    player.sendMessage("Pos2 set!");
                    break;
                case "entrypos":
                    Location loc2 = new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, player.getLocation().getBlockY(), player.getLocation().getBlockZ() + 0.5);
                    main.entryLoc = loc2;
                    player.sendMessage("EntryPos set!");
                    break;
                case "test":
                    main.enterEvent(player);
                    player.sendMessage("Attempting to enter event!");
                    break;
                case "generate":
                    main.generateMaze();
                    main.calculateHeightWidth(main.pos1, main.pos2);
                    player.sendMessage("Starting maze generation!");
                    break;
                case "gamegen":
                    main.generateIngameMaze();
                    player.sendMessage("Starting ingame generation");
                    break;
                default:
                    player.sendMessage("Wrong argument!");
                    return false;
            }
        }
        return true;
    }
}

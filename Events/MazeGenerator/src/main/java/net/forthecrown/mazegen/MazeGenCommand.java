package net.forthecrown.mazegen;

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
                case "test":
                    main.enterEvent(player);
                    player.sendMessage("Attempting to enter event!");
                    break;
                case "generate":
                    main.calculateHeightWidth(Main.POS_1, Main.POS_2);
                    main.generateMaze();
                    player.sendMessage("Maze generation complete!");
                    break;
                case "gamegen":
                    main.generateIngameMaze(null);
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

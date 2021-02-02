package net.forthecrown.mazegen;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveMazeCommand implements CommandExecutor {

    private final Main main;
    public LeaveMazeCommand(Main main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if(main.inMaze == null || main.inMaze != player) return false;

        main.endEvent(player);
        player.sendMessage(ChatColor.GRAY + "You're out of the maze!");
        return true;
    }
}

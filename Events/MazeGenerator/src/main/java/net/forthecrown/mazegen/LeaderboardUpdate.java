package net.forthecrown.mazegen;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LeaderboardUpdate implements CommandExecutor {

    private final Main main;
    public LeaderboardUpdate(Main main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        main.reloadLeaderboard();
        return true;
    }
}

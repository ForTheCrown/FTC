package ftc.bigcrown.commands;

import ftc.bigcrown.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class MyScoreCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        //gets the scores
        Scoreboard scoreboard = Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
        Objective crown = scoreboard.getObjective("crown");
        int batScr = scoreboard.getObjective("batsKilledRecord").getScore(player.getName()).getScore();
        int endScr = scoreboard.getObjective("endKilledRecord").getScore(player.getName()).getScore();
        int pinScr = scoreboard.getObjective("pinataHitsRecord").getScore(player.getName()).getScore();
        int haroldScr = scoreboard.getObjective("zombieKillRecord").getScore(player.getName()).getScore();
        int raidScr = scoreboard.getObjective("raidTimes").getScore(player.getName()).getScore();

        //sends the scores
        player.sendMessage(ChatColor.YELLOW + "Your current crown scores:");
        player.sendMessage(getTimeTakenClock(raidScr).toString());
        player.sendMessage(ChatColor.GRAY +"Bats killed: " + ChatColor.WHITE + batScr);
        player.sendMessage(ChatColor.GRAY +"Endermen killed: " + ChatColor.WHITE + endScr);
        player.sendMessage(ChatColor.GRAY +"(Harold) Zombies killed: " + ChatColor.WHITE + haroldScr);
        player.sendMessage(ChatColor.GRAY +"Pinata hits: " + ChatColor.WHITE + pinScr);
        player.sendMessage(ChatColor.GRAY +"Bats killed: " + ChatColor.WHITE + batScr);

        return true;
    }

    public StringBuilder getTimeTakenClock(int timeTaken){
        int minutes = (timeTaken /60000) % 60;
        int seconds = (timeTaken / 1000) % 60;
        int milliseconds = (timeTaken/100 ) % 100;

        StringBuilder message = new StringBuilder(ChatColor.GRAY + "Best Raid Castle time:" + ChatColor.WHITE + " ");
        message.append(String.format("%02d", minutes)).append(":");
        message.append(String.format("%02d", seconds)).append(":");
        message.append(String.format("%02d", milliseconds));
        return message;
    }

}

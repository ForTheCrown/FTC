package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KingMakerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1) return false;

        if(args[0].contains("remove")){
            if(FtcCore.getKing() == null){
                sender.sendMessage("There already is no king!");
                return true;
            }

            Bukkit.dispatchCommand(sender, "tab player " + Bukkit.getOfflinePlayer(FtcCore.getKing()).getName() + " tabprefix");
            FtcCore.setKing(null);
            sender.sendMessage("King has been removed!");
            return true;
        } else {
            Player player;
            try {
                player = Bukkit.getPlayer(args[0]);
            } catch (NullPointerException e){ return false; }

            if(FtcCore.getKing() != null){
                sender.sendMessage(ChatColor.GRAY + "There is already a king!");
                return true;
            }

            String prefix = "&l[&e&lKing&r&l] &r";

            if(args.length == 2 && args[1].contains("queen")) prefix = "&l[&e&lQueen&r&l] &r";

            Bukkit.dispatchCommand(sender, "tab player " + player.getName() + " tabprefix " + prefix);
            sender.sendMessage("King has been set!");
            FtcCore.setKing(player.getUniqueId());
        }
        return true;
    }
}

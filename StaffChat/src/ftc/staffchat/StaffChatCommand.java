package ftc.staffchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length < 1){sender.sendMessage("Usage: /sc <Message>"); return false;}

        String message = "";

        for(int i = 0; i < args.length ; i++){ //adds args to the message
            message += " ";
            message += args[i];
        }

        message = message.replaceAll(":shrug:", "¯\\\\_(ツ)_/¯"); //does the emoticon things
        message = message.replaceAll(":ughcry:", "(ಥ﹏ಥ)");
        message = message.replaceAll(":gimme:", "༼ つ ◕_◕ ༽つ");
        message = message.replaceAll(":gimmecry:", "༼ つ ಥ_ಥ ༽つ");
        message = message.replaceAll(":bear:", "ʕ• ᴥ •ʔ");
        message = message.replaceAll(":smooch:", "( ^ 3^) ♥");
        message = message.replaceAll(":why:", "ლ(ಠ益ಠლ)");
        message = message.replaceAll(":tableflip:", "(ノಠ益ಠ)ノ彡┻━┻");
        message = message.replaceAll(":tableput:", " ┬──┬ ノ( ゜-゜ノ)");
        message = message.replaceAll(":pretty:", "(◕‿◕ ✿)");
        message = message.replaceAll(":sparkle:", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧");
        message = message.replaceAll(":blush:", "(▰˘◡˘▰)");
        message = message.replaceAll(":sad:", "(._. )");

        for(Player p : Bukkit.getServer().getOnlinePlayers()){ //loops through all players and sends the one with staffchat perms the message
            if(p.hasPermission("ftc.staffchat")){
                p.sendMessage(ChatColor.GRAY + "[Staff] " + sender.getName() + ChatColor.GRAY + ChatColor.BOLD + " >" + ChatColor.RESET + message);
            }
        }
        return false;
    }
}

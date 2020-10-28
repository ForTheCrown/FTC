package ftc.staffchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class StaffSpeakEvent implements Listener {

    @EventHandler //How many fucking times have I forgotten this piece of shite
    public void onStaffTalkEvent(AsyncPlayerChatEvent event){
        Player plr = event.getPlayer();
        if (!(Main.plugin.getConfig().getStringList("PlayersWithSCT").contains(plr.getUniqueId().toString()))) return;
        
        event.setCancelled(true);
        
        String message = event.getMessage();
		message = message.replaceAll(":shrug:", "¯\\\\_(ツ)_/¯");
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
		
		
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            if(p.hasPermission("ftc.staffchat")){
                p.sendMessage(ChatColor.GRAY + "[Staff] " + plr.getName() + ChatColor.GRAY + ChatColor.BOLD + " > " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message));
                System.out.println(plr.getName() + " issued server command: /sc " + ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }
}

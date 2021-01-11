package net.forthecrown.core.chat.commands.emotes;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.chat.Chat;
import net.forthecrown.core.files.FtcUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Mwah implements CommandExecutor {

    private List<Player> onCooldown = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sender must be a player:
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command.");
            return false;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();

        if (onCooldown.contains(player)) {
            sender.sendMessage(ChatColor.GRAY + "You kiss too often :D");
            return false;
        }

        // Command no args or target = sender:
        if (args.length < 1 || args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.YELLOW + "Love yourself!" + ChatColor.RESET + " ( ^ 3^) ❤");
            player.playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
            player.spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
            return true;
        }

        Player target;
        try {
            target = Bukkit.getPlayer(args[0]);
        } catch (Exception e){
            player.sendMessage(args[0] + " is not a currently online player.");
            return false;
        }
        FtcUser playerData = FtcCore.getUserData(player.getUniqueId());
        FtcUser targetData = FtcCore.getUserData(target.getUniqueId());

        // Sender should have emotes enabled:
        if(!playerData.getAllowsEmotes()){
            Chat.senderEmoteOffMessage(player);;
            return false;
        }
        if(!targetData.getAllowsEmotes()){
            player.sendMessage(ChatColor.GRAY + "This player has disabled emotes.");
            return false;
        }

        // Actual smooching:
        player.sendMessage(ChatColor.RED + "❤" + ChatColor.RESET + " You smooched " + target.getName() + ChatColor.RED + " ❤");

        TextComponent mwahBack = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&c♥ &e" + player.getName() + " &rsmooched you! &r( ^ 3^) &c❤"));
        mwahBack.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mwah " + player.getName()));
        mwahBack.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to smooch them back")));
        target.spigot().sendMessage(mwahBack);

        Location targetLoc = target.getLocation();
        target.getWorld().playSound(targetLoc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
        target.getWorld().spawnParticle(Particle.HEART, targetLoc.getX(), targetLoc.getY()+1, targetLoc.getZ(), 5, 0.5, 0.5, 0.5);

        loc.getWorld().spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);

        // Put sender on cooldown:
        Chat.addToEmoteCooldown(player, 5*20);
        return true;
    }
}

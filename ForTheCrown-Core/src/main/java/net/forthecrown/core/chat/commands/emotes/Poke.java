package net.forthecrown.core.chat.commands.emotes;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.chat.Chat;
import net.forthecrown.core.files.FtcUserData;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Poke implements CommandExecutor {

    List<Player> onCooldown = new ArrayList<>();
    List<String> pokeOwies = Arrays.asList("stomach", "back", "arm", "butt", "cheek", "neck");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sender must be a player:
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command.");
            return false;
        }
        Player player = (Player) sender;
        FtcUserData playerData = FtcCore.getUserData(player.getUniqueId());

        // Sender can't be on cooldown:
        if (onCooldown.contains(player)) {
            sender.sendMessage(ChatColor.GRAY + "You poke people too often.");
            return false;
        }

        // Command no args or target = sender:
        if (args.length < 1 || args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage("You poked yourself! Weirdo"); //Damn, some people really be weird, pokin themselves, couldn't be me ( ._.)
            player.getWorld().playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);
            return true;
        }

        // Sender should have emotes enabled:
        if(!playerData.getAllowsEmotes()){
            Chat.senderEmoteOffMessage(player);
            return false;
        }

        Player target;
        try {
            target = Bukkit.getPlayer(args[0]);
        } catch (Exception e){
            player.sendMessage(args[0] + " is not a currently online player.");
            return false;
        }
        FtcUserData targetData = FtcCore.getUserData(target.getUniqueId());

        if(!targetData.getAllowsEmotes()){
            player.sendMessage(ChatColor.GRAY + "This player has disabled emotes.");
            return false;
        }

        // Actual poking:
        int pokeOwieInt = (int)(Math.random()*pokeOwies.size()); //The random int that determines what body part they'll poke lol
        player.sendMessage("You poked " + ChatColor.YELLOW + target.getName() + "'s " + ChatColor.RESET + pokeOwies.get(pokeOwieInt));

        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RESET + " poked your " + pokeOwies.get(pokeOwieInt));
        target.getWorld().playSound(target.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);
        target.setVelocity(target.getVelocity().add(target.getLocation().getDirection().normalize().multiply(-0.3).setY(.1)));

        // Put sender on cooldown:
        Chat.addToEmoteCooldown(player, 5*20);
        return true;
    }
}

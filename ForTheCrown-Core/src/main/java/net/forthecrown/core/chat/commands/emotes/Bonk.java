package net.forthecrown.core.chat.commands.emotes;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.chat.Chat;
import net.forthecrown.core.files.FtcUserData;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Bonk implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Command that allows players to vibe on Jingle Bells.
     * Only works if they both have emotes enabled.
     *
     * Valid usages of command:
     * - /jingle
     *
     * Referenced other classes:
     * - FtcCore
     * - Chat
     *
     * Main Author: Wout
     * Edit: Botul
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sender must be player:
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command");
            return false;
        }
        Player player = (Player) sender;
        FtcUserData playerData = FtcCore.getUserData(player.getUniqueId());

        // Sender can't be on cooldown:
        if (Chat.isOnCooldown(player)) {
            sender.sendMessage(ChatColor.GRAY + "You bonk people too often lol");
            return false;
        }
        // Command no args:
        if (args.length < 1 || args[0].equalsIgnoreCase(sender.getName())) {
            sender.sendMessage("Don't hurt yourself ❤");
            return true;
        }

        // Both sender and target should have emotes enabled:
        Player target;
        try {
            target = Bukkit.getPlayer(args[0]);
        } catch (Exception e){
            player.sendMessage(args[0] + " is not a currently online player.");
            return false;
        }
        FtcUserData targetData = FtcCore.getUserData(target.getUniqueId());

        if (!playerData.getAllowsEmotes()) {
            Chat.senderEmoteOffMessage(player);
            return false;
        }
        if (!targetData.getAllowsEmotes()) {
            player.sendMessage(ChatColor.GRAY + "This player has disabled emotes.");
            return false;
        }

        // Actual bonking:
        Location loc = target.getLocation();
        loc.setPitch(loc.getPitch() + 20F);

        player.sendMessage("You bonked " + ChatColor.YELLOW + target.getName() + ChatColor.RESET + "!");
        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RESET + " bonked you!");

        target.teleport(loc);
        target.getWorld().playSound(loc, Sound.ENTITY_SHULKER_HURT_CLOSED, 2.0F, 0.8F);
        target.getWorld().spawnParticle(Particle.CRIT, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);

        // Put sender on cooldown:
        Chat.addToEmoteCooldown(player, 5*20);
        return true;
    }
}

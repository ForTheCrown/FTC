package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.core.files.FtcUser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Bonk implements CrownCommandExecutor {

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
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        // Sender must be player:
        if (!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        Player player = (Player) sender;
        FtcUser playerData = FtcCore.getUser(player.getUniqueId());

        // Sender can't be on cooldown:
        if (FtcCore.isOnCooldown(player)) {
            sender.sendMessage(ChatColor.GRAY + "You bonk people too often lol");
            return false;
        }
        // Command no args:
        if (args.length < 1 || args[0].equalsIgnoreCase(sender.getName())) {
            sender.sendMessage("Don't hurt yourself ❤");
            return true;
        }

        // Both sender and target should have emotes enabled:
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) throw new InvalidPlayerInArgument(sender, args[0]);
        FtcUser targetData = FtcCore.getUser(target.getUniqueId());

        if (!playerData.allowsEmotes()) {
            FtcCore.senderEmoteOffMessage(player);
            return false;
        }
        if (!targetData.allowsEmotes()) {
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
        FtcCore.addToCooldown(player, 5*20, true);
        return true;
    }
}

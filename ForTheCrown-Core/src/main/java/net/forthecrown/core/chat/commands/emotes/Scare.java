package net.forthecrown.core.chat.commands.emotes;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.chat.Chat;
import net.forthecrown.core.files.FtcUser;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Scare implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sender must be a player:
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command.");
            return false;
        }
        Player player = (Player) sender;
        FtcUser playerData = FtcCore.getUserData(player.getUniqueId());

        if(Chat.isOnCooldown(player)){
            sender.sendMessage(ChatColor.GRAY + "You scare people too often lol");
            sender.sendMessage(ChatColor.DARK_GRAY + "This only works every 30 seconds.");
            return false;
        }

        // Command no args:
        if (args.length < 1 || args[0].equalsIgnoreCase(sender.getName())) {
            scare(player);
            return true;
        }

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
        FtcUser targetData = FtcCore.getUserData(target.getUniqueId());

        if(!targetData.getAllowsEmotes()){
            player.sendMessage(ChatColor.GRAY + "This player has disabled emotes.");
            return false;
        }

        // Actual scaring:
        player.sendMessage("You scared " + ChatColor.YELLOW + target.getName() + ChatColor.RESET + "!");
        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RESET + " scared you!");

        scare(target);
        Chat.addToEmoteCooldown(player, 30*20);
        return true;
    }


    private void scare(Player player) {
        Location loc = player.getLocation();
        player.spawnParticle(Particle.MOB_APPEARANCE, loc.getX(), loc.getY(), loc.getZ(), 1);
        Bukkit.getScheduler().scheduleSyncDelayedTask(FtcCore.getInstance(), new Runnable() {
            @Override
            public void run() {
                player.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 2.0F, 1F);

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 9, false, false, false));

                for (int i = 0; i < 3; i++) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(FtcCore.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            player.playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, SoundCategory.MASTER, 1.5F, 1F);
                        }
                    }, i* 3L);
                }
            }
        }, 3L);
    }

}

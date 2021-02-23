package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.EmoteDisabledException;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;

public class Scare extends CrownCommand {

    public Scare(){
        super("scare", FtcCore.getInstance());

        setPermission("ftc.emotes.scare");
        setDescription("description: Scares another player.");
        register();
    }

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        // Sender must be a player:
        if (!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        Player player = (Player) sender;
        CrownUser playerData = FtcCore.getUser(player.getUniqueId());

        if(Cooldown.contains(player, "Core_Emote_Scare")){
            sender.sendMessage(ChatColor.GRAY + "You scare people too often lol");
            sender.sendMessage(ChatColor.DARK_GRAY + "This only works every 30 seconds.");
            return false;
        }

        // Command no args:
        if (args.length < 1 || args[0].equalsIgnoreCase(sender.getName())) {
            scare(player);
            return true;
        }

        if(!playerData.allowsEmotes()) throw new EmoteDisabledException(sender).senderDisabled();

        Player target = Bukkit.getPlayer(args[0]);
        if(target == null){
            player.sendMessage(args[0] + " is not a currently online player.");
            return false;
        }
        CrownUser targetData = FtcCore.getUser(target.getUniqueId());

        if(!targetData.allowsEmotes()) throw new EmoteDisabledException(sender).targetDisabled();

        // Actual scaring:
        player.sendMessage("You scared " + ChatColor.YELLOW + target.getName() + ChatColor.RESET + "!");
        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RESET + " scared you!");

        scare(target);
        Cooldown.add(player, "Core_Emote_Scare", 30*20);
        return true;
    }


    private void scare(Player player) {
        Location loc = player.getLocation();
        player.spawnParticle(Particle.MOB_APPEARANCE, loc.getX(), loc.getY(), loc.getZ(), 1);
        Bukkit.getScheduler().scheduleSyncDelayedTask(FtcCore.getInstance(), () -> {
            player.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 2.0F, 1F);

            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 9, false, false, false));

            for (int i = 0; i < 3; i++) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(FtcCore.getInstance(), () -> player.playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, SoundCategory.MASTER, 1.5F, 1F), i* 3L);
            }
        }, 3L);
    }

}

package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EmoteScare extends CommandEmote {

    public EmoteScare(){
        super("scare", 30*20, ChatColor.GRAY + "You scare people too often lol" + "\n" + ChatColor.DARK_GRAY + "This only works every 30 seconds.");

        setDescription("description: Scares another player.");
        register();
    }

    @Override
    protected int execute(CrownUser sender, CrownUser recipient) {
        sender.sendMessage("You scared " + ChatColor.YELLOW + recipient.getName() + ChatColor.RESET + "!");
        recipient.sendMessage(ChatColor.YELLOW + sender.getName() + ChatColor.RESET + " scared you!");

        scare(recipient.getPlayer());
        return 0;
    }

    @Override
    protected int executeSelf(CrownUser user) {
        scare(user.getPlayer());
        return 0;
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

package net.forthecrown.commands.emotes;

import net.forthecrown.core.Permissions;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static net.forthecrown.core.Messages.*;

public class EmoteScare extends CommandEmote {

    public EmoteScare(){
        super("scare", 30*20, EMOTE_SCARE_COOLDOWN);

        setDescription("Scares another player.");
        setPermission(Permissions.EMOTE_SCARE);

        register();
    }

    @Override
    public int execute(User sender, User target) {
        sender.sendMessage(scareSender(target));
        target.sendMessage(scareTarget(sender, target.hasPermission(getPermission())));

        scare(target.getPlayer());
        return 0;
    }

    @Override
    public int executeSelf(User user) {
        scare(user.getPlayer());
        return 0;
    }

    private void scare(Player player) {
        Location loc = player.getLocation();
        player.spawnParticle(Particle.MOB_APPEARANCE, loc.getX(), loc.getY(), loc.getZ(), 1);

        Tasks.runLater(() -> {
            player.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 2.0F, 1F);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 9, false, false, false));

            for (int i = 0; i < 3; i++) {
                Tasks.runLater(() -> {
                    player.playSound(loc,
                            Sound.ENTITY_ENDERMAN_SCREAM,
                            SoundCategory.MASTER,
                            1.5F, 1F
                    );
                }, i* 3L);
            }
        }, 3L);
    }
}
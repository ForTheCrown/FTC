package net.forthecrown.commands.emotes;

import net.forthecrown.user.User;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

import static net.forthecrown.text.Messages.*;

public class EmoteBonk extends CommandEmote {

    public EmoteBonk(){
        super("bonk", 3*20, EMOTE_BONK_COOLDOWN);

        setDescription("Bonks a player");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Bonks a player
     *
     * Valid usages of command:
     * - /bonk <player>
     *
     * Main Author: Julie
     */

    @Override
    public int execute(User sender, User target) {
        Location loc = target.getLocation();
        loc.setPitch(loc.getPitch() + 20F);

        sender.sendMessage(bonkSender(target));
        target.sendMessage(bonkTarget(sender));

        if (target.getGameMode() != GameMode.SPECTATOR) {
            target.getPlayer().teleport(loc);
            target.getWorld().playSound(loc, Sound.ENTITY_SHULKER_HURT_CLOSED, 2.0F, 0.8F);

            Particle.CRIT.builder()
                    .location(loc.add(0, 1, 0))
                    .count(5)
                    .offset(0.5, 0.5, 0.5)
                    .spawn();
        }
        return 0;
    }

    @Override
    public int executeSelf(User user) {
        user.sendMessage(EMOTE_BONK_SELF);
        return 0;
    }
}
package net.forthecrown.commands.emotes;

import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;

import static net.forthecrown.core.Messages.*;

public class EmotePoke extends CommandEmote {

    public EmotePoke(){
        super("poke", 5*20, EMOTE_POKE_COOLDOWN);

        setDescription("Pokes a player");
        register();
    }

    private static Component randomPart() {
        var length = EMOTE_POKE_PARTS.length;
        return EMOTE_POKE_PARTS[Util.RANDOM.nextInt(length)];
    }

    @Override
    public int execute(User sender, User target) {
        Component part = randomPart();

        sender.sendMessage(pokeSender(target, part));
        target.sendMessage(pokeTarget(sender, part));

        if (target.getPlayer().getGameMode() != GameMode.SPECTATOR) {
            Location targetLoc = target.getLocation();
            var targetPlayer = target.getPlayer();

            targetLoc.getWorld().playSound(target.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);

            targetPlayer.setVelocity(
                    targetPlayer.getVelocity()
                            .add(target.getLocation()
                                    .getDirection()
                                    .normalize()
                                    .multiply(-0.3)
                                    .setY(.1)
                            )
            );
        }

        return 0;
    }

    @Override
    public int executeSelf(User user) {
        user.sendMessage(EMOTE_POKE_SELF);

        user.getWorld().playSound(user.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);
        return 0;
    }
}
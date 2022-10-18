package net.forthecrown.commands.emotes;

import net.forthecrown.core.holidays.MonthDayPeriod;
import net.forthecrown.user.User;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

import java.time.Month;
import java.time.MonthDay;
import java.time.ZonedDateTime;

import static net.forthecrown.text.Messages.*;

public class EmoteSmooch extends CommandEmote {
    private static final MonthDayPeriod VALENTINES = MonthDayPeriod.between(
            MonthDay.of(Month.FEBRUARY, 9),
            MonthDay.of(Month.FEBRUARY, 19)
    );

    public EmoteSmooch(){
        super("mwah", 3 * 20, EMOTE_SMOOCH_COOLDOWN);

        setAliases("smooch", "kiss");
        setDescription("Kisses another player.");

        register();
    }

    @Override
    public int execute(User sender, User target) {
        Location loc = sender.getLocation();

        sender.sendMessage(smoochSender(target));
        target.sendMessage(smoochTarget(sender));

        if (target.getPlayer().getGameMode() != GameMode.SPECTATOR) {
            Location targetLoc = target.getLocation();

            Particle.HEART.builder()
                    .location(targetLoc.add(0, 1, 0))
                    .count(5)
                    .offset(0.5, 0.5, 0.5)

                    // Spawn, relocate and spawn again
                    .spawn()
                    .location(loc.add(0, 1, 0))
                    .spawn();

            targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
            loc.getWorld().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
        }
        return 0;
    }

    @Override
    public int executeSelf(User user) {
        Location loc = user.getLocation();

        user.sendMessage(EMOTE_SMOOCH_SELF);
        user.getPlayer().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);

        Particle.HEART.builder()
                .location(loc.add(0, 1, 0))
                .count(5)
                .offset(0.5, 0.5, 0.5)
                .spawn();

        return 0;
    }

    @Override
    public int getCooldownTime() {
        return VALENTINES.contains(ZonedDateTime.now()) ? 0 : cooldownTime;
    }
}
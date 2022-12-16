package net.forthecrown.commands.emotes;

import net.forthecrown.core.Permissions;
import net.forthecrown.user.User;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.Tasks;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

import static net.forthecrown.core.Messages.*;

public class EmoteHug extends CommandEmote {
    private static final int HUG_TICKS = 100;
    private static final String COOLDOWN = "emote_hug_received";

    public EmoteHug() {
        super("hug", 20*3, EMOTE_HUG_COOLDOWN);

        setDescription("Hugs a player");
        setPermission(Permissions.EMOTE_HUG);

        register();
    }

    @Override
    public int execute(User sender, User target) {
        if (Cooldown.contains(target, COOLDOWN)) {
            sender.sendMessage(hugReceived(target));
            return -1;
        }

        sender.sendMessage(hugSender(target));
        target.sendMessage(hugTarget(
                sender,
                target.hasPermission(getPermission())
        ));

        if (target.getGameMode() != GameMode.SPECTATOR) {
            Cooldown.add(target, COOLDOWN);
            Tasks.runTimer(new HugTask(target), 0, 2);
        }

        return 0;
    }

    @Override
    public int executeSelf(User user) {
        user.sendMessage(EMOTE_HUG_SELF);

        spawnParticles(user.getLocation());
        return -1;
    }

    static void spawnParticles(Location l) {
        Particle.HEART.builder()
                .location(l.add(0, 0.5, 0))
                .count(3)
                .offset(0.25, 0.25, 0.25)
                .spawn();
    }

    public static class HugTask implements Consumer<BukkitTask> {
        private int i = 0;
        private final User user;

        public HugTask(User user) {
            this.user = user;
        }

        @Override
        public void accept(BukkitTask task) {
            if (i == HUG_TICKS || !user.isOnline()) {
                Tasks.cancel(task);
                Cooldown.remove(user, COOLDOWN);

                return;
            }

            spawnParticles(user.getLocation());
            i++;
        }
    }
}
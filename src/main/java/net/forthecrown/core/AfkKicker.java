package net.forthecrown.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class that automatically kicks people for AFKing
 */
public class AfkKicker {
    private static final Map<UUID, KickEntry> ENTRIES = new HashMap<>();

    public static void addOrDelay(UUID uuid) {
        KickEntry entry = ENTRIES.computeIfAbsent(uuid, KickEntry::new);

        entry.setStage(Stage.NONE);
        entry.schedule(GeneralConfig.autoAfkDelay);
    }

    public static void remove(UUID uuid) {
        var entry = ENTRIES.remove(uuid);

        if (entry == null) {
            return;
        }

        entry.cancel();
    }

    public static void onAfk(UUID uuid) {
        KickEntry entry = ENTRIES.computeIfAbsent(uuid, KickEntry::new);
        entry.setStage(Stage.AWAITING_KICK);
        entry.schedule(GeneralConfig.afkKickDelay);
    }

    @Getter
    @RequiredArgsConstructor
    public static class KickEntry implements Runnable {
        private final UUID uniqueId;

        private BukkitTask task;

        @Setter
        private Stage stage;

        public void schedule(long millis) {
            cancel();
            task = Tasks.runLaterAsync(this, Time.millisToTicks(millis));
        }

        @Override
        public void run() {
            if (stage == Stage.AWAITING_KICK) {
                runKick();
            } else {
                runAutoAfk();
            }
        }

        public void runAutoAfk() {
            var player = Users.getLoadedUser(getUniqueId());

            if (player == null || !player.isOnline()) {
                return;
            }


            if (!player.isAfk()) {
                player.afk(Messages.autoAfkReason());
            }

            stage = Stage.AWAITING_KICK;
            schedule(GeneralConfig.afkKickDelay);
        }

        public void runKick() {
            var player = Users.getLoadedUser(getUniqueId());

            if (player == null || !player.isOnline()) {
                return;
            }

            Tasks.runSync(() -> {
                player.getPlayer().kick(Messages.AFK_KICK, PlayerKickEvent.Cause.IDLING);
            });
        }

        public void cancel() {
            task = Tasks.cancel(task);
        }
    }

    public enum Stage {
        NONE,
        AWAITING_KICK
    }
}
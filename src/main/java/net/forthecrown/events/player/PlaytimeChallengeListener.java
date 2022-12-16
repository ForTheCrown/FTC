package net.forthecrown.events.player;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.challenge.Challenges;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PlaytimeChallengeListener implements Listener {
    public static final long TIME = 60 * 20;

    private static final Map<UUID, TaskRunnable>
            TASKS = new Object2ObjectOpenHashMap<>();

    private static boolean active = false;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!active) {
            return;
        }

        TaskRunnable runnable = new TaskRunnable(event.getPlayer());
        TaskRunnable old = TASKS.put(event.getPlayer().getUniqueId(), runnable);

        if (old != null) {
            old.cancelled = true;
        }

        Tasks.runTimer(runnable, TIME, TIME);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var runnable = TASKS.remove(event.getPlayer().getUniqueId());

        if (runnable != null) {
            runnable.cancelled = true;
        }
    }

    public static void reset() {
        if (!active) {
            return;
        }

        TASKS.forEach((uuid, runnable) -> {
            runnable.minutesPassed = 0;
        });
    }

    public static void setActive(boolean active) {
        PlaytimeChallengeListener.active = active;

        if (active) {
            for (var p: Bukkit.getOnlinePlayers()) {
                track(p);
            }
        } else {
            TASKS.forEach((uuid, runnable) -> runnable.cancelled = true);
            TASKS.clear();
        }
    }

    private static void track(Player player) {
        TaskRunnable runnable = new TaskRunnable(player);
        TaskRunnable old = TASKS.put(player.getUniqueId(), runnable);

        if (old != null) {
            old.cancelled = true;
        }

        Tasks.runTimer(runnable, TIME, TIME);
    }

    @RequiredArgsConstructor
    private static class TaskRunnable implements Consumer<BukkitTask> {
        private final Player player;
        private int minutesPassed = 0;
        private boolean cancelled;

        @Override
        public void accept(BukkitTask task) {
            if (cancelled) {
                Tasks.cancel(task);
                return;
            }

            if (Users.get(player).isAfk()) {
                return;
            }

            Challenges.apply("daily/playtime", challenge -> {
                ++minutesPassed;
                challenge.trigger(player);

                if (minutesPassed >= challenge.getGoal(Users.get(player))) {
                    TASKS.remove(player.getUniqueId());
                    Tasks.cancel(task);
                }
            });
        }
    }
}
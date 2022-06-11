package net.forthecrown.core;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class that automatically kicks people for AFKing
 */
public class AfkKicker {
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(0, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private static final Map<UUID, ScheduledFuture> KICK_TASKS = new Object2ObjectOpenHashMap<>();

    public static void addOrDelay(UUID uuid) {
        ScheduledFuture future = EXECUTOR.schedule(() -> kick(uuid), FtcVars.afkKickDelay.get(), TimeUnit.MILLISECONDS);

        ScheduledFuture replacing = KICK_TASKS.put(uuid, future);
        cancel(replacing);
    }

    public static void kick(UUID uuid) {
        remove(uuid);

        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;

        Bukkit.getScheduler().runTask(Crown.inst(), () -> {
            player.kick(
                    Component.translatable("multiplayer.disconnect.idling"),
                    PlayerKickEvent.Cause.IDLING
            );
        });
    }

    public static void remove(UUID uuid) {
        ScheduledFuture future = get(uuid);
        if(future == null) return;

        cancel(future);
        KICK_TASKS.remove(uuid);
    }

    static ScheduledFuture get(UUID uuid) {
        return KICK_TASKS.get(uuid);
    }

    private static void cancel(ScheduledFuture future) {
        if(future == null) return;
        if(future.isCancelled() || future.isDone()) return;

        future.cancel(false);
    }
}
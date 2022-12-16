package net.forthecrown.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Announcer extends SerializableObject.Json {
    private static final Announcer INSTANCE = new Announcer();

    @Getter
    private final List<Component> announcements = new ArrayList<>();

    private BukkitTask broadcaster;

    private Announcer() {
        super(PathUtil.pluginPath("announcer.json"));
    }

    public static Announcer get() {
        return INSTANCE;
    }

    protected void save(final JsonWrapper json) {
        JsonArray array = new JsonArray();

        for (Component c: announcements) {
            array.add(JsonUtils.writeText(c));
        }

        json.add("announcements", array);
    }

    protected void load(final JsonWrapper json) {
        announcements.clear();

        JsonArray array = json.getArray("announcements");
        for (JsonElement j: array) {
            announcements.add(JsonUtils.readText(j));
        }
    }

    /**
     * Stops the AutoAnnouncer
     */
    public void stop() {
        broadcaster = Tasks.cancel(broadcaster);
    }

    /**
     * Starts the AutoAnnouncer
     */
    @OnEnable
    public void start() {
        stop();
        broadcaster = Tasks.runTimer(new BroadcastRunnable(), 20, GeneralConfig.broadcastDelay);
    }

    public void announce(ComponentLike announcement,
                         @Nullable Predicate<Player> predicate
    ) {
        for (Player p: Bukkit.getOnlinePlayers()) {
            if (predicate != null && !predicate.test(p)) {
                continue;
            }

            p.sendMessage(announcement);
        }
    }

    private Component formatMessage(ComponentLike message) {
        return Component.text()
                .append(Messages.FTC_PREFIX)
                .append(message)
                .build();
    }

    public void announce(ComponentLike message) {
        announce(formatMessage(message), player -> true);
    }

    private class BroadcastRunnable implements Runnable {
        private int counter = 0;

        @Override
        public void run() {
            if (getAnnouncements().isEmpty()) {
                return;
            }

            Component broadcast = Component.text()
                    .append(Messages.FTC_PREFIX)
                    .append(getAnnouncements().get(counter++))
                    .build();

            Users.getOnline()
                    .stream()

                    .filter(user -> !user.get(Properties.IGNORING_ANNOUNCEMENTS))

                    .forEach(user -> {
                        user.sendMessage(broadcast);

                        if (user.getWorld().equals(Worlds.resource())) {
                            user.sendMessage(Component.text(
                                    "You're in the resource world! To get back " +
                                            "to the normal survival world, do " +
                                            "/warp portal.",

                                    NamedTextColor.GRAY
                            ));
                        }
                    });

            counter %= getAnnouncements().size();
        }
    }
}
package net.forthecrown.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import net.forthecrown.utils.Tasks;
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
    private final List<Component> announcements = new ArrayList<>();
    private BukkitTask broadcaster;

    public Announcer(){
        super(PathUtil.pluginPath("announcer.json"));

        reload();
        Crown.logger().info("Announcer loaded");
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
     * Gets the string list of announcements used by the AutoAnnouncer
     * @return The list of announcements
     */
    public List<Component> getAnnouncements() {
        return announcements;
    }

    /**
     * Adds an announcement
     * @param announcement the announcement to add
     */
    public void add(Component announcement) {
        announcements.add(announcement);
    }

    /**
     * Removes an announcement
     * @param acIndex The index of the announcement to remove
     */
    public void remove(int acIndex) {
        announcements.remove(acIndex);
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
    public void start() {
        stop();
        broadcaster = Tasks.runTimer(new BroadcastRunnable(), 20, Vars.broadcastDelay);
    }

    public void announce(ComponentLike announcement, @Nullable Predicate<Player> predicate) {
        for (Player p: Bukkit.getOnlinePlayers()) {
            if (predicate != null && !predicate.test(p)) {
                continue;
            }

            p.sendMessage(announcement);
        }
    }

    private Component formatMessage(ComponentLike message) {
        return Component.text()
                .append(Crown.prefix())
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
            Component broadcast = Component.text()
                    .append(Crown.prefix())
                    .append(getAnnouncements().get(counter++))
                    .build();

            for (User player : Users.getOnline()) {
                // Don't broadcast info messages to people that don't want to
                // see broadcasts
                if (player.getProperties().get(Properties.IGNORING_ANNOUNCEMENTS)) {
                    continue;
                }

                player.sendMessage(broadcast);

                if (player.getWorld().equals(Worlds.resource())) {
                    player.sendMessage(Component.text(
                            "You're in the resource world! To get back to the normal " +
                                    "survival world, do /warp portal.",
                            NamedTextColor.GRAY
                    ));
                }
            }

            counter %= getAnnouncements().size();
        }
    }
}
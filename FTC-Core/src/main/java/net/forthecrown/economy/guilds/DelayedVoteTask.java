package net.forthecrown.economy.guilds;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.guilds.topics.VoteTopic;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * A delayed vote task is a task created by a vote topic after
 * voting has ended. It allows a topic to execute code after
 * voting has ended, because the topic itself cannot serialize
 * any data.
 * <p></p>
 * The time variable here is the UNIX timestamp for when the
 * task is meant to be executed.
 * <p></p>
 * data variable contains any data the topic might wish to
 * store for the task's execution in the form of JSON
 */
public class DelayedVoteTask implements JsonSerializable, Runnable {
    public final long time;
    public final JsonElement data;
    public final Key topic;

    private BukkitTask task;

    public DelayedVoteTask(long time, JsonElement data, VoteTopic topic) {
        this(time, data, topic.key());
    }

    private DelayedVoteTask(long time, JsonElement data, Key topic) {
        this.time = time;
        this.data = data;
        this.topic = topic;
    }

    /**
     * Runs the task
     */
    @Override
    public void run() {
        VoteTopic voteTopic = Registries.VOTE_TOPICS.get(topic);
        voteTopic.runTask(this);
    }

    /**
     * Gets the time at which the task is meant to be executed
     * @return The task's execution time stamp
     */
    public long getTime() {
        return time;
    }

    /**
     * Gets the task's data
     * @return Task data
     */
    public JsonElement getData() {
        return data;
    }

    /**
     * Gets the key of the {@link VoteTopic} that created
     * this task.
     *
     * @return Key of the topic that created this task.
     */
    public Key getTopic() {
        return topic;
    }

    /**
     * Schedules the task to be executed automatically.
     */
    public void schedule() {
        unSchedule();

        long execute = TimeUtil.timeUntil(time);
        if(execute <= 0) {
            run();
            return;
        }

        task = Bukkit.getScheduler().runTaskLater(Crown.inst(), this, TimeUtil.millisToTicks(execute));
    }

    /**
     * Cancels any scheduled execution of this task
     */
    public void unSchedule() {
        if(task == null || task.isCancelled()) return;
        task.cancel();
        task = null;
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("time", time);
        json.add("data", data);
        json.addKey("topic", topic);

        return json.getSource();
    }

    public static DelayedVoteTask of(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        return new DelayedVoteTask(
                json.getLong("time"),
                json.get("data"),
                json.getKey("topic")
        );
    }
}

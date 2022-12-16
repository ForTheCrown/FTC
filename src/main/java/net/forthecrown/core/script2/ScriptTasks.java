package net.forthecrown.core.script2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.Tasks;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class ScriptTasks {
    private final Script script;
    private final List<TaskWrapper> tasks = new ObjectArrayList<>();

    public TaskWrapper run(Consumer<TaskWrapper> consumer) {
        TaskWrapper wrapper = new TaskWrapper(consumer, this);
        wrapper.task = Tasks.runSync(wrapper::run);
        return add(wrapper);
    }

    public TaskWrapper run(Runnable runnable) {
        return run((task) -> runnable.run());
    }

    public TaskWrapper runLater(Consumer<TaskWrapper> consumer, long delayTicks) {
        TaskWrapper wrapper = new TaskWrapper(consumer, this);
        wrapper.task = Tasks.runLater(wrapper::run, delayTicks);
        return add(wrapper);
    }

    public TaskWrapper runLater(Runnable runnable, long delayTicks) {
        return runLater((task) -> runnable.run(), delayTicks);
    }

    public TaskWrapper runTimer(Consumer<TaskWrapper> consumer, long initialDelayMillis, long delayMillis) {
        TaskWrapper wrapper = new TaskWrapper(consumer, this);
        wrapper.task = Tasks.runTimer(wrapper::run, initialDelayMillis, delayMillis);
        return add(wrapper);
    }

    public TaskWrapper runTimer(Runnable runnable, long initialDelayMillis, long delayMillis) {
        return runTimer((task) -> runnable.run(), initialDelayMillis, delayMillis);
    }

    private TaskWrapper add(TaskWrapper wrapper) {
        tasks.add(wrapper);
        return wrapper;
    }

    void close() {
        tasks.forEach(wrapper -> Tasks.cancel(wrapper.task));
        tasks.clear();
    }

    @Getter
    @RequiredArgsConstructor
    public static class TaskWrapper {
        private final Consumer<TaskWrapper> callback;
        private final ScriptTasks tasks;

        private BukkitTask task;

        public void run() {
            callback.accept(this);
        }

        public int getTaskId() {
            return task.getTaskId();
        }

        public void cancel() {
            Tasks.cancel(task);
            tasks.tasks.remove(this);
        }
    }
}
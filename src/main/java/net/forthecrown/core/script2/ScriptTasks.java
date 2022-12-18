package net.forthecrown.core.script2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.Tasks;
import org.bukkit.scheduler.BukkitTask;

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

  public TaskWrapper runLater(long delayTicks, Consumer<TaskWrapper> consumer) {
    TaskWrapper wrapper = new TaskWrapper(consumer, this);
    wrapper.task = Tasks.runLater(wrapper::run, delayTicks);
    return add(wrapper);
  }

  public TaskWrapper runTimer(long initialDelayMillis,
                              long delayMillis,
                              Consumer<TaskWrapper> consumer
  ) {
    TaskWrapper wrapper = new TaskWrapper(consumer, this);
    wrapper.task = Tasks.runTimer(wrapper::run, initialDelayMillis, delayMillis);
    return add(wrapper);
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
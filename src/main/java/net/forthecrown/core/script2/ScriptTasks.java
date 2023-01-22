package net.forthecrown.core.script2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.utils.Tasks;
import org.apache.logging.log4j.Logger;
import org.bukkit.scheduler.BukkitTask;

@Getter
@RequiredArgsConstructor
public class ScriptTasks {
  private static final Logger LOGGER = Loggers.getLogger();

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

  public TaskWrapper runTimer(long initialDelayTicks,
                              long delayTicks,
                              Consumer<TaskWrapper> consumer
  ) {
    TaskWrapper wrapper = new TaskWrapper(consumer, this);
    wrapper.task = Tasks.runTimer(wrapper::run, initialDelayTicks, delayTicks);
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
      try {
        callback.accept(this);
      } catch (Exception exc) {
        LOGGER.error("Couldn't invoke task callback for script {}",
            tasks.getScript().getName(),
            exc
        );
      }
    }

    public int getTaskId() {
      return task == null ? 0 : task.getTaskId();
    }

    public void cancel() {
      Tasks.cancel(task);
      tasks.tasks.remove(this);
    }
  }
}
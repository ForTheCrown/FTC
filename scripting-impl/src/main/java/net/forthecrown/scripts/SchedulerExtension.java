package net.forthecrown.scripts;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.forthecrown.utils.Tasks;
import org.bukkit.scheduler.BukkitTask;

public class SchedulerExtension extends ScriptExtension {

  private final List<ScriptTask> tasks = new ObjectArrayList<>();

  public ScriptTask run(Consumer<ScriptTask> consumer) {
    ScriptTask wrapper = new ScriptTask(consumer);
    wrapper.task = Tasks.runSync(wrapper);
    return add(wrapper);
  }

  public ScriptTask runLater(long delayTicks, Consumer<ScriptTask> consumer) {
    ScriptTask wrapper = new ScriptTask(consumer);
    wrapper.task = Tasks.runLater(wrapper, delayTicks);
    return add(wrapper);
  }

  public ScriptTask runTimer(
      long initialDelayTicks,
      long delayTicks,
      Consumer<ScriptTask> consumer
  ) {
    ScriptTask wrapper = new ScriptTask(consumer);
    wrapper.task = Tasks.runTimer(wrapper, initialDelayTicks, delayTicks);
    return add(wrapper);
  }

  private ScriptTask add(ScriptTask wrapper) {
    tasks.add(wrapper);
    return wrapper;
  }

  @Override
  protected void onScriptClose(Script script) {
    var it = tasks.iterator();
    while (it.hasNext()) {
      var n = it.next();
      it.remove();
      n.cancel();
    }
  }

  public class ScriptTask implements Runnable {

    private final Consumer<ScriptTask> callback;
    private BukkitTask task;

    public ScriptTask(Consumer<ScriptTask> callback) {
      this.callback = callback;
    }

    @Override
    public void run() {
      callback.accept(this);
    }

    public boolean isCancelled() {
      return !Tasks.isScheduled(task);
    }

    public void cancel() {
      Tasks.cancel(task);
      tasks.remove(this);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof ScriptTask task)) {
        return false;
      }

      return task.task.getTaskId() == this.task.getTaskId();
    }

    @Override
    public int hashCode() {
      return task.getTaskId();
    }
  }
}
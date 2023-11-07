package net.forthecrown.dungeons.boss.evoker;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.utils.Tasks;
import org.bukkit.scheduler.BukkitTask;

public class TickSequence {

  protected final List<SequenceNode> nodes;
  protected int nodeIndex = 0;
  protected BukkitTask task;

  @Getter
  protected boolean running;

  public TickSequence(Collection<SequenceNode> nodes) {
    this.nodes = new ObjectArrayList<>(nodes);
  }

  public TickSequence() {
    this(Collections.emptyList());
  }

  public TickSequence addNode(SequenceNode node) {
    this.nodes.add(node);
    return this;
  }

  public TickSequence addNode(Runnable runnable, int delay) {
    return addNode(new SequenceNode(runnable, delay));
  }

  public void start() {
    stop();
    startNext();
    running = true;
  }

  public void stop() {
    task = Tasks.cancel(task);
    running = false;
    nodeIndex = 0;
  }

  private void run() {
    SequenceNode node = nodes.get(nodeIndex++);

    try {
      node.runnable.run();
    } catch (Exception e) {
      Loggers.getLogger().error("Couldn't run sequence node", e);
    }

    if (nodeIndex >= nodes.size()) {
      nodeIndex = 0;
      running = false;
      return;
    }

    startNext();
  }

  private void startNext() {
    task = Tasks.runLater(this::run, nodes.get(nodeIndex).delay);
  }

  public record SequenceNode(Runnable runnable, int delay) {

  }
}
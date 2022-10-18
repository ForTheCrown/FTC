package net.forthecrown.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A class to perform a series of actions
 * in delayed sequences.
 * <p>
 * The delay each node holds is relative
 * to the last one. The first node's delay
 * is the delay before anything is executed.
 * <p>
 * Use {@link #start()} to start the sequence
 * execution and {@link #stop()} to stop it lol
 */
public class TickSequence {
    protected final List<SequenceNode> nodes;
    protected int nodeIndex = 0;
    protected BukkitTask task;
    protected boolean running;

    public TickSequence(SequenceNode... nodes) {
        this(Arrays.asList(nodes));
    }

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

    public boolean isRunning() {
        return running;
    }

    private void run() {
        SequenceNode node = nodes.get(nodeIndex++);

        try {
            node.runnable.run();
        } catch (Exception e) {
            Crown.logger().error("Couldn't run sequence node", e);
        }

        if(nodeIndex >= nodes.size()) {
            nodeIndex = 0;
            running = false;
            return;
        }

        startNext();
    }

    private void startNext() {
        task = Tasks.runLater(this::run, nodes.get(nodeIndex).delay);
    }

    public record SequenceNode(Runnable runnable, int delay) {}
}
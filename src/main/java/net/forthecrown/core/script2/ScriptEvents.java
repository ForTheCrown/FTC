package net.forthecrown.core.script2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import net.forthecrown.core.logging.Loggers;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class ScriptEvents {
  private static final Logger LOGGER = Loggers.getLogger();

  private final Script script;
  private final List<ExecutorWrapper> wrappers = new ObjectArrayList<>();

  /* ---------------------------- REGISTRATION ---------------------------- */

  public <E extends Event> void register(Class<E> eventClass,
                                         Consumer<E> mirror
  ) {
     register(eventClass, mirror, true, EventPriority.NORMAL);
  }

  public <E extends Event> void register(Class<E> eventClass,
                                         Consumer<E> mirror,
                                         EventPriority priority
  ) {
    register(eventClass, mirror, true, priority);
  }

  public <E extends Event> void register(Class<E> eventClass,
                                         Consumer<E> mirror,
                                         boolean ignoreCancelled
  ) {
    register(eventClass, mirror, ignoreCancelled, EventPriority.NORMAL);
  }

  public <E extends Event> void register(Class<E> eventClass,
                                         Consumer<E> mirror,
                                         boolean ignoreCancelled,
                                         EventPriority priority
  ) {
    Objects.requireNonNull(eventClass, "Null event class");
    Objects.requireNonNull(mirror, "Null function");
    Objects.requireNonNull(priority, "Null priority");

    ExecutorWrapper<E> wrapper
        = new ExecutorWrapper<>(eventClass, mirror, ignoreCancelled);

    var manager = Bukkit.getPluginManager();
    manager.registerEvent(
        eventClass,
        wrapper,
        priority,
        wrapper,
        FTC.getPlugin(),
        ignoreCancelled
    );

    wrappers.add(wrapper);
  }

  /* --------------------------- UNREGISTRATION --------------------------- */

  public void unregisterAll() {
    wrappers.forEach(HandlerList::unregisterAll);
    wrappers.clear();
  }

  public void unregisterFrom(Class<? extends Event> eventClass) {
    Objects.requireNonNull(eventClass);

    wrappers.removeIf(wrapper -> {
      if (wrapper.type == eventClass) {
        HandlerList.unregisterAll(wrapper);
        return true;
      }

      return false;
    });
  }

  void close() {
    unregisterAll();
  }

  @Getter
  @RequiredArgsConstructor
  class ExecutorWrapper<E extends Event> implements EventExecutor, Listener {

    private final Class<E> type;
    private final Consumer<E> mirror;
    private final boolean ignoreCancelled;

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event)
        throws EventException
    {
      if (!type.isInstance(event)) {
        return;
      }

      if (event instanceof Cancellable c
          && c.isCancelled()
          && ignoreCancelled
      ) {
        return;
      }

      mirror.accept((E) event);
    }
  }
}
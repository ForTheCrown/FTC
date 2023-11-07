package net.forthecrown.resourceworld.gen;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import net.forthecrown.Loggers;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class SeedThreadFactory implements ThreadFactory {

  private AtomicInteger idGenerator = new AtomicInteger(0);

  @Override
  public Thread newThread(@NotNull Runnable r) {
    Thread t = new Thread(r, "SeedThread-" + idGenerator.getAndIncrement());
    Logger logger = Loggers.getLogger(t.getName());

    t.setUncaughtExceptionHandler((t1, e) -> {
      logger.error("Error finding seed", e);
    });

    return t;
  }
}

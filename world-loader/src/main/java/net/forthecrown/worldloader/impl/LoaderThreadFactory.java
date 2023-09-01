package net.forthecrown.worldloader.impl;

import java.util.concurrent.ThreadFactory;
import net.forthecrown.Loggers;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class LoaderThreadFactory implements ThreadFactory {

  private int count;

  @Override
  public Thread newThread(@NotNull Runnable r) {
    String name = "ChunkLoaderThread-%s".formatted(count++);
    Thread t = new Thread(r, name);
    Logger logger = Loggers.getLogger(name);

    t.setUncaughtExceptionHandler((t1, e) -> {
      logger.error("Error in chunk loader thread!", e);
    });

    t.setDaemon(true);

    return t;
  }
}

package net.forthecrown.worldloader.impl;

import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.text.PeriodFormat;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.worldloader.LoadingArea;
import net.forthecrown.worldloader.WorldLoadCompleteEvent;
import net.forthecrown.worldloader.WorldLoaderService.LoadMode;
import net.forthecrown.worldloader.WorldLoaderService.WorldLoad;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.vector.Vector2i;

@Getter
public class LoadingWorld implements WorldLoad {

  /**
   * The size of a {@link LoadSection} in chunks
   */
  private static final int SECTION_SIZE = 32;

  /**
   * The amount of chunks that have to be loaded before the logger logs a progress update message
   */
  private static final int LOG_INTERVAL = 1000;

  private final LoaderService service;

  private final World world;

  private final ExecutorService executor;
  private final Executor mainThread;
  private final Semaphore semaphore;

  private final CompletableFuture<World> future;
  private final Logger logger;

  private LoadMode mode;
  private State state;
  private boolean silent;
  private LoadingArea area;

  // Progress tracking
  private long loaded;
  private long lastLog;
  private long chunkCount;
  private long startedAt;

  private LoadSection[] sections;

  public LoadingWorld(LoaderService service, World world, LoadMode mode) {
    this.service = service;
    this.world = world;
    this.mode = mode;

    this.executor = service.getExecutor();
    this.semaphore = service.getSemaphore();
    this.mainThread = service.getMainThreadExecutor();

    this.logger = Loggers.getLogger(world.getName());
    this.future = new CompletableFuture<>();

    this.state = State.NOT_STARTED;
  }

  private void ensureNotStarted() {
    Preconditions.checkState(state == State.NOT_STARTED, "Loader already started");
  }

  @Override
  public WorldLoad mode(@NotNull LoadMode mode) throws IllegalStateException {
    Objects.requireNonNull(mode);
    ensureNotStarted();

    this.mode = mode;
    return this;
  }

  @Override
  public WorldLoad areaBounds(int minX, int minZ, int maxX, int maxZ) {
    ensureNotStarted();
    this.area = new LoadingArea(minX, minZ, maxX, maxZ);
    return this;
  }

  @Override
  public WorldLoad silent() {
    this.silent = true;
    return this;
  }

  @Override
  public CompletableFuture<World> start() {
    logger.debug("start() called");

    ensureNotStarted();

    logger.debug("not already started");

    if (!isValidSize()) {
      return future;
    }

    logger.debug("world size correct");

    startedAt = System.currentTimeMillis();
    state = State.RUNNING;
    computeSections();

    logger.debug("sections computed");

    if (mode == LoadMode.ASYNC_SERIES) {
      try {
        executor.execute(this::runSeries);
      } catch (Throwable t) {
        future.completeExceptionally(t);
        stop();
      }
    } else {
      runParallel();
    }

    logger.debug("before return");
    return future;
  }

  private void runSeries() {
    int i = 0;

    while (i < sections.length) {
      LoadSection section = sections[i++];

      if (section == null) {
        continue;
      }

      section.load();
      section.completed = true;

      if (state != State.RUNNING) {
        return;
      }
    }

    complete();
  }

  private void runParallel() {
    // Start all sections
    for (LoadSection s : sections) {
      executor.execute(s);
    }
  }

  @Override
  public void stop() {
    close(false);
    service.remove(this);
  }

  LoadingArea getArea() {
    return LoadingArea.getArea(area, world);
  }

  private boolean isValidSize() {
    var area = getArea();

    int size = Math.max(area.sizeX(), area.sizeZ());
    int max = service.plugin.getLoaderConfig().maxWorldSize;

    if (size <= max) {
      return true;
    }

    future.completeExceptionally(
        new IllegalArgumentException("World size above maximum (max=" + max + " size=" + size + ")")
    );
    return false;
  }

  private void computeSections() {
    LoadingArea area = getArea();

    var chunkArea = area.div(Vectors.CHUNK_SIZE);
    var sectionArea = chunkArea.div(SECTION_SIZE);

    int sectionsX = sectionArea.sizeX();
    int sectionsZ = sectionArea.sizeZ();

    int totalSections = sectionArea.area();

    var backToChunks = sectionArea.mul(SECTION_SIZE);

    this.sections = new LoadSection[totalSections];
    this.chunkCount = backToChunks.area();

    int index = 0;
    Vector2i startChunk = Vector2i.from(chunkArea.minX(), chunkArea.minZ());

    for (int x = 0; x < sectionsX; x++) {
      for (int z = 0; z < sectionsZ; z++) {
        final int xOffset = x * SECTION_SIZE;
        final int zOffset = z * SECTION_SIZE;

        Vector2i sectionStart = startChunk.add(xOffset, zOffset);
        sections[index] = new LoadSection(sectionStart.x(), sectionStart.y());
        index++;
      }
    }

    // Log load data
    logger.debug("load data:");
    logger.debug("- LoadSections used={}", totalSections);
    logger.debug("- chunkSize={}", chunkCount);
    logger.debug("- startingChunk={}", startChunk);
    logger.debug("- sectionArea={}", sectionArea);
    logger.debug("- chunkArea={}", chunkArea);
    logger.debug("- area={}", area);
  }

  public void close(boolean success) {
    if (success) {
      future.complete(world);
    }

    Tasks.runSync(() -> {
      WorldLoadCompleteEvent event = new WorldLoadCompleteEvent(world, success);
      event.callEvent();
    });

    state = State.STOPPED;
  }

  // Method called by sections to invoke a
  // check if all sections are done or not
  // if all have finished, complete the
  // CompletableFuture with null
  synchronized void completeSection() {
    // Check if all sections have been loaded
    // If any have not been loaded, stop
    for (LoadSection s : sections) {
      if (!s.completed) {
        return;
      }
    }

    complete();
  }


  private void complete() {
    logger.debug("complete() called");

    long interval = System.currentTimeMillis() - startedAt;
    long msPerChunk = interval / loaded;

    logger.info("Finished load: took {}, average of {} / chunk, loaded {} chunks",
        DurationFormatUtils.formatDuration(interval, "HH:mm:ss"),
        PeriodFormat.of(msPerChunk),
        loaded
    );

    // Shutdown this loader instance
    close(true);
  }

  private void onChunkLoaded() {
    lastLog++;
    loaded++;

    if (lastLog < service.plugin.getLoaderConfig().logFrequency) {
      return;
    }

    lastLog = 0;

    try {
      tryLogProgress();
    } catch (Throwable t) {
      logger.error("Failed to log progress lmfao", t);
    }
  }

  private static String formatDuration(long durationMillis) {
    if (durationMillis < 1) {
      return String.valueOf(durationMillis);
    }
    return DurationFormatUtils.formatDuration(durationMillis, "HH:mm:ss");
  }

  private void tryLogProgress() {
    float progressPercent = (float) loaded / (float) chunkCount * 100;
    System.gc();

    long elapsedMillis = System.currentTimeMillis() - startedAt;
    long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);
    float cps = (float) loaded / elapsedSeconds;

    long totalSeconds = GenericMath.floorl((float) chunkCount / cps);
    long remainingMillis = TimeUnit.SECONDS.toMillis(totalSeconds) - elapsedMillis;

    String remainingFormatted = formatDuration(remainingMillis);
    String elapsedFormatted = formatDuration(elapsedMillis);

    logger.info("load-progress={}/{} ({}%), cps={}, remaining={}, elapsed={}",
        loaded,
        chunkCount,
        String.format("%.2f", progressPercent),

        String.format("%.2f", cps),

        remainingFormatted,
        elapsedFormatted
    );
  }

  private enum State {
    NOT_STARTED,
    RUNNING,
    STOPPED
  }

  /**
   * A single section of a world that's being loaded by the {@link LoadingWorld}
   */
  private class LoadSection implements Runnable {

    private final int startX;
    private final int startZ;

    private boolean completed;

    public LoadSection(int chunkX, int chunkZ) {
      this.startX = chunkX;
      this.startZ = chunkZ;
    }

    @Override
    public void run() {
      load();
      complete();
    }

    public void load() {
      // Run a nested for loop through all of these sections'
      // chunks and load those
      for (int x = 0; x < SECTION_SIZE; x++) {
        for (int z = 0; z < SECTION_SIZE; z++) {
          // If the loader has been stopped, stop
          if (state != State.RUNNING) {
            return;
          }

          // The position of the chunk we're loading
          int cX = startX + x;
          int cZ = startZ + z;

          // In the case that we're loading a massive world
          // we can't be loading too many chunks at once, that'll
          // cause CPU and RAM issues, so use the semaphore to
          // ensure we're not loading too many chunks at once
          semaphore.acquireUninterruptibly();

          // Load chunk, are these comments obvious enough
          world.getChunkAtAsync(cX, cZ, true, false).whenComplete((chunk, throwable) -> {
            if (throwable != null) {
              logger.error("Error while loading chunk", throwable);
              return;
            }

            if (chunk == null) {
              logger.warn("Couldn't load chunk at [x={}, z={}], unknown cause", cX, cZ);
              return;
            }

            // Unload the chunk to make sure it doesn't stay in RAM
            // We're just generating the chunks beforehand, not
            // trying to overwhelm the server here
            if (!Bukkit.isPrimaryThread()) {
              mainThread.execute(() -> chunk.unload(true));
            } else {
              chunk.unload(true);
            }

            //Update progress tracker and release the semaphore permit
            onChunkLoaded();
            semaphore.release();
          });
        }
      }
    }

    private void complete() {
      completed = true;
      completeSection();
    }
  }
}

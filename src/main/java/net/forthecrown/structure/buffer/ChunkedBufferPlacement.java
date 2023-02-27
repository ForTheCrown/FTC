package net.forthecrown.structure.buffer;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.structure.buffer.ChunkedBlockBuffer.Section;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.Transform;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;

@Getter
class ChunkedBufferPlacement implements Runnable {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final long SECTIONS_PER_TICK = 15;

  private final ChunkedBlockBuffer buffer;
  private final World world;
  private final Transform transform;
  private final boolean updatePhysics;

  private final CompletableFuture<Void> future = new CompletableFuture<>();

  private long sectionsScheduled = 0;
  private long sectionsPlaced = 0;

  public ChunkedBufferPlacement(World world,
                                ChunkedBlockBuffer buffer,
                                Transform transform,
                                boolean updatePhysics
  ) {
    this.buffer = Objects.requireNonNull(buffer);
    this.world = Objects.requireNonNull(world);
    this.transform = Objects.requireNonNull(transform);
    this.updatePhysics = updatePhysics;
  }

  public CompletableFuture<Void> start() {
    if (Bukkit.isPrimaryThread()) {
      runSafe();
    } else {
      Tasks.runSync(this::runSafe);
    }

    return future;
  }

  void runSafe() {
    try {
      run();
    } catch (Throwable t) {
      future.completeExceptionally(t);
    }
  }

  void onSectionFinish() {
    if (sectionsPlaced < sectionsScheduled) {
      return;
    }

    future.complete(null);
  }

  @Override
  public void run() {
    Section[] sections = buffer.getSections();

    for (Section section : sections) {
      if (section == null) {
        continue;
      }

      long tickOffset = sectionsScheduled / SECTIONS_PER_TICK;

      Tasks.runLater(() -> place(section), tickOffset);
      sectionsScheduled++;
    }
  }

  void place(Section section) {
    section.place(world, transform, updatePhysics);

    sectionsPlaced++;
    onSectionFinish();
  }
}
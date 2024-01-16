package net.forthecrown.resourceworld.gen;

import static net.forthecrown.resourceworld.Constants.HEIGHT_ACCESSOR;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.resourceworld.RwConfig;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.craftbukkit.v1_20_R3.CraftHeightMap;
import org.slf4j.Logger;

@Getter
public class SeedFinder {

  static final Logger LOGGER = Loggers.getLogger();

  private final GenConfig params;
  private final RwConfig config;

  private final int worldSize;

  private final ExecutorService service;
  private final CompletableFuture<Long> future;

  private final Random seedgen;
  private final LongSet triedSeeds;

  private final Heightmap.Types heightmap;

  private final Vector<Seed> allSeeds;

  private final AtomicInteger started = new AtomicInteger(0);
  private final AtomicInteger finished = new AtomicInteger(0);

  public SeedFinder(GenConfig params, RwConfig config, int worldSize) {
    Objects.requireNonNull(params, "Null params");
    Objects.requireNonNull(config, "Null config");

    this.config = config;
    this.params = params;
    this.worldSize = worldSize;

    this.service = Executors.newFixedThreadPool(params.getFinderThreads(), new SeedThreadFactory());
    this.allSeeds = new Vector<>(params.getFinderThreads() * params.getFindAttempts());

    this.future = new CompletableFuture<>();
    this.seedgen = new Random();
    this.triedSeeds = LongSets.synchronize(new LongOpenHashSet(), this);

    this.heightmap = CraftHeightMap.toNMS(params.getHeightMap());
  }

  public CompletableFuture<Long> run() {
    for (int i = 0; i < params.getFinderThreads(); i++) {
      SeedValidator[] pipeline = createPipeline();
      Finder finder = new Finder(pipeline);
      service.execute(finder);
      started.incrementAndGet();
    }

    return future;
  }

  private SeedValidator[] createPipeline() {
    List<SeedValidator> validators = new ObjectArrayList<>();

    if (!params.isSpawnCheckDisabled()) {
      validators.add(new SpawnValidator());
    }

    if (!params.isBiomeCheckDisabled()) {
      validators.add(new BiomeValidator());
    }

    if (!params.isStructureCheckDisabled()) {
      validators.add(new StructureValidator());
    }

    SeedValidator[] arr = validators.toArray(SeedValidator[]::new);
    for (SeedValidator seedValidator : arr) {
      seedValidator.bindFinder(this);
    }
    return arr;
  }

  private void onComplete() {
    finished.incrementAndGet();

    if (finished.get() < started.get()) {
      return;
    }

    allSeeds.sort(Comparator.naturalOrder());
    Seed first = allSeeds.firstElement();

    if (first.failed) {
      if (params.isInvalidSeedsAllowed()) {
        future.complete(first.seed);
      } else {
        future.complete(null);
      }
    }

    close();
    future.complete(first.seed);
  }

  public void close() {
    service.shutdownNow();
  }

  public class Finder implements Runnable {

    private int attemptsMade = 0;
    private Seed seed;

    private final List<Seed> previous;

    private final SeedValidator[] pipeline;

    public Finder(SeedValidator[] pipeline) {
      this.previous = new ObjectArrayList<>(params.getFindAttempts());
      this.pipeline = pipeline;
    }

    @Override
    public void run() {
      while (attemptsMade < params.getFindAttempts()) {
        attemptsMade++;
        tryGen();
      }

      allSeeds.addAll(previous);
      onComplete();
    }

    private void tryGen() {
      long seedValue = seedgen.nextLong();

      if (triedSeeds.contains(seedValue)) {
        return;
      }

      synchronized (SeedFinder.this) {
        triedSeeds.add(seedValue);
      }

      seed = new Seed(seedValue);
      evaluateSeed();

      previous.add(seed);
      seed = null;
    }

    private void evaluateSeed() {
      var gen = seed.gen;
      int baseY = gen.getBaseHeight(0, 0, heightmap, HEIGHT_ACCESSOR, seed.randomState);

      for (SeedValidator seedValidator : pipeline) {
        seedValidator.bindSeed(seed, baseY, worldSize);
        seedValidator.evaluate();
        seedValidator.bindSeed(null, 0, 0);

        if (seed.failed) {
          return;
        }
      }
    }


  }

}

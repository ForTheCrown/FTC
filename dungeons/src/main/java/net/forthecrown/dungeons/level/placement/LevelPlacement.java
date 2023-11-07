package net.forthecrown.dungeons.level.placement;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.dungeons.DungeonManager;
import net.forthecrown.dungeons.level.BiomeSource;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.LevelBiome;
import net.forthecrown.structure.BlockRotProcessor.IntegrityProvider;
import net.forthecrown.structure.FunctionInfo;
import net.forthecrown.structure.buffer.BlockBuffer;
import net.forthecrown.structure.buffer.BlockBuffers;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.AbstractBounds3i;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.PerlinNoiseGenerator;
import org.slf4j.Logger;
import org.spongepowered.math.vector.Vector3i;

@Getter
public class LevelPlacement implements IntegrityProvider {

  private static final Logger LOGGER = Loggers.getLogger();

  private final Map<String, List<FunctionInfo>> postProcessMarkers
      = new Object2ObjectOpenHashMap<>();

  private final Map<String, PostPlacementProcessor> processorMap
      = new Object2ObjectOpenHashMap<>();

  private final World world;
  private final Random random;
  private final DungeonLevel level;

  private final BlockBuffer buffer;
  private final DungeonEntityPlacement entityPlacement;

  private final BiomeSource biomeSource;

  private final NoiseGenerator rotGenerator;
  private final double rotNoiseScale = 0.25D;

  private final ExecutorService executorService;

  private final CompletableFuture<Void> future = new CompletableFuture<>();

  public LevelPlacement(World world, Random random, DungeonLevel level) {
    this.world = Objects.requireNonNull(world);
    this.random = Objects.requireNonNull(random);
    this.level = Objects.requireNonNull(level);

    this.biomeSource = new BiomeSource(random, LevelBiome.values());
    this.rotGenerator = new PerlinNoiseGenerator(random);

    var area = level.getChunkMap()
        .values()
        .parallelStream()
        .map(DungeonPiece::getBounds)
        .reduce(AbstractBounds3i::combine)
        .orElseThrow();

    this.buffer = BlockBuffers.allocate(area);
    this.entityPlacement = new DungeonEntityPlacement();

    this.executorService = DungeonManager.getDungeons().getExecutorService();
  }

  public static LevelPlacement create(World world, DungeonLevel level) {
    Random random = new Random();
    return new LevelPlacement(world, random, level);
  }

  public CompletableFuture<Void> run() {
    try {
      RoomPlacingVisitor visitor = new RoomPlacingVisitor(this);
      level.getRoot().visit(visitor);
    } catch (Throwable t) {
      future.completeExceptionally(t);
    }

    return future;
  }

  void onPlacementsFinished() {
    if (!processorMap.isEmpty()) {
      processorMap.forEach((s, processor) -> {
        var markers = getMarkers(s);
        processor.processAll(this, markers, random);
      });
    }

    buffer.place(world).whenComplete((unused, throwable) -> {
      if (throwable != null) {
        future.completeExceptionally(throwable);
        return;
      }

      if (Bukkit.isPrimaryThread()) {
        runPostPlacement();
      } else {
        Tasks.runSync(this::runPostPlacement);
      }
    });
  }

  private void runPostPlacement() {
    entityPlacement.place(world);

    future.complete(null);
  }

  public void addMarker(FunctionInfo info) {
    var list = postProcessMarkers.computeIfAbsent(
        info.getFunctionKey(),
        s -> new ArrayList<>()
    );

    list.add(info);
  }

  public List<FunctionInfo> getMarkers(String key) {
    return postProcessMarkers.getOrDefault(key, ObjectLists.emptyList());
  }

  @Override
  public double getIntegrity(Vector3i pos) {
    pos = pos.mul(rotNoiseScale);

    double noise = rotGenerator.noise(pos.x(), pos.y(), pos.z());
    return (noise + 1.0D) / 2.0D;
  }
}
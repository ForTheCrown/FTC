package net.forthecrown.dungeons.level.placement;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import lombok.Getter;
import net.forthecrown.dungeons.level.BiomeSource;
import net.forthecrown.dungeons.level.LevelBiome;
import net.forthecrown.structure.BlockRotProcessor.IntegrityProvider;
import net.forthecrown.structure.FunctionInfo;
import org.bukkit.World;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.PerlinNoiseGenerator;
import org.spongepowered.math.vector.Vector3i;

@Getter
public class LevelPlacement implements IntegrityProvider {
  private final Map<String, List<FunctionInfo>> postProcessMarkers
      = new Object2ObjectOpenHashMap<>();

  private final Map<String, PostPlacementProcessor> processorMap
      = new Object2ObjectOpenHashMap<>();

  private final World world;
  private final Random random;

  private final BiomeSource biomeSource;

  private final NoiseGenerator rotGenerator;
  private double rotNoiseScale = 0.25D;

  public LevelPlacement(World world, Random random) {
    this.world = Objects.requireNonNull(world);
    this.random = Objects.requireNonNull(random);
    this.biomeSource = new BiomeSource(random, LevelBiome.values());
    this.rotGenerator = new PerlinNoiseGenerator(random);
  }

  public static LevelPlacement create(World world) {
    Random random = new Random();
    return new LevelPlacement(world, random);
  }

  public void runPostProcessors() {
    if (processorMap.isEmpty()) {
      return;
    }

    processorMap.forEach((s, processor) -> {
      var markers = getMarkers(s);
      processor.processAll(this, markers, random);
    });
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
package net.forthecrown.dungeons.level.placement;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import lombok.Getter;
import net.forthecrown.structure.FunctionInfo;
import org.bukkit.World;

@Getter
public class PostProcessorManager {
  private final Map<String, List<FunctionInfo>> postProcessMarkers
      = new Object2ObjectOpenHashMap<>();

  private final Map<String, PostPlacementProcessor> processorMap
      = new Object2ObjectOpenHashMap<>();

  private final World world;
  private final Random random;

  public PostProcessorManager(World world, Random random) {
    this.world = Objects.requireNonNull(world);
    this.random = Objects.requireNonNull(random);
  }

  public static PostProcessorManager create(World world) {
    Random random = new Random();
    return new PostProcessorManager(world, random);
  }

  public void runPostProcessors() {
    if (processorMap.isEmpty()) {
      return;
    }

    processorMap.forEach((s, processor) -> {
      var markers = getMarkers(s);
      processor.processAll(world, markers, random);
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
}
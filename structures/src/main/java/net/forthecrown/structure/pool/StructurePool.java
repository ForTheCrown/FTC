package net.forthecrown.structure.pool;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import lombok.Getter;
import net.forthecrown.registry.Registry;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.utils.WeightedList;

public class StructurePool {

  public static StructurePool EMPTY
      = new StructurePool(Collections.emptyList());

  public static final Codec<StructurePool> CODEC = PoolEntry.CODEC.listOf()
      .xmap(StructurePool::new, StructurePool::getEntries);

  @Getter
  private final ImmutableList<PoolEntry> entries;

  @Getter
  private final int totalWeight;

  public StructurePool(List<PoolEntry> entries) {
    Objects.requireNonNull(entries);

    if (entries.isEmpty()) {
      this.totalWeight = 0;
      this.entries = ImmutableList.of();
    } else {
      this.totalWeight = entries.stream().mapToInt(PoolEntry::weight).sum();
      this.entries = ImmutableList.copyOf(entries);
    }
  }

  public WeightedList<BlockStructure> toWeightedList(Registry<BlockStructure> structures) {
    WeightedList<BlockStructure> result = new WeightedList<>();

    for (PoolEntry entry : entries) {
      structures.get(entry.structureName()).ifPresent(struct -> {
        result.add(entry.weight(), struct);
      });
    }

    return result;
  }

  public Optional<BlockStructure> getRandom(Registry<BlockStructure> structures, Random random) {
    if (isEmpty()) {
      return Optional.empty();
    }

    int weightVal = random.nextInt(0, totalWeight);
    int index = 0;

    while (index < entries.size()) {
      PoolEntry entry = entries.get(index);
      weightVal -= entry.weight();

      if (weightVal <= 0) {
        return structures.get(entry.structureName());
      }

      index++;
    }

    return Optional.empty();
  }

  public boolean isEmpty() {
    return totalWeight < 1 || entries.isEmpty();
  }
}
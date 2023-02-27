package net.forthecrown.structure;

import static net.forthecrown.structure.BlockStructure.DEFAULT_PALETTE_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.structure.buffer.BlockBuffer;
import net.forthecrown.structure.buffer.BlockBuffers;
import net.forthecrown.utils.math.Transform;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

@Getter
public class StructurePlaceConfig {

  private final BlockBuffer buffer;
  private final Vector3i destination;

  private final ImmutableList<BlockProcessor> processors;
  private final ImmutableMap<String, FunctionProcessor> functions;

  private final StructureEntitySpawner entitySpawner;

  private final Transform transform;

  private final String paletteName;

  private StructurePlaceConfig(Builder builder) {
    this.buffer = builder.buffer;
    this.destination = builder.pos;
    this.processors = builder.processors.build();
    this.functions = builder.functions.build();
    this.entitySpawner = builder.entitySpawner;

    this.paletteName = builder.paletteName();

    this.transform = builder.transform
        .addOffset(destination);
  }

  public static Builder builder() {
    return new Builder();
  }

  public @Nullable Pair<BlockInfo, Vector3i> run(
      @NotNull BlockInfo original,
      @NotNull Vector3i offset
  ) {
    var result = original.copy();
    original = original.copy();
    Mutable<Vector3i> position = new MutableObject<>(offset);

    for (var p : processors) {
      result = p.process(
          original,
          result == null ? null : result.copy(),
          this,
          position
      );
    }

    return Pair.of(result, position.getValue());
  }

  @Getter
  @Setter
  @Accessors(chain = true, fluent = true)
  public static class Builder {

    private BlockBuffer buffer;
    private Vector3i pos;

    private final ImmutableList.Builder<BlockProcessor> processors = ImmutableList.builder();
    private final ImmutableMap.Builder<String, FunctionProcessor> functions = ImmutableMap.builder();

    private StructureEntitySpawner entitySpawner;

    private Transform transform = Transform.IDENTITY;

    private String paletteName = DEFAULT_PALETTE_NAME;

    public Builder addProcessor(BlockProcessor processor) {
      processors.add(processor);
      return this;
    }

    public Builder world(World world) {
      this.buffer = BlockBuffers.immediate(world);
      this.entitySpawner = StructureEntitySpawner.world(world);
      return this;
    }

    public Builder addNonNullProcessor() {
      return addProcessor(BlockProcessors.NON_NULL_PROCESSOR);
    }

    public Builder addRotationProcessor() {
      return addProcessor(BlockProcessors.ROTATION_PROCESSOR);
    }

    public Builder addFunction(String functionKey, FunctionProcessor processor) {
      this.functions.put(functionKey, processor);
      return this;
    }

    public StructurePlaceConfig build() {
      return new StructurePlaceConfig(this);
    }
  }
}
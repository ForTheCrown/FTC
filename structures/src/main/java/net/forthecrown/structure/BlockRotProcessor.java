package net.forthecrown.structure;

import java.util.Objects;
import java.util.Random;
import lombok.Getter;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

@Getter
public class BlockRotProcessor implements BlockProcessor {
  private final IntegrityProvider integrityProvider;
  private final Random random;

  public BlockRotProcessor(IntegrityProvider integrityProvider, Random random) {
    Objects.requireNonNull(integrityProvider);
    Objects.requireNonNull(random);

    this.integrityProvider = integrityProvider;
    this.random = random;
  }

  @Override
  public @Nullable BlockInfo process(
      @NotNull BlockInfo original,
      @Nullable BlockInfo previous,
      @NotNull StructurePlaceConfig config,
      Mutable<Vector3i> position
  ) {
    if (previous == null) {
      return null;
    }

    var pos = config.getTransform().apply(position.getValue());
    double rotLevel = integrityProvider.getIntegrity(pos);

    if (random.nextFloat() > rotLevel) {
      return previous;
    }

    var data = previous.getData();
    var rottedData = RotLookup.rot(data, random);

    if (rottedData == null) {
      return previous;
    }

    return previous.withData(rottedData);
  }

  public interface IntegrityProvider {
    double getIntegrity(Vector3i worldPosition);

    static IntegrityProvider fixed(float rot) {
      return worldPosition -> rot;
    }
  }
}
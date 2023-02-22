package net.forthecrown.structure;

import java.util.Objects;
import java.util.Random;
import lombok.Getter;
import net.forthecrown.utils.VanillaAccess;
import org.apache.commons.lang3.mutable.Mutable;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
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
  public @Nullable BlockInfo process(@NotNull BlockInfo original,
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

    var originalType = previous.getData().getMaterial();
    var rotted = lookupRot(originalType);

    if (rotted == null) {
      return previous;
    }

    var data = previous.getData();
    var newData = cloneWithMaterial(data, rotted);

    return previous.withData(newData);
  }

  private Material lookupRot(Material material) {
    var name = material.name();

    if (material == Material.STONE_BRICK_WALL) {
      return Material.ANDESITE_WALL;
    }

    if (name.contains("STONE")) {
      return Material.matchMaterial(name.replaceAll("STONE", "ANDESITE"));
    }

    if (name.contains("ANDESITE")) {
      return Material.matchMaterial(name.replaceAll("ANDESITE", "COBBLESTONE"));
    }

    if (name.contains("STONE_BRICK")) {
      return Material.matchMaterial(
          name.replaceAll("STONE_BRICKS?", "CRACKED_STONE_BRICK")
      );
    }

    return null;
  }

  private BlockData cloneWithMaterial(BlockData data, Material material) {
    BlockData result = material.createBlockData();
    return VanillaAccess.merge(result, data);
  }

  public interface IntegrityProvider {
    double getIntegrity(Vector3i worldPosition);

    static IntegrityProvider fixed(float rot) {
      return worldPosition -> rot;
    }
  }
}
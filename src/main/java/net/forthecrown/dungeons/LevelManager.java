package net.forthecrown.dungeons;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.utils.Util;

public class LevelManager {
  /**
   * Amount of bits a block position must be shifted by to translate to a
   * cell position.
   * <p>
   * With 9 bits, this means a level cell's size is 512x512
   */
  public static final int CELL_BITS = 9;

  /** Size of a single level cell */
  public static final int CELL_SIZE = 1 << CELL_BITS;

  /** Half size of a single cell, used for getting a cell's center */
  public static final int CELL_HALF_SIZE = CELL_SIZE / 2;

  @Getter
  private final DungeonManager manager;

  @Getter
  private final DungeonDataStorage storage;

  private final Map<LevelCell, DungeonLevel> levels
      = new Object2ObjectOpenHashMap<>();

  private final LinkedList<LevelCell> availableCells = new LinkedList<>();
  private int cellRadius = 0;

  public LevelManager(DungeonManager manager) {
    this.manager = manager;
    this.storage = manager.getStorage();

    expandBounds();
  }

  public void addLevel(LevelCell cell, DungeonLevel level) {
    level.setCell(cell);

    var existing = levels.get(cell);

    if (existing != null) {
      throw Util.newException("Cell %s is already in use!", cell);
    }

    levels.put(cell, level);
  }

  public LevelCell popFreeCell() {
    if (availableCells.isEmpty()) {
      expandBounds();
    }

    return availableCells.pop();
  }

  private void expandBounds() {
    final int currentRadius = cellRadius;
    ++cellRadius;

    for (int x = -cellRadius; x <= cellRadius; x++) {
      for (int z = -cellRadius; z <= cellRadius; z++) {

        // If inside currently existing bounds, skip
        if ((x >= -currentRadius && x <= currentRadius)
            && (z >= -currentRadius && z <= currentRadius)
        ) {
          continue;
        }

        availableCells.push(new LevelCell(x, z));
      }
    }
  }

  public void closeLevel(LevelCell cell) {
    DungeonLevel level = levels.remove(cell);

    if (level == null) {
      return;
    }

    availableCells.push(cell);
    level.stopTicking();
    level.setCell(null);
  }

  @Getter
  @RequiredArgsConstructor
  public static class LevelCell {
    public static final LevelCell ZERO = new LevelCell(0, 0);

    private final int x;
    private final int z;

    public static LevelCell of(int blockX, int blockZ) {
      return new LevelCell(
          blockX >> CELL_BITS,
          blockZ >> CELL_BITS
      );
    }

    public LevelCell add(int x, int z) {
      return new LevelCell(this.x + x, this.z + z);
    }

    public int getBlockX() {
      return x << CELL_BITS;
    }

    public int getBlockZ() {
      return z << CELL_BITS;
    }

    public int getCenterX() {
      return getBlockX() + CELL_HALF_SIZE;
    }

    public int getCenterZ() {
      return getBlockZ() + CELL_HALF_SIZE;
    }

    @Override
    public String toString() {
      return "(" + x + ", " + z + ")";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof LevelCell cell)) {
        return false;
      }

      return getX() == cell.getX()
          && getZ() == cell.getZ();
    }

    @Override
    public int hashCode() {
      return Objects.hash(getX(), getZ());
    }
  }
}
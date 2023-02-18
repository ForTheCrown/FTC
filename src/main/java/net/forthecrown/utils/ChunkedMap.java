package net.forthecrown.utils;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.util.AbstractCollection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.LongConsumer;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.math.AbstractBounds3i;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

/**
 * A map created for easy implementation of subdivision-based 3D spatial lookups.
 * <p>
 * I'm sure that each and every word I just typed was used in a wrong context.
 * <p>
 * This map divides all given entries between 3D subdivisions known as chunks to provide faster and
 * easier 3D lookups of objects.
 *
 * @param <T> The type this map holds
 */
@RequiredArgsConstructor
public class ChunkedMap<T extends BoundsHolder> {
  /* ----------------------------- CONSTANTS ------------------------------ */
  /**
   * Constant returned by {@link #findNearest(Vector3d)} when no near object was found
   */
  @SuppressWarnings("rawtypes")
  public static final ObjectDoublePair NO_NEAREST
      = ObjectDoublePair.of(null, -1.0D);

  /* -------------------------- INSTANCE FIELDS --------------------------- */

  /**
   * Chunk pos to chunk entry list map.
   * <p>
   * The <code>long</code> key in this map is a packed chunk position, generated by
   * {@link #toChunkLong(Vector3i)}, and the entries are a quick implementation of the {@link List}
   * class
   */
  protected final Long2ObjectMap<ChunkList<T>> chunkMap
      = new Long2ObjectOpenHashMap<>();

  /**
   * Entries registered within this map.
   * <p>
   * Used mainly for testing if a value has been added to this map or not, entries themselves cache
   * the bounds that a value has when {@link #add(BoundsHolder)} is called, so that changes to the
   * underlying value don't alter the bounds of the entry. Because if that happened, this map
   * wouldn't know in which chunks an entry was stored.
   */
  protected final Map<T, Entry<T>> entries
      = new Object2ObjectOpenHashMap<>();

  /**
   * The total area this map encompasses, may be null.
   * <p>
   * The issue with this bounding box is that it is never downsized when removing objects from the
   * map, only when the map becomes empty or is explicitly cleared is it reset to null.
   */
  private Bounds3i totalArea;

  /* ------------------------------ UTILITY ------------------------------- */

  /**
   * Translates a block position into a packed chunk position.
   * <p>
   * The translation method used here is not implemented in vanilla minecraft, this is just a quick
   * way to turn a 3d position that's been bit-shifted to align to chunks, into a long
   */
  @SuppressWarnings("deprecation")
  private static long toChunkLong(Vector3i blockPos) {
    int x = Vectors.toChunk(blockPos.x());
    int y = Vectors.toChunk(blockPos.y());
    int z = Vectors.toChunk(blockPos.z());
    return Block.getBlockKey(x, y, z);
  }

  /**
   * Iterates through the chunks of the given bounds
   */
  @SuppressWarnings("deprecation")
  private static void forEachChunk(AbstractBounds3i<?> bounds3i,
                                   LongConsumer consumer
  ) {
    // Min chunk pos
    int minX = Vectors.toChunk(bounds3i.minX());
    int minY = Vectors.toChunk(bounds3i.minY());
    int minZ = Vectors.toChunk(bounds3i.minZ());

    // Max chunk pos
    int maxX = Vectors.toChunk(bounds3i.maxX());
    int maxY = Vectors.toChunk(bounds3i.maxY());
    int maxZ = Vectors.toChunk(bounds3i.maxZ());

    // Loop through all bounds
    for (int x = minX; x <= maxX; x++) {
      for (int y = minY; y <= maxY; y++) {
        for (int z = minZ; z <= maxZ; z++) {
          long packed = Block.getBlockKey(x, y, z);
          consumer.accept(packed);
        }
      }
    }
  }

  /* ------------------------------ GETTERS ------------------------------- */

  /**
   * Gets the total area this map encompasses. If the map is empty, this will return
   * {@link Bounds3i#EMPTY} instead.
   *
   * @return The map's total area
   */
  public @NotNull Bounds3i getTotalArea() {
    return totalArea == null ? Bounds3i.EMPTY : totalArea;
  }

  /* ------------------------------ METHODS ------------------------------- */

  /**
   * Gets all entries that overlap the given bounds
   *
   * @param bounds3i The bounds to find overlapping entries for
   * @return All overlapping entries, empty, if none found
   */
  public @NotNull Set<T> getOverlapping(@NotNull AbstractBounds3i<?> bounds3i) {
    Objects.requireNonNull(bounds3i, "Bounds");

    // If we're empty, return empty
    if (isEmpty() || !totalArea.overlaps(bounds3i)) {
      return ObjectSets.emptySet();
    }

    Set<T> result = new ObjectOpenHashSet<>();

    // Go through chunks and find all overlapping entries
    // Since set is being used, entries shouldn't
    // occur more than once
    forEachChunk(bounds3i, value -> {
      List<Entry<T>> list = getChunk(value);

      for (Entry<T> p : list) {
        if (p.bounds3i.overlaps(bounds3i)) {
          result.add(p.value);
        }
      }
    });

    return result;
  }

  /**
   * Gets all entries that contain the given position
   *
   * @param pos The position to query entries for
   * @return All entries at the given position, empty, if none found
   */
  public @NotNull Set<T> get(@NotNull Vector3i pos) {
    Objects.requireNonNull(pos, "pos");

    if (isEmpty() || !totalArea.contains(pos)) {
      return ObjectSets.emptySet();
    }

    return getChunk(toChunkLong(pos))
        .stream()
        .filter(pair -> pair.bounds3i().contains(pos))
        .map(Entry::value)
        .collect(ObjectOpenHashSet.toSet());
  }

  /**
   * Gets the list of entries for the given chunk.
   * <p>
   * Will return an empty set if the given chunk does not exist, and if it does, but is empty, it'll
   * remove said chunk from this map
   */
  private @NotNull List<Entry<T>> getChunk(long l) {
    ChunkList<T> chunk = chunkMap.get(l);

    if (chunk == null) {
      return ObjectLists.emptyList();
    }

    if (chunk.isEmpty()) {
      chunkMap.remove(l);
      return ObjectLists.emptyList();
    }

    return chunk;
  }

  /**
   * Adds the given entry to this map along with its bounds.
   * <p>
   * <b>Be aware!</b> If the bounds of the underlying entry change,
   * then they will not be updated within this map until it's updated.
   * <p>
   * By updated, I mean calling this method again with the object's bounds updated.
   *
   * @param value The entry to add
   * @return True, if the map changed as a result of this method call, false otherwise
   */
  public boolean add(@NotNull T value) {
    Objects.requireNonNull(value, "Value");
    Entry<T> existing = entries.get(value);

    // Test if already added to map, if it is
    // Test if the bounds are different, if they are
    // update bounds, else return false
    if (existing != null) {
      if (!existing.bounds3i().equals(value.getBounds())) {
        _remove(existing);
      } else {
        return false;
      }
    }

    Entry<T> entry = new Entry<>(value, value.getBounds());
    entries.put(value, entry);

    // Update total area
    if (totalArea == null) {
      totalArea = Bounds3i.of(value.getBounds());
    } else {
      totalArea = totalArea.combine(value.getBounds());
    }

    // Add to each chunk's list
    forEachChunk(value.getBounds(), cPos -> {
      var list = chunkMap.computeIfAbsent(cPos, pos -> new ChunkList<>());
      list.add(entry);
    });

    return true;
  }

  /**
   * Finds the nearest entry along with the distance the point had from the nearest result.
   * <p>
   * This uses {@link AbstractBounds3i#getClosestPosition(Vector3d)} for distance calculation, in
   * other words, the distance value used by this method is that of the given point's distance to
   * the edge of an entry, and not to the entry's center.
   * <p>
   * If the given point is already within an entry, then that entry is returned with a distance of
   * 0. If this map is entry, then a result with a null value and a -1 distance is returned.
   * <p>
   * At present, this method runs in <code>o(n)</code> time, as it loops through all entries to find
   * the closest one.
   *
   * @param point The point to find the nearest to
   * @return The nearest result.
   */
  @SuppressWarnings("unchecked")
  public @NotNull ObjectDoublePair<T> findNearest(@NotNull Vector3d point) {
    Objects.requireNonNull(point);

    if (isEmpty()) {
      return NO_NEAREST;
    }

    List<Entry<T>> inChunk = getChunk(toChunkLong(point.toInt()));

    // If point is within existing entries, return first one,
    // Don't do distance check
    if (!inChunk.isEmpty()) {
      return ObjectDoublePair.of(inChunk.get(0).value, 0.0D);
    }

    T nearest = null;
    double distSq = Double.MAX_VALUE;

    for (var e : entries.values()) {
      var bounds = e.bounds3i;
      var closest = bounds.getClosestPosition(point);

      double eDist = point.distanceSquared(closest);

      if (eDist < distSq) {
        distSq = eDist;
        nearest = e.value;
      }
    }

    return ObjectDoublePair.of(nearest, GenericMath.sqrt(distSq));
  }

  /**
   * Removes the given value from this map
   *
   * @param value The value to remove
   * @return True, if this map changed as a result of this method call, false otherwise
   */
  public boolean remove(@NotNull T value) {
    Objects.requireNonNull(value);
    Entry<T> entry = entries.remove(value);

    if (entry == null) {
      return false;
    }

    _remove(entry);
    return true;
  }

  private void _remove(Entry<T> entry) {
    // Loop through all the entries chunks
    forEachChunk(entry.bounds3i(), cPos -> {
      List<Entry<T>> chunk = getChunk(cPos);

      if (chunk.isEmpty()) {
        return;
      }

      chunk.remove(entry);
    });

    if (entries.isEmpty()) {
      clear();
    } else {
      chunkMap.values()
          .removeIf(AbstractCollection::isEmpty);
    }
  }

  /**
   * Clears this map.
   */
  public void clear() {
    chunkMap.clear();
    totalArea = null;
    entries.clear();
  }

  /**
   * Gets this map's size
   *
   * @return This map's size
   */
  public int size() {
    return entries.size();
  }

  /**
   * Tests if this map is empty
   *
   * @return True, if this map is empty, false otherwise
   */
  public boolean isEmpty() {
    return entries.isEmpty() || totalArea == null;
  }

  /* ---------------------------- SUB CLASSES ----------------------------- */

  private record Entry<T>(T value, AbstractBounds3i<?> bounds3i) {

  }

  private static class ChunkList<T> extends ObjectArrayList<Entry<T>> {

  }
}
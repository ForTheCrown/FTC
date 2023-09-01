package net.forthecrown.utils.collision;

import static net.forthecrown.utils.math.Vectors.CHUNK_SIZE;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Objects;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.Location;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector2i;

/**
 * 2D Collision map where the collidable units are chunks, instead of user-defined bounding boxes,
 * like in the case of {@link ChunkedMap}
 */
public class ChunkCollisionMap<T> implements CollisionLookup<T> {

  private final Long2ObjectMap<T> map = new Long2ObjectOpenHashMap<>();
  private Reference<World> world;

  public void setWorld(World world) {
    if (world == null) {
      this.world = null;
    } else {
      this.world = new WeakReference<>(world);
    }
  }

  public World getWorld() {
    return world == null ? null : world.get();
  }

  public T put(T value, int chunkX, int chunkZ) {
    long key = Vectors.toChunkLong(chunkX, chunkZ);
    return put(key, value);
  }

  public T put(long chunkKey, T value) {
    return map.put(chunkKey, value);
  }

  public T remove(int chunkX, int chunkZ) {
    return remove(Vectors.toChunkLong(chunkX, chunkZ));
  }

  public T remove(long chunkKey) {
    return map.remove(chunkKey);
  }

  public T get(int chunkX, int chunkZ) {
    return get(Vectors.toChunkLong(chunkX, chunkZ));
  }

  public T get(long chunkKey) {
    return map.get(chunkKey);
  }

  public T get(Location location) {
    if (!Objects.equals(location.getWorld(), getWorld())) {
      return null;
    }

    Vector2i chunkPos = Vectors.intFrom(location).div(16).toVector2(true);
    return get(chunkPos.x(), chunkPos.y());
  }

  public LongSet getChunks(T value) {
    return LongOpenHashSet.toSet(
        map.long2ObjectEntrySet().stream()
            .filter(tEntry -> Objects.equals(value, tEntry.getValue()))
            .mapToLong(Entry::getLongKey)
    );
  }

  public void clear() {
    map.clear();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public void getColliding(World world, Bounds3i bounds3i, CollisionSet<T> out) {
    if (isEmpty()) {
      return;
    }

    var mapWorld = getWorld();
    if (mapWorld == null || !Objects.equals(mapWorld, world)) {
      return;
    }

    // Min chunk pos
    int minX = Vectors.toChunk(bounds3i.minX());
    int minZ = Vectors.toChunk(bounds3i.minZ());

    // Max chunk pos
    int maxX = Vectors.toChunk(bounds3i.maxX());
    int maxZ = Vectors.toChunk(bounds3i.maxZ());

    // Loop through all bounds
    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        long packed = Vectors.toChunkLong(x, z);
        T value = map.get(packed);

        if (value == null) {
          continue;
        }

        var bounds = chunkBounds(world, x, z);
        out.add(new Collision<>(value, world, bounds));
      }
    }
  }

  private Bounds3i chunkBounds(World world, int cX, int cZ) {
    int minX = Vectors.toBlock(cX);
    int minZ = Vectors.toBlock(cZ);

    int maxX = minX + CHUNK_SIZE;
    int maxZ = minZ + CHUNK_SIZE;

    int minY = world.getMinHeight();
    int maxY = world.getMaxHeight();

    return new Bounds3i(minX, minY, minZ, maxX, maxY, maxZ);
  }

  public Collection<Entry<T>> entrySet() {
    return map.long2ObjectEntrySet();
  }
}

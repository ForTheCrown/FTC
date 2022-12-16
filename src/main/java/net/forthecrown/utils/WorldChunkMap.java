package net.forthecrown.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.forthecrown.utils.math.AbstractBounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A sister class to {@link ChunkedMap} to provide
 * a chunk based spatial lookup that takes values being
 * located in different worlds into consideration, in
 * essence, separating all given values between the worlds
 * they're in.
 *
 * @see ChunkedMap
 * @param <T> The map's type
 */
public class WorldChunkMap<T extends BoundsHolder> {
    private final Map<String, ChunkedMap<T>> worlds = new Object2ObjectOpenHashMap<>();

    public boolean add(World world, T value) {
        Objects.requireNonNull(world, "World was null");
        Objects.requireNonNull(value, "Value was null");

        var worldMap = worlds.computeIfAbsent(
                world.getName(),
                s -> new ChunkedMap<>()
        );

        return worldMap.add(value);
    }

    public boolean remove(@NotNull World world, @NotNull T value) {
        Objects.requireNonNull(world, "World was null");
        Objects.requireNonNull(value, "Value was null");

        var worldMap = getWorld(world);

        if (worldMap == null) {
            return false;
        }

        boolean result = worldMap.remove(value);

        if (worldMap.isEmpty()) {
            worlds.remove(world.getName());
        }

        return result;
    }

    public Set<T> get(WorldVec3i vec) {
        return get(vec.getWorld(), vec.getPos());
    }

    public Set<T> get(Location location) {
        return get(location.getWorld(), Vectors.intFrom(location));
    }

    public Set<T> get(World world, Vector3i pos) {
        var worldMap = getWorld(world);

        if (worldMap == null) {
            return ObjectSets.emptySet();
        }

        return worldMap.get(pos);
    }

    public Set<T> getOverlapping(WorldBounds3i bounds3i) {
        return getOverlapping(bounds3i.getWorld(), bounds3i);
    }

    public Set<T> getOverlapping(World world, AbstractBounds3i bounds3i) {
        var worldMap = getWorld(world);

        if (worldMap == null) {
            return ObjectSets.emptySet();
        }

        return worldMap.getOverlapping(bounds3i);
    }

    public ObjectDoublePair<T> findNearest(Location location) {
        return findNearest(Vectors.doubleFrom(location), location.getWorld());
    }

    public ObjectDoublePair<T> findNearest(Vector3d pos, World world) {
        var worldMap = getWorld(world);

        if (worldMap == null) {
            return ChunkedMap.NO_NEAREST;
        }

        return worldMap.findNearest(pos);
    }

    public boolean isEmpty() {
        return worlds.isEmpty();
    }

    public int size() {
        return worlds.values()
                .stream()
                .filter(map -> !map.isEmpty())
                .mapToInt(value -> value.size() + 1)
                .sum();
    }

    public void clear() {
        worlds.clear();
    }

    private ChunkedMap<T> getWorld(World world) {
        var worldMap = worlds.get(world.getName());

        if (worldMap == null) {
            return null;
        }

        if (worldMap.isEmpty()) {
            worlds.remove(world.getName());
            return null;
        }

        return worldMap;
    }
}
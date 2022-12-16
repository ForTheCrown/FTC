package net.forthecrown.utils.math;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static net.forthecrown.utils.Util.MAX_Y;
import static net.forthecrown.utils.Util.MIN_Y;

public class WorldBounds3i extends AbstractBounds3i<WorldBounds3i> implements Iterable<Block> {
    private final WeakReference<World> world;

    public WorldBounds3i(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.world = new WeakReference<>(world);
    }

    public static WorldBounds3i of(World world, Region region) {
        return of(
                world,
                region.getMinimumPoint(),
                region.getMaximumPoint()
        );
    }

    public static WorldBounds3i of(Region region) {
        return of(
                BukkitAdapter.asBukkitWorld(region.getWorld()).getWorld(),
                region
        );
    }

    public static WorldBounds3i of(World world, ProtectedRegion region) {
        return of(world, region.getMinimumPoint(), region.getMaximumPoint());
    }

    public static WorldBounds3i of(Location l, int radius) {
        return of(
                l.clone().subtract(radius, radius, radius),
                l.clone().add(radius, radius, radius)
        );
    }

    public static WorldBounds3i of(Location l1, Location l2) {
        Validate.isTrue(
                l1.getWorld().equals(l2.getWorld()),
                "Locations cannot be in different worlds"
        );

        // GenericMath.floor
        Vector3d p1 = Vectors.doubleFrom(l1);
        Vector3d p2 = Vectors.doubleFrom(l2);

        p1 = p1.min(p2);
        p2 = p1.max(p2).ceil();

        return of(
                l1.getWorld(),

                p1.toInt(),
                p2.toInt()
        );
    }

    public static WorldBounds3i of(World world, BlockVector3 vec1, BlockVector3 vec2) {
        return new WorldBounds3i(world,
                vec1.getX(),
                vec1.getY(),
                vec1.getZ(),

                vec2.getX(),
                vec2.getY(),
                vec2.getZ()
        );
    }

    public static WorldBounds3i of(World world, Vector3i vec1, Vector3i vec2) {
        return new WorldBounds3i(world,
                vec1.x(),
                vec1.y(),
                vec1.z(),

                vec2.x(),
                vec2.y(),
                vec2.z()
        );
    }

    public static WorldBounds3i of(CompoundTag tag) {
        int[] cords = tag.getIntArray("cords");
        World world = Bukkit.getWorld(tag.getString("world"));

        return new WorldBounds3i(world, cords[0], cords[1], cords[2], cords[3], cords[4], cords[5]);
    }

    public static WorldBounds3i of(World world) {
        var border = world.getWorldBorder();
        var center = border.getCenter();
        var radius = border.getSize() / 2;

        var min = center.clone().subtract(radius, 0, radius);
        min.setY(MIN_Y);

        var max = center.clone().add(radius, 0, radius);
        max.setY(MAX_Y);

        return of(min, max);
    }

    public World getWorld() {
        return world == null ? null : world.get();
    }

    public WorldBounds3i setWorld(World world) {
        return new WorldBounds3i(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    protected WorldBounds3i cloneAt(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return new WorldBounds3i(getWorld(), minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean contains(Location vec) {
        if (!world.equals(vec.getWorld())) {
            return false;
        }

        return super.contains(vec);
    }


    @Override
    public boolean contains(AbstractBounds3i o) {
        if (o instanceof WorldBounds3i w) {
            if (!world.equals(w.getWorld())) {
                return false;
            }
        }

        return super.contains(o);
    }

    @Override
    public boolean overlaps(AbstractBounds3i o) {
        if(o instanceof WorldBounds3i w) {
            if(!world.equals(w.getWorld())) return false;
        }

        return super.overlaps(o);
    }

    @NotNull
    @Override
    public BlockIterator iterator() {
        return new BlockIterator(getWorld(), min(), max(), volume());
    }

    public Collection<Entity> getEntities() {
        return getWorld().getNearbyEntities(toBukkit());
    }

    public Collection<Entity> getEntities(Predicate<Entity> predicate) {
        return getWorld().getNearbyEntities(toBukkit(), predicate);
    }

    public <T extends Entity> Collection<T> getEntitiesByType(Class<T> clazz) {
        return getEntitiesByType(clazz, null);
    }

    public <T extends Entity> Collection<T> getEntitiesByType(Class<T> clazz, Predicate<T> predicate) {
        List<T> nearby = new ObjectArrayList<>();

        for (Entity entity : getEntities()) {
            if (!clazz.isAssignableFrom(entity.getClass())) continue;
            T ent = (T) entity;

            if(predicate != null && !predicate.test(ent)) continue;

            nearby.add(ent);
        }

        return nearby;
    }

    public Collection<LivingEntity> getLivingEntities() {
        return getEntitiesByType(LivingEntity.class);
    }

    public Collection<Player> getPlayers() {
        return getEntitiesByType(Player.class);
    }

    @Override
    public Tag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("world", getWorld().getName());
        tag.put("cords", super.save());

        return tag;
    }

    @Override
    public String toString() {
        return "(" + getWorld().getName() + ", " + super.toString() + ")";
    }
}
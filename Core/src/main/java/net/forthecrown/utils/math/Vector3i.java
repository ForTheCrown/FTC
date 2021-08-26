package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.math.BlockVector3;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * A vector with 3 integer coordinates.
 */
public class Vector3i extends AbstractVector3i<Vector3i> {
    public Vector3i(int x, int y, int z) {
        super(x, y, z);
    }

    public Vector3i() {}

    @Override
    protected Vector3i getThis() {
        return this;
    }

    @Override
    protected Vector3i cloneAt(int x, int y, int z) {
        return new Vector3i(x, y, z);
    }

    public static Vector3i of(Block block){
        return new Vector3i(block.getX(), block.getY(), block.getZ());
    }

    public static Vector3i of(Location location){
        return new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Vector3i of(Entity entity){
        return of(entity.getLocation());
    }

    public static Vector3i of(Vector vector){
        return new Vector3i(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static Vector3i of(BlockVector3 vector3){
        return new Vector3i(vector3.getBlockX(), vector3.getBlockY(), vector3.getBlockZ());
    }

    public static Vector3i of(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        final int x = json.get("x").getAsInt();
        final int y = json.get("y").getAsInt();
        final int z = json.get("z").getAsInt();

        return new Vector3i(x, y, z);
    }

    public Block getBlock(World world){
        return world.getBlockAt(x, y, z);
    }

    public <T extends BlockState> T stateAs(World world){
        return (T) getBlock(world).getState();
    }

    public <T extends BlockState> boolean stateIs(World world, Class<T> clazz){
        BlockState state = getBlock(world).getState();
        return clazz.isAssignableFrom(state.getClass());
    }

    public Material getMaterial(World world){
        return getBlock(world).getType();
    }

    public Location toLoc(World world){
        return toLoc(world, 0f, 0f);
    }

    public Location toLoc(World world, float yaw, float pitch) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public WorldVec3i toWorldVector(World world) {
        return new WorldVec3i(world, x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector3i pos = (Vector3i) o;

        return new EqualsBuilder()
                .append(getX(), pos.getX())
                .append(getY(), pos.getY())
                .append(getZ(), pos.getZ())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getX())
                .append(getY())
                .append(getZ())
                .toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "x=" + x +
                ",y=" + y +
                ",z=" + z +
                '}';
    }
}

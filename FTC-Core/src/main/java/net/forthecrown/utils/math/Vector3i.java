package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import com.sk89q.worldedit.math.BlockVector3;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Chunk;
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
    public static final Vector3i ZERO = new Vector3i(0, 0, 0, true);

    public Vector3i(int x, int y, int z) {
        super(x, y, z);
    }

    Vector3i(int x, int y, int z, boolean immutable) {
        super(x, y, z, immutable);
    }

    @Override
    protected Vector3i getThis() {
        return this;
    }

    @Override
    protected Vector3i cloneAt(int x, int y, int z, boolean immutable) {
        return new Vector3i(x, y, z, immutable);
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

    public static Vector3i of(Vec3i pos) {
        return new Vector3i(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vector3i of(double x, double y, double z) {
        return new Vector3i((int) x, (int) y, (int) z);
    }

    public static Vector3i of(Tag tag) {
        IntArrayTag arr = (IntArrayTag) tag;
        int[] val = arr.getAsIntArray();

        return new Vector3i(val[0], val[1], val[2]);
    }

    public static Vector3i of(long l) {
        return of(BlockPos.of(l));
    }

    // Deserialization function
    // If it's legacy, aka an object, deserialize
    // from the x y z values in the object
    // if it's not, deserialize from an int array
    public static Vector3i of(JsonElement element){
        if(element.isJsonObject()) {
            JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

            return new Vector3i(
                    json.getInt("x"),
                    json.getInt("y"),
                    json.getInt("z")
            );
        }

        int[] cords = JsonUtils.readIntArray(element.getAsJsonArray());
        return new Vector3i(cords[0], cords[1], cords[2]);
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

    public Chunk getChunk(World world) {
        ChunkPos pos = getChunkPos();
        return world.getChunkAt(pos.x, pos.z);
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
}

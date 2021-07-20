package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.math.BlockVector3;
import net.forthecrown.serializer.JsonSerializable;
import net.minecraft.core.Vec3i;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * Represents the position of a block, aka just 3 integers
 */
public class BlockPos implements JsonSerializable, Cloneable, ImmutableBlockPos {
    public int x;
    public int y;
    public int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(int x, int z) {
        this.x = x;
        this.y = 0;
        this.z = z;
    }

    public BlockPos() { }

    public static BlockPos of(Block block){
        return new BlockPos(block.getX(), block.getY(), block.getZ());
    }

    public static BlockPos of(Location location){
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockPos of(Entity entity){
        return of(entity.getLocation());
    }

    public static BlockPos of(Vector vector){
        return new BlockPos(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static BlockPos of(BlockVector3 vector3){
        return new BlockPos(vector3.getBlockX(), vector3.getBlockY(), vector3.getBlockZ());
    }

    public static BlockPos of(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        final int x = json.get("x").getAsInt();
        final int y = json.get("y").getAsInt();
        final int z = json.get("z").getAsInt();

        return new BlockPos(x, y, z);
    }

    public int getX() {
        return x;
    }

    public BlockPos setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public BlockPos setY(int y) {
        this.y = y;
        return this;
    }

    public int getZ() {
        return z;
    }

    public BlockPos setZ(int z) {
        this.z = z;
        return this;
    }

    public BlockPos above(int amount) { return inDirection(BlockFace.UP, amount); }
    public BlockPos above() { return inDirection(BlockFace.UP, 1); }

    public BlockPos below(int amount) { return inDirection(BlockFace.DOWN, amount); }
    public BlockPos below() { return inDirection(BlockFace.DOWN, 1); }

    public BlockPos inDirection(BlockFace face){ return inDirection(face, 1); }

    public BlockPos inDirection(BlockFace face, int amount){
        final int x = face.getModX() * amount;
        final int y = face.getModY() * amount;
        final int z = face.getModZ() * amount;

        return new BlockPos(x + this.x, y + this.y, z + this.z);
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

    public Vector toVec(){
        return new Vector(x, y, z);
    }

    public BlockPos subtract(ImmutableBlockPos pos){ return subtract(pos.getX(), pos.getY(), pos.getZ()); }
    public BlockPos subtract(Location l){ return subtract(l.getBlockX(), l.getBlockY(), l.getBlockZ()); }
    public BlockPos subtract(Vector vector){ return subtract(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()); }
    public BlockPos subtract(Vec3i vec3i){ return subtract(vec3i.getX(), vec3i.getY(), vec3i.getZ()); }

    public BlockPos add(ImmutableBlockPos pos){ return add(pos.getX(), pos.getY(), pos.getZ()); }
    public BlockPos add(Location l){ return add(l.getBlockX(), l.getBlockY(), l.getBlockZ()); }
    public BlockPos add(Vector vector){ return add(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()); }
    public BlockPos add(Vec3i vec3i){ return add(vec3i.getX(), vec3i.getY(), vec3i.getZ()); }

    public BlockPos add(int x, int y, int z){
        this.x += x;
        this.y += y;
        this.z += z;

        return this;
    }

    public BlockPos subtract(int x, int y, int z){
        this.x -= x;
        this.y -= y;
        this.z -= z;

        return this;
    }

    public net.minecraft.core.BlockPos toNms(){
        return new net.minecraft.core.BlockPos(x, y, z);
    }

    public BlockVector3 toWE(){
        return BlockVector3.at(x, y, z);
    }

    @Override
    public BlockPos clone() {
        return new BlockPos(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockPos pos = (BlockPos) o;

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

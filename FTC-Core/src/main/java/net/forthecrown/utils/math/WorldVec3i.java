package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.math.BlockVector3;
import net.forthecrown.serializer.JsonWrapper;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Validate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;

public class WorldVec3i extends AbstractVector3i<WorldVec3i> {
    private World world;

    public WorldVec3i(World world, int x, int y, int z) {
        super(x, y, z);
        this.world = Validate.notNull(world);
    }

    private WorldVec3i(World world, int x, int y, int z, boolean immutable) {
        super(x, y, z, immutable);
        this.world = world;
    }

    public static WorldVec3i of(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        //Save cords as an array, instead of an object with x y z key-values
        int[] cords = json.getIntArray("cords");

        return new WorldVec3i(
                Bukkit.getWorld(json.getString("world")),
                cords[0], cords[1], cords[2]
        );
    }

    public static WorldVec3i of(Location location) {
        return new WorldVec3i(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static WorldVec3i of(Block block){
        return new WorldVec3i(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    public static WorldVec3i of(Entity entity){
        return of(entity.getLocation());
    }

    // WorldEdit adapter
    public static WorldVec3i of(World world, BlockVector3 point) {
        return new WorldVec3i(world, point.getX(), point.getY(), point.getZ());
    }

    public Location toLocation() {
        return new Location(world, x, y, z);
    }

    public Location toLocation(float yaw, float pitch) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = Validate.notNull(world);
    }

    public Block getBlock() {
        return getWorld().getBlockAt(x, y, z);
    }

    public Chunk getChunk() {
        return world.getChunkAt(
                SectionPos.blockToSectionCoord(x),
                SectionPos.blockToSectionCoord(z)
        );
    }

    public boolean stateIs(Class<? extends BlockState> state) {
        return state.isAssignableFrom(getBlock().getState().getClass());
    }

    public <T extends BlockState> T stateAs(Class<T> state) {
        if(!stateIs(state)) return null;

        return (T) getBlock().getState();
    }

    public Vector3i toNonWorld() {
        return new Vector3i(getX(), getY(), getZ(), immutable);
    }

    public Material getMaterial() {
        return getBlock().getType();
    }

    @Override
    protected WorldVec3i getThis() {
        return this;
    }

    @Override
    protected WorldVec3i cloneAt(int x, int y, int z, boolean immutable) {
        return new WorldVec3i(world, x, y, z, immutable);
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("world", world.getName());
        json.add("cords", super.serialize());

        return json;
    }

    @Override
    public Tag saveAsTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("world", world.getName());
        tag.put("cords", super.saveAsTag());

        return tag;
    }

    @Override
    public String toString() {
        return "[" + getWorld().getName() + ": " + getX() + ", " + getY() + ", " + getZ() + "]";
    }
}
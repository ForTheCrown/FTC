package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonWrapper;
import net.minecraft.core.SectionPos;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.entity.Entity;

public class WorldVec3i extends AbstractVector3i<WorldVec3i> {
    private World world;

    public WorldVec3i(World world, int x, int y, int z) {
        super(x, y, z);
        this.world = Validate.notNull(world);
    }

    public static WorldVec3i of(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        return new WorldVec3i(
                Bukkit.getWorld(json.getString("world")),
                json.getInt("x"),
                json.getInt("y"),
                json.getInt("z")
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
        return new Vector3i(getX(), getY(), getZ());
    }

    public Material getMaterial() {
        return getBlock().getType();
    }

    @Override
    protected WorldVec3i getThis() {
        return this;
    }

    @Override
    protected WorldVec3i cloneAt(int x, int y, int z) {
        return new WorldVec3i(world, x, y, z);
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = super.serialize();

        json.addProperty("world", world.getName());

        return json;
    }
}

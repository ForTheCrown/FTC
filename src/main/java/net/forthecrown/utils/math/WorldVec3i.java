package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonWrapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.spongepowered.math.vector.Vector3i;

@Getter
@RequiredArgsConstructor
public class WorldVec3i implements JsonSerializable {
    private final World world;
    @Delegate
    private final Vector3i pos;

    public WorldVec3i(World world, int x, int y, int z) {
        this.world = world;
        this.pos = Vector3i.from(x, y, z);
    }

    public static WorldVec3i of(Location l) {
        return new WorldVec3i(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public static WorldVec3i of(Block block) {
        return new WorldVec3i(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    public static WorldVec3i of(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        if (json.has(Vectors.AXIS_X)) {
            var pos = Vectors.read3i(json.getSource());

            return new WorldVec3i(
                    Bukkit.getWorld(json.getString("world")),
                    pos
            );
        }

        //Save cords as an array, instead of an object with x y z key-values
        int[] cords = json.getIntArray("cords");

        return new WorldVec3i(
                Bukkit.getWorld(json.getString("world")),
                cords[0], cords[1], cords[2]
        );
    }

    public Location toLocation() {
        return new Location(world, x(), y(), z());
    }

    public Block getBlock() {
        return Vectors.getBlock(pos, world);
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = (JsonObject) Vectors.writeJson(pos);
        json.addProperty("world", world.getName());

        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof WorldVec3i)) {
            return false;
        }

        WorldVec3i i = (WorldVec3i) o;

        return new EqualsBuilder()
                .append(getWorld(), i.getWorld())
                .append(getPos(), i.getPos())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getWorld())
                .append(getPos())
                .toHashCode();
    }
}